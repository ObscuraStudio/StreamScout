package org.obscura.backend.favourite;

import org.obscura.backend.twitch.TwitchStream;

public record StreamResponse(
        String streamerName,
        String streamerLogin,
        String title,
        int viewerCount,
        String thumbnailUrl) {

    public static StreamResponse fromTwitchStream(TwitchStream stream) {
        return new StreamResponse(
                stream.streamerName(),
                stream.streamerLogin(),
                stream.title(),
                stream.viewerCount(),
                stream.thumbnailUrl());
    }
}
