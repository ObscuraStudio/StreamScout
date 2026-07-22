package org.obscura.backend.steam;

import org.obscura.backend.exception.SteamApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class SteamWebApiClient {

    private static final Logger log = LoggerFactory.getLogger(SteamWebApiClient.class);
    private static final String GET_PLAYER_SUMMARIES_URL =
            "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key={key}&steamids={steamId}";
    private static final String GET_OWNED_GAMES_URL =
            "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={key}&steamid={steamId}&include_appinfo=1&format=json";
    private static final String GET_SCHEMA_FOR_GAME_URL =
            "https://api.steampowered.com/ISteamUserStats/GetSchemaForGame/v2/?key={key}&appid={appid}";
    private static final String GET_PLAYER_ACHIEVEMENTS_URL =
            "https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/?appid={appid}&key={key}&steamid={steamid}";

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
                    .map(g -> new OwnedGame(
                            g.appid(),
                            g.name(),
                            g.playtime_forever(),
                            g.rtime_last_played() == null ? 0L : g.rtime_last_played(),
                            g.img_icon_url()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to fetch owned games for {}: {}", steamId, e.getMessage());
            throw new SteamApiException("Could not reach Steam");
        }
    }

    public AchievementSummary getAchievementSummary(String steamId, int appId) {
        int total = safeFetchTotalAchievementCount(appId);

        try {
            int achieved = fetchAchievedCount(steamId, appId);
            return new AchievementSummary(achieved, total, false);
        } catch (HttpClientErrorException.Forbidden e) {
            log.warn("Profile not public for achievements: steamId={} appId={}", steamId, appId);
            return new AchievementSummary(0, total, true);
        } catch (Exception e) {
            log.warn("Failed to fetch achieved count for steamId={} appId={}: {}", steamId, appId, e.getMessage());
            return new AchievementSummary(0, total, false);
        }
    }

    private int safeFetchTotalAchievementCount(int appId) {
        try {
            return fetchTotalAchievementCount(appId);
        } catch (Exception e) {
            log.warn("Failed to fetch achievement schema for appId={}: {}", appId, e.getMessage());
            return 0;
        }
    }

    private int fetchTotalAchievementCount(int appId) {
        SchemaResponse response = restClient.get()
                .uri(GET_SCHEMA_FOR_GAME_URL, apiKey, appId)
                .retrieve()
                .body(SchemaResponse.class);

        if (response == null || response.game() == null
                || response.game().availableGameStats() == null
                || response.game().availableGameStats().achievements() == null) {
            return 0;
        }
        return response.game().availableGameStats().achievements().size();
    }

    private int fetchAchievedCount(String steamId, int appId) {
        PlayerAchievementsResponse response = restClient.get()
                .uri(GET_PLAYER_ACHIEVEMENTS_URL, appId, apiKey, steamId)
                .retrieve()
                .body(PlayerAchievementsResponse.class);

        if (response == null || response.playerstats() == null
                || response.playerstats().achievements() == null) {
            return 0;
        }
        return (int) response.playerstats().achievements().stream()
                .filter(a -> a.achieved() == 1)
                .count();
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

    private record Game(int appid, String name, int playtime_forever, Long rtime_last_played, String img_icon_url) {
    }

    private record SchemaResponse(GameSchema game) {
    }

    private record GameSchema(AvailableGameStats availableGameStats) {
    }

    private record AvailableGameStats(List<AchievementDefinition> achievements) {
    }

    private record AchievementDefinition(String name) {
    }

    private record PlayerAchievementsResponse(PlayerStats playerstats) {
    }

    private record PlayerStats(List<PlayerAchievement> achievements, Boolean success) {
    }

    private record PlayerAchievement(String apiname, int achieved) {
    }
}
