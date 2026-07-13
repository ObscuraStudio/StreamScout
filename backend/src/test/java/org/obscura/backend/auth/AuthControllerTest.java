package org.obscura.backend.auth;

import org.junit.jupiter.api.Test;
import org.obscura.backend.steam.PlayerSummary;
import org.obscura.backend.steam.SteamOpenIdClient;
import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.user.User;
import org.obscura.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SteamOpenIdClient steamOpenIdClient;

    @MockitoBean
    private SteamWebApiClient steamWebApiClient;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void me_returnsUnauthorized_whenNoSession() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginFlow_establishesSession_soMeReturnsUser() throws Exception {
        when(steamOpenIdClient.verify(anyMap())).thenReturn(true);
        when(steamOpenIdClient.extractSteamId(anyString())).thenReturn("76561198012345678");
        when(steamWebApiClient.getPlayerSummary("76561198012345678"))
                .thenReturn(new PlayerSummary("76561198012345678", "SomeName", "https://avatar.example/x.jpg"));
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MvcResult callbackResult = mockMvc.perform(get("/api/auth/steam/callback")
                        .param("openid.mode", "id_res")
                        .param("openid.claimed_id", "https://steamcommunity.com/openid/id/76561198012345678"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) callbackResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steamId").value("76561198012345678"))
                .andExpect(jsonPath("$.displayName").value("SomeName"))
                .andExpect(jsonPath("$.avatarUrl").value("https://avatar.example/x.jpg"));
    }

    @Test
    void callback_redirectsWithError_whenSteamVerificationFails() throws Exception {
        when(steamOpenIdClient.verify(anyMap())).thenReturn(false);

        mockMvc.perform(get("/api/auth/steam/callback")
                        .param("openid.mode", "id_res")
                        .param("openid.claimed_id", "https://steamcommunity.com/openid/id/76561198012345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("authError=steam_failed")));
    }
}
