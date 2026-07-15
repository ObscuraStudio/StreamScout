package org.obscura.backend.favourite;

import org.junit.jupiter.api.Test;
import org.obscura.backend.steam.SteamOpenIdClient;
import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.user.Favourite;
import org.obscura.backend.user.User;
import org.obscura.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class FavouriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    // Present so the application context starts (AuthController/LibraryController depend on them).
    @MockitoBean
    private SteamOpenIdClient steamOpenIdClient;

    @MockitoBean
    private SteamWebApiClient steamWebApiClient;

    private static UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, List.of());
    }

    @Test
    void list_returnsUnauthorized_whenNoAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/favourites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_returnsUsersFavourites() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        User stored = new User("76561198012345678", "Name", null);
        stored.addFavourite(new Favourite(440, "Team Fortress 2", "https://img/440"));
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.of(stored));

        mockMvc.perform(get("/api/favourites").with(authentication(auth(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appId").value(440))
                .andExpect(jsonPath("$[0].name").value("Team Fortress 2"))
                .andExpect(jsonPath("$[0].imageUrl").value("https://img/440"));
    }

    @Test
    void add_addsFavourite_andIsIdempotent() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        User stored = new User("76561198012345678", "Name", null);
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.of(stored));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = "{\"appId\":440,\"name\":\"Team Fortress 2\",\"imageUrl\":\"https://img/440\"}";

        mockMvc.perform(post("/api/favourites")
                        .with(authentication(auth(principal))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/favourites")
                        .with(authentication(auth(principal))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        assertThat(stored.getFavourites()).hasSize(1);
        assertThat(stored.getFavourites().getFirst().appId()).isEqualTo(440);
    }

    @Test
    void remove_removesFavourite() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        User stored = new User("76561198012345678", "Name", null);
        stored.addFavourite(new Favourite(440, "Team Fortress 2", "https://img/440"));
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.of(stored));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(delete("/api/favourites/440")
                        .with(authentication(auth(principal))).with(csrf()))
                .andExpect(status().isOk());

        assertThat(stored.getFavourites()).isEmpty();
    }

    @Test
    void remove_isNoOpWhenAppIdNotFavourited() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        User stored = new User("76561198012345678", "Name", null);
        stored.addFavourite(new Favourite(570, "Dota 2", "https://img/570"));
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.of(stored));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(delete("/api/favourites/440")
                        .with(authentication(auth(principal))).with(csrf()))
                .andExpect(status().isOk());

        assertThat(stored.getFavourites()).hasSize(1);
        assertThat(stored.getFavourites().getFirst().appId()).isEqualTo(570);
    }
}
