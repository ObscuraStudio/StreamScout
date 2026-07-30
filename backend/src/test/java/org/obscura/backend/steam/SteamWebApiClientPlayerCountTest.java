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

class SteamWebApiClientPlayerCountTest {

    private static final String URL =
            "https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid={appid}";

    private MockRestServiceServer mockServer;
    private SteamWebApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new SteamWebApiClient(builder.build(), "test-api-key");
    }

    @Test
    void getCurrentPlayerCount_returnsCount_whenSuccessful() {
        mockServer.expect(requestToUriTemplate(URL, "1245620"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"response": {"player_count": 31407, "result": 1}}
                        """, MediaType.APPLICATION_JSON));

        int count = client.getCurrentPlayerCount(1245620);

        assertThat(count).isEqualTo(31407);
    }

    @Test
    void getCurrentPlayerCount_returnsZero_whenAppHasNoPlayerCount() {
        mockServer.expect(requestToUriTemplate(URL, "999999999"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"response": {"result": 42}}
                        """, MediaType.APPLICATION_JSON));

        int count = client.getCurrentPlayerCount(999999999);

        assertThat(count).isZero();
    }

    @Test
    void getCurrentPlayerCount_returnsZero_whenRequestFails() {
        mockServer.expect(requestToUriTemplate(URL, "1245620"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        int count = client.getCurrentPlayerCount(1245620);

        assertThat(count).isZero();
    }
}
