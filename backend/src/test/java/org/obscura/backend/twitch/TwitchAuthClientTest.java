package org.obscura.backend.twitch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TwitchAuthClientTest {

    private MockRestServiceServer mockServer;
    private TwitchAuthClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new TwitchAuthClient(builder.build(), "test-client-id", "test-client-secret");
    }

    @Test
    void getAppAccessToken_fetchesAndReturnsToken() {
        mockServer.expect(requestTo("https://id.twitch.tv/oauth2/token"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {"access_token": "abc123", "expires_in": 3600, "token_type": "bearer"}
                        """, MediaType.APPLICATION_JSON));

        String token = client.getAppAccessToken();

        assertThat(token).isEqualTo("abc123");
    }

    @Test
    void getAppAccessToken_returnsCachedToken_withoutRefetching() {
        mockServer.expect(requestTo("https://id.twitch.tv/oauth2/token"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {"access_token": "abc123", "expires_in": 3600, "token_type": "bearer"}
                        """, MediaType.APPLICATION_JSON));

        String first = client.getAppAccessToken();
        String second = client.getAppAccessToken();

        assertThat(first).isEqualTo("abc123");
        assertThat(second).isEqualTo("abc123");
        mockServer.verify();
    }

    @Test
    void getAppAccessToken_refetches_whenCachedTokenIsAlreadyExpired() {
        mockServer.expect(requestTo("https://id.twitch.tv/oauth2/token"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {"access_token": "expired-token", "expires_in": 0, "token_type": "bearer"}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("https://id.twitch.tv/oauth2/token"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {"access_token": "fresh-token", "expires_in": 3600, "token_type": "bearer"}
                        """, MediaType.APPLICATION_JSON));

        String first = client.getAppAccessToken();
        String second = client.getAppAccessToken();

        assertThat(first).isEqualTo("expired-token");
        assertThat(second).isEqualTo("fresh-token");
        mockServer.verify();
    }
}
