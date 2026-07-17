package org.obscura.backend.steam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SteamOpenIdClientTest {

    private MockRestServiceServer mockServer;
    private SteamOpenIdClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new SteamOpenIdClient(builder.build());
    }

    @Test
    void buildLoginRedirectUrl_pointsAtSteamWithRequiredParams() {
        String url = client.buildLoginRedirectUrl(
                "http://localhost:8080",
                "http://localhost:8080/api/auth/steam/callback");

        assertThat(url).startsWith("https://steamcommunity.com/openid/login?")
                .contains("openid.mode=checkid_setup")
                .contains("openid.return_to=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fauth%2Fsteam%2Fcallback")
                .contains("openid.realm=http%3A%2F%2Flocalhost%3A8080");
    }

    @Test
    void verify_returnsTrue_whenSteamConfirmsValid() {
        mockServer.expect(requestTo("https://steamcommunity.com/openid/login"))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "ns:http://specs.openid.net/auth/2.0\nis_valid:true\n",
                        MediaType.TEXT_PLAIN));

        boolean result = client.verify(Map.of(
                "openid.mode", "id_res",
                "openid.claimed_id", "https://steamcommunity.com/openid/id/76561198012345678"));

        assertThat(result).isTrue();
    }

    @Test
    void verify_returnsFalse_whenSteamRejectsResponse() {
        mockServer.expect(requestTo("https://steamcommunity.com/openid/login"))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "ns:http://specs.openid.net/auth/2.0\nis_valid:false\n",
                        MediaType.TEXT_PLAIN));

        boolean result = client.verify(Map.of(
                "openid.mode", "id_res",
                "openid.claimed_id", "https://steamcommunity.com/openid/id/76561198012345678"));

        assertThat(result).isFalse();
    }

    @Test
    void extractSteamId_parsesTrailingNumericId() {
        String steamId = client.extractSteamId("https://steamcommunity.com/openid/id/76561198012345678");

        assertThat(steamId).isEqualTo("76561198012345678");
    }

    @Test
    void extractSteamId_rejectsMalformedClaimedId() {
        assertThrows(IllegalArgumentException.class, () -> client.extractSteamId("not-a-valid-id"));
    }

    @Test
    void extractSteamId_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> client.extractSteamId(null));
    }
}
