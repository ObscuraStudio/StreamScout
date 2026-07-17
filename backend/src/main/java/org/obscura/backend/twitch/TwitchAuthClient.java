package org.obscura.backend.twitch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class TwitchAuthClient {

    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
    private static final long EXPIRY_SAFETY_MARGIN_SECONDS = 60;

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

    public TwitchAuthClient(
            RestClient restClient,
            @Value("${twitch.client-id}") String clientId,
            @Value("${twitch.client-secret}") String clientSecret) {
        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getAppAccessToken() {
        CachedToken current = cachedToken.get();
        if (current != null && current.isValid()) {
            return current.token();
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "client_credentials");

        TokenResponse response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        Instant expiresAt = Instant.now().plusSeconds(response.expires_in());
        CachedToken fresh = new CachedToken(response.access_token(), expiresAt);
        cachedToken.set(fresh);
        return fresh.token();
    }

    private record CachedToken(String token, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt.minusSeconds(EXPIRY_SAFETY_MARGIN_SECONDS));
        }
    }

    private record TokenResponse(String access_token, long expires_in, String token_type) {
    }
}
