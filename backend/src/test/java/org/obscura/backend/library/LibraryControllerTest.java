package org.obscura.backend.library;

import org.junit.jupiter.api.Test;
import org.obscura.backend.steam.OwnedGame;
import org.obscura.backend.steam.SteamOpenIdClient;
import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.user.User;
import org.obscura.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SteamWebApiClient steamWebApiClient;

    // Present so the application context starts (AuthController depends on them).
    @MockitoBean
    private SteamOpenIdClient steamOpenIdClient;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void library_returnsUnauthorized_whenNoAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/library"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void library_returnsGamesSortedByPlaytimeDesc_withHoursAndImageUrl() throws Exception {
        User user = new User("76561198012345678", "SomeName", null);
        when(steamWebApiClient.getOwnedGames("76561198012345678")).thenReturn(List.of(
                new OwnedGame(570, "Dota 2", 60),
                new OwnedGame(440, "Team Fortress 2", 1234)));

        mockMvc.perform(get("/api/library")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(user, null, List.of()))))
                .andExpect(status().isOk())
                // Sorted by playtime desc: TF2 (1234) first, Dota 2 (60) second
                .andExpect(jsonPath("$[0].appId").value(440))
                .andExpect(jsonPath("$[0].name").value("Team Fortress 2"))
                .andExpect(jsonPath("$[0].playtimeHours").value(20.6))
                .andExpect(jsonPath("$[0].imageUrl")
                        .value("https://cdn.cloudflare.steamstatic.com/steam/apps/440/header.jpg"))
                .andExpect(jsonPath("$[1].appId").value(570))
                .andExpect(jsonPath("$[1].playtimeHours").value(1.0));
    }

    @Test
    void library_returnsEmptyArray_whenUserHasNoGames() throws Exception {
        User user = new User("76561198012345678", "SomeName", null);
        when(steamWebApiClient.getOwnedGames("76561198012345678")).thenReturn(List.of());

        mockMvc.perform(get("/api/library")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(user, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
