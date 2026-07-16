package org.obscura.backend.twitch;

public record TwitchStream(
        String streamerName,
        String streamerLogin,
        String title,
        int viewerCount,
        String thumbnailUrl) {
}
