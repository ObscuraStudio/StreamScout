package org.obscura.backend.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String steamId;

    private String displayName;
    private String avatarUrl;
    private Instant createdAt;
    private List<Favourite> favourites = new ArrayList<>();

    public User(String steamId, String displayName, String avatarUrl) {
        this.steamId = steamId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.createdAt = Instant.now();
    }

    public void updateProfile(String displayName, String avatarUrl) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public void addFavourite(Favourite favourite) {
        boolean alreadyPresent = favourites.stream()
                .anyMatch(existing -> existing.appId() == favourite.appId());
        if (!alreadyPresent) {
            favourites.add(favourite);
        }
    }

    public void removeFavourite(int appId) {
        favourites.removeIf(favourite -> favourite.appId() == appId);
    }
}
