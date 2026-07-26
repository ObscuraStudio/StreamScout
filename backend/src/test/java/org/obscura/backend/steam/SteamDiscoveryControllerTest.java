package org.obscura.backend.steam;

import org.junit.jupiter.api.Test;
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
class SteamDiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SteamStoreSearchClient steamStoreSearchClient;

    // Present so the application context starts (AuthController depends on them).
    @MockitoBean
    private SteamOpenIdClient steamOpenIdClient;

    @MockitoBean
    private SteamWebApiClient steamWebApiClient;

    private static UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, List.of());
    }

    @Test
    void comingSoon_returnsUnauthorized_whenNoAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/steam/coming-soon"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void comingSoon_returnsListings_whenAuthenticated() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        when(steamStoreSearchClient.getComingSoon(10)).thenReturn(List.of(
                new SteamStoreListing(1234, "Example Game", "Coming Soon", "https://cdn.example.com/1234.jpg", 1)));

        mockMvc.perform(get("/api/steam/coming-soon")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appId").value(1234))
                .andExpect(jsonPath("$[0].name").value("Example Game"))
                .andExpect(jsonPath("$[0].releaseDate").value("Coming Soon"))
                .andExpect(jsonPath("$[0].capsuleImageUrl").value("https://cdn.example.com/1234.jpg"))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    void mostWishlisted_returnsUnauthorized_whenNoAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/steam/most-wishlisted"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mostWishlisted_returnsListings_whenAuthenticated() throws Exception {
        User principal = new User("76561198012345678", "Name", null);
        when(steamStoreSearchClient.getMostWishlisted(10)).thenReturn(List.of(
                new SteamStoreListing(5678, "Anticipated Game", "Q4 2026", "https://cdn.example.com/5678.jpg", 1)));

        mockMvc.perform(get("/api/steam/most-wishlisted")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appId").value(5678))
                .andExpect(jsonPath("$[0].name").value("Anticipated Game"))
                .andExpect(jsonPath("$[0].releaseDate").value("Q4 2026"))
                .andExpect(jsonPath("$[0].capsuleImageUrl").value("https://cdn.example.com/5678.jpg"))
                .andExpect(jsonPath("$[0].rank").value(1));
    }
}
