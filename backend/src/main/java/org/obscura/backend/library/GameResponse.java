package org.obscura.backend.library;

import org.obscura.backend.steam.OwnedGame;

public record GameResponse(
        int appId,
        String name,
        double playtimeHours,
        String imageUrl,
        long lastPlayedEpochSeconds,
        String iconImageUrl) {

    private static final String HEADER_IMAGE_URL =
            "https://cdn.cloudflare.steamstatic.com/steam/apps/%d/header.jpg";
    private static final String ICON_IMAGE_URL =
            "https://media.steampowered.com/steamcommunity/public/images/apps/%d/%s.jpg";

    public static GameResponse fromOwnedGame(OwnedGame game) {
        double hours = Math.round(game.playtimeForeverMinutes() / 6.0) / 10.0;
        String imageUrl = HEADER_IMAGE_URL.formatted(game.appId());
        String iconImageUrl = game.iconHash() == null || game.iconHash().isBlank()
                ? null
                : ICON_IMAGE_URL.formatted(game.appId(), game.iconHash());
        return new GameResponse(
                game.appId(), game.name(), hours, imageUrl, game.lastPlayedEpochSeconds(), iconImageUrl);
    }
}
