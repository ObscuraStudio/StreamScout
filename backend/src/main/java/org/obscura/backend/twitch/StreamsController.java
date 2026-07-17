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

    private final TwitchClient twitchClient;

    public StreamsController(TwitchClient twitchClient) {
        this.twitchClient = twitchClient;
    }

    @GetMapping("/api/streams")
    public ResponseEntity<List<StreamResponse>> streams(
            Authentication authentication, @RequestParam String name) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<StreamResponse> body = twitchClient.getLiveStreams(name).stream()
                .map(StreamResponse::fromTwitchStream)
                .toList();
        return ResponseEntity.ok(body);
    }
}
