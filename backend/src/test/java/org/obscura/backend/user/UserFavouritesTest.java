package org.obscura.backend.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserFavouritesTest {

    @Test
    void newUser_hasNoFavourites() {
        User user = new User("76561198012345678", "Name", null);
        assertThat(user.getFavourites()).isEmpty();
    }

    @Test
    void addFavourite_addsNewFavourite() {
        User user = new User("76561198012345678", "Name", null);

        user.addFavourite(new Favourite(440, "Team Fortress 2", "https://img/440"));

        assertThat(user.getFavourites()).hasSize(1);
        assertThat(user.getFavourites().getFirst().appId()).isEqualTo(440);
        assertThat(user.getFavourites().getFirst().name()).isEqualTo("Team Fortress 2");
    }

    @Test
    void addFavourite_isIdempotentOnDuplicateAppId() {
        User user = new User("76561198012345678", "Name", null);

        user.addFavourite(new Favourite(440, "Team Fortress 2", "https://img/440"));
        user.addFavourite(new Favourite(440, "Team Fortress 2", "https://img/440"));

        assertThat(user.getFavourites()).hasSize(1);
    }

    @Test
    void removeFavourite_removesMatchingAppId() {
        User user = new User("76561198012345678", "Name", null);
        user.addFavourite(new Favourite(440, "Team Fortress 2", "https://img/440"));
        user.addFavourite(new Favourite(570, "Dota 2", "https://img/570"));

        user.removeFavourite(440);

        assertThat(user.getFavourites()).hasSize(1);
        assertThat(user.getFavourites().getFirst().appId()).isEqualTo(570);
    }

    @Test
    void removeFavourite_isNoOpWhenAbsent() {
        User user = new User("76561198012345678", "Name", null);
        user.addFavourite(new Favourite(570, "Dota 2", "https://img/570"));

        user.removeFavourite(440);

        assertThat(user.getFavourites()).hasSize(1);
    }
}
