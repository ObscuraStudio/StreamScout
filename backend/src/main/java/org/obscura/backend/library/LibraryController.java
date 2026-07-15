package org.obscura.backend.library;

import org.obscura.backend.steam.OwnedGame;
import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
public class LibraryController {

    private final SteamWebApiClient steamWebApiClient;

    public LibraryController(SteamWebApiClient steamWebApiClient) {
        this.steamWebApiClient = steamWebApiClient;
    }

    @GetMapping("/api/library")
    public ResponseEntity<List<GameResponse>> library(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<GameResponse> games = steamWebApiClient.getOwnedGames(user.getSteamId()).stream()
                .sorted(Comparator.comparingInt(OwnedGame::playtimeForeverMinutes).reversed())
                .map(GameResponse::fromOwnedGame)
                .toList();

        return ResponseEntity.ok(games);
    }
}
