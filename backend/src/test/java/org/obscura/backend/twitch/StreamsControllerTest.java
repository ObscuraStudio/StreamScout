package org.obscura.backend.twitch;

import org.junit.jupiter.api.Test;
import org.obscura.backend.steam.SteamOpenIdClient;
import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.user.User;
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
class StreamsControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        mockMvc.perform(get("/api/streams").param("name", "Team Fortress 2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void streams_returnsStreams_forAnyGameName_notJustFavourites() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        when(twitchClient.getLiveStreams("Team Fortress 2")).thenReturn(List.of(
                new TwitchStream("SomeStreamer", "somestreamer", "Playing TF2", 42,
                        "https://img/preview-320x180.jpg")));

        mockMvc.perform(get("/api/streams")
                        .param("name", "Team Fortress 2")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].streamerName").value("SomeStreamer"))
                .andExpect(jsonPath("$[0].streamerLogin").value("somestreamer"))
                .andExpect(jsonPath("$[0].title").value("Playing TF2"))
                .andExpect(jsonPath("$[0].viewerCount").value(42))
                .andExpect(jsonPath("$[0].thumbnailUrl").value("https://img/preview-320x180.jpg"));
    }

    @Test
    void streams_returnsEmptyArray_whenTwitchHasNoStreams() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        when(twitchClient.getLiveStreams("Some Obscure Game")).thenReturn(List.of());

        mockMvc.perform(get("/api/streams")
                        .param("name", "Some Obscure Game")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
