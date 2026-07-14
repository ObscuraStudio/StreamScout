package org.obscura.backend.auth;

public record UserResponse(String steamId, String displayName, String avatarUrl) {
}
