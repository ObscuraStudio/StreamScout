package org.obscura.backend.playercount;

import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayerCountController {

    private final SteamWebApiClient steamWebApiClient;

    public PlayerCountController(SteamWebApiClient steamWebApiClient) {
        this.steamWebApiClient = steamWebApiClient;
    }

    @GetMapping("/api/player-count")
    public ResponseEntity<PlayerCountResponse> playerCount(
            Authentication authentication, @RequestParam int appId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int count = steamWebApiClient.getCurrentPlayerCount(appId);
        return ResponseEntity.ok(new PlayerCountResponse(count));
    }
}
