package org.obscura.backend.twitch;

import org.obscura.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StreamsController {

    private static final int DEFAULT_TRENDING_LIMIT = 8;

    private final TwitchClient twitchClient;

    public StreamsController(TwitchClient twitchClient) {
        this.twitchClient = twitchClient;
    }

    @GetMapping("/api/streams")
    public ResponseEntity<List<StreamResponse>> streams(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam(required = false) String language) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<StreamResponse> body = twitchClient.getLiveStreams(name, language).stream()
                .map(StreamResponse::fromTwitchStream)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/streams/trending")
    public ResponseEntity<List<StreamResponse>> trending(
            Authentication authentication,
            @RequestParam(required = false) Integer limit) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<StreamResponse> body = twitchClient.getTopStreams(limit == null ? DEFAULT_TRENDING_LIMIT : limit)
                .stream()
                .map(StreamResponse::fromTwitchStream)
                .toList();
        return ResponseEntity.ok(body);
    }
}
