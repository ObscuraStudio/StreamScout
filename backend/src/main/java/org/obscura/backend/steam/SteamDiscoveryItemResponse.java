package org.obscura.backend.steam;

public record SteamDiscoveryItemResponse(int appId, String name, String releaseDate, String capsuleImageUrl, int rank) {

    public static SteamDiscoveryItemResponse fromListing(SteamStoreListing listing) {
        return new SteamDiscoveryItemResponse(
                listing.appId(), listing.name(), listing.releaseDate(), listing.capsuleImageUrl(), listing.rank());
    }
}
