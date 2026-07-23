package org.obscura.backend.steam;

public record OwnedGame(
        int appId, String name, int playtimeForeverMinutes, long lastPlayedEpochSeconds, String iconHash) {
}
