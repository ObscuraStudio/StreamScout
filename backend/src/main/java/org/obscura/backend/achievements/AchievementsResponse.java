package org.obscura.backend.achievements;

import org.obscura.backend.steam.AchievementSummary;

public record AchievementsResponse(int achieved, int total, boolean profilePrivate) {

    public static AchievementsResponse fromSummary(AchievementSummary summary) {
        return new AchievementsResponse(summary.achieved(), summary.total(), summary.profilePrivate());
    }
}
