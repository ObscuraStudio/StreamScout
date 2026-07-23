package org.obscura.backend.achievements;

import org.junit.jupiter.api.Test;
import org.obscura.backend.steam.AchievementSummary;
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
class AchievementsControllerTest {

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
    void achievements_returnsUnauthorized_whenNoAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/achievements").param("appId", "440"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void achievements_returnsAchievedAndTotal_whenAuthenticated() throws Exception {
        User user = new User("76561198012345678", "SomeName", null);
        when(steamWebApiClient.getAchievementSummary("76561198012345678", 440))
                .thenReturn(new AchievementSummary(2, 3, false));

        mockMvc.perform(get("/api/achievements")
                        .param("appId", "440")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(user, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achieved").value(2))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.profilePrivate").value(false));
    }

    @Test
    void achievements_returnsProfilePrivateFlag_whenProfileIsNotPublic() throws Exception {
        User user = new User("76561198012345678", "SomeName", null);
        when(steamWebApiClient.getAchievementSummary("76561198012345678", 1245620))
                .thenReturn(new AchievementSummary(0, 42, true));

        mockMvc.perform(get("/api/achievements")
                        .param("appId", "1245620")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(user, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achieved").value(0))
                .andExpect(jsonPath("$.total").value(42))
                .andExpect(jsonPath("$.profilePrivate").value(true));
    }
}
