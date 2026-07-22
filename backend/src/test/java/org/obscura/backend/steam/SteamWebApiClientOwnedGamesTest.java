package org.obscura.backend.steam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.obscura.backend.exception.SteamApiException;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SteamWebApiClientOwnedGamesTest {

    private static final String URI_TEMPLATE =
            "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={key}&steamid={steamId}&include_appinfo=1&format=json";

    private MockRestServiceServer mockServer;
    private SteamWebApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new SteamWebApiClient(builder.build(), "test-api-key");
    }

    @Test
    void getOwnedGames_returnsParsedGames_whenSteamRespondsWithGames() {
        mockServer.expect(requestToUriTemplate(URI_TEMPLATE, "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "game_count": 2,
                            "games": [
                              {"appid": 440, "name": "Team Fortress 2", "playtime_forever": 1234, "rtime_last_played": 1721000000, "img_icon_url": "abc123hash"},
                              {"appid": 570, "name": "Dota 2", "playtime_forever": 60}
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<OwnedGame> games = client.getOwnedGames("76561198012345678");

        assertThat(games).hasSize(2);
        assertThat(games.getFirst().appId()).isEqualTo(440);
        assertThat(games.getFirst().name()).isEqualTo("Team Fortress 2");
        assertThat(games.getFirst().playtimeForeverMinutes()).isEqualTo(1234);
        assertThat(games.getFirst().lastPlayedEpochSeconds()).isEqualTo(1721000000L);
        assertThat(games.getLast().lastPlayedEpochSeconds()).isEqualTo(0L);
        assertThat(games.getFirst().iconHash()).isEqualTo("abc123hash");
        assertThat(games.getLast().iconHash()).isNull();
    }

    @Test
    void getOwnedGames_returnsEmptyList_whenResponseHasNoGames() {
        mockServer.expect(requestToUriTemplate(URI_TEMPLATE, "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"response\": {}}", MediaType.APPLICATION_JSON));

        List<OwnedGame> games = client.getOwnedGames("76561198012345678");

        assertThat(games).isEmpty();
    }

    @Test
    void getOwnedGames_throwsSteamApiException_whenSteamCallFails() {
        mockServer.expect(requestToUriTemplate(URI_TEMPLATE, "test-api-key", "76561198012345678"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        assertThrows(SteamApiException.class, () -> client.getOwnedGames("76561198012345678"));
    }
}
