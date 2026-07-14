package org.obscura.backend.steam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class SteamWebApiClient {

    private static final Logger log = LoggerFactory.getLogger(SteamWebApiClient.class);
    private static final String GET_PLAYER_SUMMARIES_URL =
            "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key={key}&steamids={steamId}";

    private final RestClient restClient;
    private final String apiKey;

    public SteamWebApiClient(RestClient restClient, @Value("${steam.api-key}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public PlayerSummary getPlayerSummary(String steamId) {
        try {
            PlayerSummariesResponse response = restClient.get()
                    .uri(GET_PLAYER_SUMMARIES_URL, apiKey, steamId)
                    .retrieve()
                    .body(PlayerSummariesResponse.class);

            List<Player> players = response == null || response.response() == null
                    ? List.of()
                    : response.response().players();

            if (players == null || players.isEmpty()) {
                return fallback(steamId);
            }

            Player player = players.getFirst();
            return new PlayerSummary(steamId, player.personaname(), player.avatarfull());
        } catch (Exception e) {
            log.warn("Failed to fetch Steam player summary for {}: {}", steamId, e.getMessage());
            return fallback(steamId);
        }
    }

    private static PlayerSummary fallback(String steamId) {
        return new PlayerSummary(steamId, "Steam User", null);
    }

    private record PlayerSummariesResponse(PlayersWrapper response) {
    }

    private record PlayersWrapper(List<Player> players) {
    }

    private record Player(String steamid, String personaname, String avatarfull) {
    }
}
