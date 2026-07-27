package org.obscura.backend.steam;

public record SteamStoreListing(int appId, String name, String releaseDate, String capsuleImageUrl, int rank) {
}
