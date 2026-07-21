package org.obscura.backend.twitch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameNameNormalizerTest {

    @Test
    void stripEditionSuffix_stripsWindowsEdition() {
        assertThat(GameNameNormalizer.stripEditionSuffix("FINAL FANTASY XV WINDOWS EDITION"))
                .isEqualTo("FINAL FANTASY XV");
    }

    @Test
    void stripEditionSuffix_stripsGameOfTheYearEdition() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Fallout 3 Game of the Year Edition"))
                .isEqualTo("Fallout 3");
    }

    @Test
    void stripEditionSuffix_stripsGotyEdition() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Borderlands GOTY Edition"))
                .isEqualTo("Borderlands");
    }

    @Test
    void stripEditionSuffix_stripsBareGoty() {
        assertThat(GameNameNormalizer.stripEditionSuffix("The Witcher 2 GOTY"))
                .isEqualTo("The Witcher 2");
    }

    @Test
    void stripEditionSuffix_stripsDefinitiveEdition() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Divinity: Original Sin Definitive Edition"))
                .isEqualTo("Divinity: Original Sin");
    }

    @Test
    void stripEditionSuffix_stripsUltimateEdition() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Command & Conquer Ultimate Edition"))
                .isEqualTo("Command & Conquer");
    }

    @Test
    void stripEditionSuffix_stripsDeluxeEditionWithHyphenSeparator() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Borderlands 2 - Deluxe Edition"))
                .isEqualTo("Borderlands 2");
    }

    @Test
    void stripEditionSuffix_stripsCompleteEdition() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Skyrim Complete Edition"))
                .isEqualTo("Skyrim");
    }

    @Test
    void stripEditionSuffix_stripsEnhancedEdition() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Halo Enhanced Edition"))
                .isEqualTo("Halo");
    }

    @Test
    void stripEditionSuffix_stripsAnniversaryEdition() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Tomb Raider Anniversary Edition"))
                .isEqualTo("Tomb Raider");
    }

    @Test
    void stripEditionSuffix_stripsRemastered() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Half-Life Remastered"))
                .isEqualTo("Half-Life");
    }

    @Test
    void stripEditionSuffix_stripsRemaster() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Portal Remaster"))
                .isEqualTo("Portal");
    }

    @Test
    void stripEditionSuffix_isCaseInsensitive() {
        assertThat(GameNameNormalizer.stripEditionSuffix("final fantasy xv windows edition"))
                .isEqualTo("final fantasy xv");
    }

    @Test
    void stripEditionSuffix_stripsSuffixWithNoSeparatorWhitespace() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Some Game:Windows Edition"))
                .isEqualTo("Some Game");
    }

    @Test
    void stripEditionSuffix_returnsInputUnchanged_whenNoSuffixMatches() {
        assertThat(GameNameNormalizer.stripEditionSuffix("Counter Strike 2"))
                .isEqualTo("Counter Strike 2");
    }
}
