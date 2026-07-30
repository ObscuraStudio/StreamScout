package org.obscura.backend.playercount;

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
class PlayerCountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SteamWebApiClient steamWebApiClient;

    // Present so the application context starts (AuthController depends on them).
    @MockitoBean
    private SteamOpenIdClient steamOpenIdClient;

    @Test
    void playerCount_returnsUnauthorized_whenNoAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/player-count").param("appId", "1245620"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void playerCount_returnsCount_whenAuthenticated() throws Exception {
        User user = new User("76561198012345678", "SomeName", null);
        when(steamWebApiClient.getCurrentPlayerCount(1245620)).thenReturn(31407);

        mockMvc.perform(get("/api/player-count")
                        .param("appId", "1245620")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(user, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerCount").value(31407));
    }
}
