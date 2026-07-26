package org.obscura.backend.steam;

import org.obscura.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SteamDiscoveryController {

    private static final int LIMIT = 10;

    private final SteamStoreSearchClient steamStoreSearchClient;

    public SteamDiscoveryController(SteamStoreSearchClient steamStoreSearchClient) {
        this.steamStoreSearchClient = steamStoreSearchClient;
    }

    @GetMapping("/api/steam/coming-soon")
    public ResponseEntity<List<SteamDiscoveryItemResponse>> comingSoon(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(toResponses(steamStoreSearchClient.getComingSoon(LIMIT)));
    }

    @GetMapping("/api/steam/most-wishlisted")
    public ResponseEntity<List<SteamDiscoveryItemResponse>> mostWishlisted(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(toResponses(steamStoreSearchClient.getMostWishlisted(LIMIT)));
    }

    private static List<SteamDiscoveryItemResponse> toResponses(List<SteamStoreListing> listings) {
        return listings.stream().map(SteamDiscoveryItemResponse::fromListing).toList();
    }
}
