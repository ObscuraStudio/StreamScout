package org.obscura.backend.steam;

import org.obscura.backend.exception.SteamApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class SteamWebApiClient {

    private static final Logger log = LoggerFactory.getLogger(SteamWebApiClient.class);
    private static final String GET_PLAYER_SUMMARIES_URL =
            "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key={key}&steamids={steamId}";
    private static final String GET_OWNED_GAMES_URL =
            "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={key}&steamid={steamId}&include_appinfo=1&format=json";

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

    public List<OwnedGame> getOwnedGames(String steamId) {
        try {
            OwnedGamesResponse response = restClient.get()
                    .uri(GET_OWNED_GAMES_URL, apiKey, steamId)
                    .retrieve()
                    .body(OwnedGamesResponse.class);

            List<Game> games = response == null || response.response() == null
                    ? List.of()
                    : response.response().games();

            if (games == null) {
                return List.of();
            }

            return games.stream()
                    .map(g -> new OwnedGame(g.appid(), g.name(), g.playtime_forever()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to fetch owned games for {}: {}", steamId, e.getMessage());
            throw new SteamApiException("Could not reach Steam");
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

    private record OwnedGamesResponse(GamesWrapper response) {
    }

    private record GamesWrapper(List<Game> games) {
    }

    private record Game(int appid, String name, int playtime_forever) {
    }
}
