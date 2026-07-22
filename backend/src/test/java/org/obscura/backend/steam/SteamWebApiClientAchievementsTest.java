package org.obscura.backend.steam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SteamWebApiClientAchievementsTest {

    private static final String SCHEMA_URL =
            "https://api.steampowered.com/ISteamUserStats/GetSchemaForGame/v2/?key={key}&appid={appid}";
    private static final String PLAYER_ACHIEVEMENTS_URL =
            "https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/?appid={appid}&key={key}&steamid={steamid}";

    private MockRestServiceServer mockServer;
    private SteamWebApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new SteamWebApiClient(builder.build(), "test-api-key");
    }

    @Test
    void getAchievementSummary_returnsAchievedAndTotal_whenBothCallsSucceed() {
        mockServer.expect(requestToUriTemplate(SCHEMA_URL, "test-api-key", "440"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "game": {
                            "gameName": "Team Fortress 2",
                            "availableGameStats": {
                              "achievements": [
                                {"name": "ACH_1"},
                                {"name": "ACH_2"},
                                {"name": "ACH_3"}
                              ]
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(PLAYER_ACHIEVEMENTS_URL, "440", "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "playerstats": {
                            "achievements": [
                              {"apiname": "ACH_1", "achieved": 1},
                              {"apiname": "ACH_2", "achieved": 0},
                              {"apiname": "ACH_3", "achieved": 1}
                            ],
                            "success": true
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        AchievementSummary summary = client.getAchievementSummary("76561198012345678", 440);

        assertThat(summary.achieved()).isEqualTo(2);
        assertThat(summary.total()).isEqualTo(3);
        assertThat(summary.profilePrivate()).isFalse();
    }

    @Test
    void getAchievementSummary_returnsZeroZero_whenGameHasNoAchievementSchema() {
        mockServer.expect(requestToUriTemplate(SCHEMA_URL, "test-api-key", "12345"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"game": {"gameName": "Some Game"}}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(PLAYER_ACHIEVEMENTS_URL, "12345", "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"playerstats": {"error": "Requested app has no stats", "success": false}}
                        """, MediaType.APPLICATION_JSON));

        AchievementSummary summary = client.getAchievementSummary("76561198012345678", 12345);

        assertThat(summary.achieved()).isEqualTo(0);
        assertThat(summary.total()).isEqualTo(0);
        assertThat(summary.profilePrivate()).isFalse();
    }

    @Test
    void getAchievementSummary_returnsZeroZero_whenSchemaCallFails() {
        mockServer.expect(requestToUriTemplate(SCHEMA_URL, "test-api-key", "440"))
                .andExpect(method(GET))
                .andRespond(withServerError());
        mockServer.expect(requestToUriTemplate(PLAYER_ACHIEVEMENTS_URL, "440", "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        AchievementSummary summary = client.getAchievementSummary("76561198012345678", 440);

        assertThat(summary.achieved()).isEqualTo(0);
        assertThat(summary.total()).isEqualTo(0);
        assertThat(summary.profilePrivate()).isFalse();
    }

    @Test
    void getAchievementSummary_returnsProfilePrivate_andPreservesTotal_whenAchievedCallForbidden() {
        mockServer.expect(requestToUriTemplate(SCHEMA_URL, "test-api-key", "1245620"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "game": {
                            "gameName": "ELDEN RING",
                            "availableGameStats": {
                              "achievements": [
                                {"name": "ACH_1"},
                                {"name": "ACH_2"}
                              ]
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(PLAYER_ACHIEVEMENTS_URL, "1245620", "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"playerstats": {"error": "Profile is not public", "success": false}}
                                """));

        AchievementSummary summary = client.getAchievementSummary("76561198012345678", 1245620);

        assertThat(summary.achieved()).isEqualTo(0);
        assertThat(summary.total()).isEqualTo(2);
        assertThat(summary.profilePrivate()).isTrue();
    }
}
