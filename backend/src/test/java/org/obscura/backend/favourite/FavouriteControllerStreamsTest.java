package org.obscura.backend.favourite;

import org.junit.jupiter.api.Test;
import org.obscura.backend.steam.SteamOpenIdClient;
import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.twitch.TwitchClient;
import org.obscura.backend.twitch.TwitchStream;
import org.obscura.backend.user.Favourite;
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
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class FavouriteControllerStreamsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TwitchClient twitchClient;

    // Present so the application context starts (AuthController/LibraryController depend on them).
    @MockitoBean
    private SteamOpenIdClient steamOpenIdClient;

    @MockitoBean
    private SteamWebApiClient steamWebApiClient;

    private static UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, List.of());
    }

    @Test
    void streams_returnsUnauthorized_whenNoAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/favourites/440/streams"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void streams_returnsNotFound_whenAppIdIsNotFavourited() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        User stored = new User("76561198012345678", "Name", null);
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.of(stored));

        mockMvc.perform(get("/api/favourites/440/streams")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(principal))))
                .andExpect(status().isNotFound());
    }

    @Test
    void streams_returnsStreams_whenFavouritedAndTwitchHasResults() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        User stored = new User("76561198012345678", "Name", null);
        stored.addFavourite(new Favourite(440, "Team Fortress 2", "https://img/440"));
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.of(stored));
        when(twitchClient.getLiveStreams("Team Fortress 2")).thenReturn(List.of(
                new TwitchStream("SomeStreamer", "somestreamer", "Playing TF2", 42,
                        "https://img/preview-320x180.jpg")));

        mockMvc.perform(get("/api/favourites/440/streams")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].streamerName").value("SomeStreamer"))
                .andExpect(jsonPath("$[0].streamerLogin").value("somestreamer"))
                .andExpect(jsonPath("$[0].title").value("Playing TF2"))
                .andExpect(jsonPath("$[0].viewerCount").value(42))
                .andExpect(jsonPath("$[0].thumbnailUrl").value("https://img/preview-320x180.jpg"));
    }

    @Test
    void streams_returnsEmptyArray_whenFavouritedButTwitchHasNoStreams() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        User stored = new User("76561198012345678", "Name", null);
        stored.addFavourite(new Favourite(440, "Team Fortress 2", "https://img/440"));
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.of(stored));
        when(twitchClient.getLiveStreams("Team Fortress 2")).thenReturn(List.of());

        mockMvc.perform(get("/api/favourites/440/streams")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
