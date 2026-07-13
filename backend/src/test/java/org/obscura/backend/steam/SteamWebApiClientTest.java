package org.obscura.backend.steam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SteamWebApiClientTest {

    private static final String URI_TEMPLATE =
            "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key={key}&steamids={steamId}";

    private MockRestServiceServer mockServer;
    private SteamWebApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new SteamWebApiClient(builder.build(), "test-api-key");
    }

    @Test
    void getPlayerSummary_returnsParsedProfile_whenSteamRespondsWithPlayer() {
        mockServer.expect(requestToUriTemplate(URI_TEMPLATE, "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "players": [
                              {
                                "steamid": "76561198012345678",
                                "personaname": "SomeName",
                                "avatarfull": "https://avatars.steamstatic.com/full.jpg"
                              }
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PlayerSummary summary = client.getPlayerSummary("76561198012345678");

        assertThat(summary.steamId()).isEqualTo("76561198012345678");
        assertThat(summary.displayName()).isEqualTo("SomeName");
        assertThat(summary.avatarUrl()).isEqualTo("https://avatars.steamstatic.com/full.jpg");
    }

    @Test
    void getPlayerSummary_returnsFallback_whenNoPlayersInResponse() {
        mockServer.expect(requestToUriTemplate(URI_TEMPLATE, "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"response": {"players": []}}
                        """, MediaType.APPLICATION_JSON));

        PlayerSummary summary = client.getPlayerSummary("76561198012345678");

        assertThat(summary.steamId()).isEqualTo("76561198012345678");
        assertThat(summary.displayName()).isEqualTo("Steam User");
        assertThat(summary.avatarUrl()).isNull();
    }

    @Test
    void getPlayerSummary_returnsFallback_whenSteamCallFails() {
        mockServer.expect(requestToUriTemplate(URI_TEMPLATE, "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        PlayerSummary summary = client.getPlayerSummary("76561198012345678");

        assertThat(summary.steamId()).isEqualTo("76561198012345678");
        assertThat(summary.displayName()).isEqualTo("Steam User");
        assertThat(summary.avatarUrl()).isNull();
    }
}
