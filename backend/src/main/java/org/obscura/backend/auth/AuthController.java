package org.obscura.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.obscura.backend.steam.PlayerSummary;
import org.obscura.backend.steam.SteamOpenIdClient;
import org.obscura.backend.steam.SteamWebApiClient;
import org.obscura.backend.user.User;
import org.obscura.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final SteamOpenIdClient steamOpenIdClient;
    private final SteamWebApiClient steamWebApiClient;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;
    private final String frontendUrl;

    public AuthController(
            SteamOpenIdClient steamOpenIdClient,
            SteamWebApiClient steamWebApiClient,
            UserRepository userRepository,
            SecurityContextRepository securityContextRepository,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.steamOpenIdClient = steamOpenIdClient;
        this.steamWebApiClient = steamWebApiClient;
        this.userRepository = userRepository;
        this.securityContextRepository = securityContextRepository;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/api/auth/steam/login")
    public ResponseEntity<Void> login(HttpServletRequest request) {
        String realm = ServletUriComponentsBuilder.fromContextPath(request).build().toUriString();
        String returnTo = realm + "/api/auth/steam/callback";

        String redirectUrl = steamOpenIdClient.buildLoginRedirectUrl(realm, returnTo);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @GetMapping("/api/auth/steam/callback")
    public ResponseEntity<Void> callback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });

        boolean valid;
        try {
            valid = steamOpenIdClient.verify(params);
        } catch (Exception e) {
            log.warn("Steam OpenID verification request failed: {}", e.getMessage());
            valid = false;
        }

        if (!valid) {
            return redirectWithError();
        }

        String steamId;
        try {
            steamId = steamOpenIdClient.extractSteamId(params.get("openid.claimed_id"));
        } catch (IllegalArgumentException e) {
            log.warn("Steam OpenID callback had an invalid claimed_id: {}", e.getMessage());
            return redirectWithError();
        }

        PlayerSummary summary = steamWebApiClient.getPlayerSummary(steamId);

        User user = userRepository.findBySteamId(steamId)
                .orElseGet(() -> new User(steamId, summary.displayName(), summary.avatarUrl()));
        user.updateProfile(summary.displayName(), summary.avatarUrl());
        userRepository.save(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, frontendUrl)
                .build();
    }

    @GetMapping("/api/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UserResponse(user.getSteamId(), user.getDisplayName(), user.getAvatarUrl()));
    }

    private ResponseEntity<Void> redirectWithError() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, frontendUrl + "?authError=steam_failed")
                .build();
    }
}
