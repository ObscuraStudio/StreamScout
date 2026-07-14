package org.obscura.backend.steam;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SteamOpenIdClient {

    private static final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/login";
    private static final String OPENID_NS = "http://specs.openid.net/auth/2.0";
    private static final String IDENTIFIER_SELECT = "http://specs.openid.net/auth/2.0/identifier_select";
    private static final Pattern CLAIMED_ID_PATTERN =
            Pattern.compile("^https://steamcommunity\\.com/openid/id/(\\d+)$");

    private final RestClient restClient;

    public SteamOpenIdClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public String buildLoginRedirectUrl(String realm, String returnTo) {
        return STEAM_OPENID_URL
                + "?openid.ns=" + encode(OPENID_NS)
                + "&openid.mode=checkid_setup"
                + "&openid.return_to=" + encode(returnTo)
                + "&openid.realm=" + encode(realm)
                + "&openid.identity=" + encode(IDENTIFIER_SELECT)
                + "&openid.claimed_id=" + encode(IDENTIFIER_SELECT);
    }

    public boolean verify(Map<String, String> callbackParams) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        callbackParams.forEach(form::add);
        form.set("openid.mode", "check_authentication");

        String response = restClient.post()
                .uri(STEAM_OPENID_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        return response != null && response.contains("is_valid:true");
    }

    public String extractSteamId(String claimedId) {
        if (claimedId == null) {
            throw new IllegalArgumentException("claimedId must not be null");
        }
        Matcher matcher = CLAIMED_ID_PATTERN.matcher(claimedId);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("claimedId is not a valid Steam identity URL: " + claimedId);
        }
        return matcher.group(1);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
