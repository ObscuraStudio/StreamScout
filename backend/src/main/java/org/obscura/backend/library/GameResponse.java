package org.obscura.backend.library;

import org.obscura.backend.steam.OwnedGame;

public record GameResponse(int appId, String name, double playtimeHours, String imageUrl) {

    private static final String HEADER_IMAGE_URL =
            "https://cdn.cloudflare.steamstatic.com/steam/apps/%d/header.jpg";

    public static GameResponse fromOwnedGame(OwnedGame game) {
        double hours = Math.round(game.playtimeForeverMinutes() / 6.0) / 10.0;
        String imageUrl = HEADER_IMAGE_URL.formatted(game.appId());
        return new GameResponse(game.appId(), game.name(), hours, imageUrl);
    }
}
