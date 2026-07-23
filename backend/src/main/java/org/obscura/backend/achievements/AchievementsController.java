package org.obscura.backend.achievements;

import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AchievementsController {

    private final SteamWebApiClient steamWebApiClient;

    public AchievementsController(SteamWebApiClient steamWebApiClient) {
        this.steamWebApiClient = steamWebApiClient;
    }

    @GetMapping("/api/achievements")
    public ResponseEntity<AchievementsResponse> achievements(
            Authentication authentication, @RequestParam int appId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var summary = steamWebApiClient.getAchievementSummary(user.getSteamId(), appId);
        return ResponseEntity.ok(AchievementsResponse.fromSummary(summary));
    }
}
