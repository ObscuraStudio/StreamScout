package org.obscura.backend.twitch;

import org.obscura.backend.exception.TwitchApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TwitchClient {

    private static final Logger log = LoggerFactory.getLogger(TwitchClient.class);
    private static final String GAMES_URL = "https://api.twitch.tv/helix/games?name={name}";
    private static final String STREAMS_URL = "https://api.twitch.tv/helix/streams?game_id={gameId}&first=6";

    private final RestClient restClient;
    private final TwitchAuthClient authClient;
    private final String clientId;

    public TwitchClient(
            RestClient restClient,
            TwitchAuthClient authClient,
            @Value("${twitch.client-id}") String clientId) {
        this.restClient = restClient;
        this.authClient = authClient;
        this.clientId = clientId;
    }

    public List<TwitchStream> getLiveStreams(String gameName) {
        try {
            String gameId = resolveGameId(gameName);
            if (gameId == null) {
                return List.of();
            }
            return fetchStreams(gameId);
        } catch (Exception e) {
            log.warn("Failed to fetch Twitch streams for {}: {}", gameName, e.getMessage());
            throw new TwitchApiException("Could not reach Twitch");
        }
    }

    private String resolveGameId(String gameName) {
        GamesResponse response = restClient.get()
                .uri(GAMES_URL, gameName)
                .header("Authorization", "Bearer " + authClient.getAppAccessToken())
                .header("Client-Id", clientId)
                .retrieve()
                .body(GamesResponse.class);

        List<TwitchGame> games = response == null ? List.of() : response.data();
        if (games == null || games.isEmpty()) {
            return null;
        }
        return games.getFirst().id();
    }

    private List<TwitchStream> fetchStreams(String gameId) {
        StreamsResponse response = restClient.get()
                .uri(STREAMS_URL, gameId)
                .header("Authorization", "Bearer " + authClient.getAppAccessToken())
                .header("Client-Id", clientId)
                .retrieve()
                .body(StreamsResponse.class);

        List<HelixStream> streams = response == null ? List.of() : response.data();
        if (streams == null) {
            return List.of();
        }

        return streams.stream()
                .map(s -> new TwitchStream(
                        s.user_name(),
                        s.user_login(),
                        s.title(),
                        s.viewer_count(),
                        s.thumbnail_url()
                                .replace("{width}", "320")
                                .replace("{height}", "180")))
                .toList();
    }

    private record GamesResponse(List<TwitchGame> data) {
    }

    private record TwitchGame(String id, String name) {
    }

    private record StreamsResponse(List<HelixStream> data) {
    }

    private record HelixStream(
            String user_name,
            String user_login,
            String title,
            int viewer_count,
            String thumbnail_url) {
    }
}
