package org.obscura.backend.favourite;

import org.obscura.backend.twitch.TwitchClient;
import org.obscura.backend.user.Favourite;
import org.obscura.backend.user.User;
import org.obscura.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class FavouriteController {

    private final UserRepository userRepository;
    private final TwitchClient twitchClient;

    public FavouriteController(UserRepository userRepository, TwitchClient twitchClient) {
        this.userRepository = userRepository;
        this.twitchClient = twitchClient;
    }

    @GetMapping("/api/favourites")
    public ResponseEntity<List<FavouriteResponse>> list(Authentication authentication) {
        String steamId = authenticatedSteamId(authentication);
        if (steamId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findBySteamId(steamId).orElseThrow();
        List<FavouriteResponse> body = user.getFavourites().stream()
                .map(f -> new FavouriteResponse(f.appId(), f.name(), f.imageUrl()))
                .toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/api/favourites")
    public ResponseEntity<Void> add(Authentication authentication, @RequestBody FavouriteRequest request) {
        String steamId = authenticatedSteamId(authentication);
        if (steamId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findBySteamId(steamId).orElseThrow();
        user.addFavourite(new Favourite(request.appId(), request.name(), request.imageUrl()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/favourites/{appId}")
    public ResponseEntity<Void> remove(Authentication authentication, @PathVariable int appId) {
        String steamId = authenticatedSteamId(authentication);
        if (steamId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findBySteamId(steamId).orElseThrow();
        user.removeFavourite(appId);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/favourites/{appId}/streams")
    public ResponseEntity<List<StreamResponse>> streams(Authentication authentication, @PathVariable int appId) {
        String steamId = authenticatedSteamId(authentication);
        if (steamId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findBySteamId(steamId).orElseThrow();

        Optional<Favourite> favourite = user.getFavourites().stream()
                .filter(f -> f.appId() == appId)
                .findFirst();
        if (favourite.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<StreamResponse> body = twitchClient.getLiveStreams(favourite.get().name()).stream()
                .map(StreamResponse::fromTwitchStream)
                .toList();
        return ResponseEntity.ok(body);
    }

    private static String authenticatedSteamId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getSteamId();
        }
        return null;
    }
}
