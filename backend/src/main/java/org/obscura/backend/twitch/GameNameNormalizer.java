package org.obscura.backend.twitch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GameNameNormalizer {

    private static final Pattern EDITION_SUFFIX_PATTERN = Pattern.compile(
            "[\\s:-]*+(WINDOWS EDITION|GAME OF THE YEAR EDITION|GOTY EDITION|GOTY|DEFINITIVE EDITION|"
                    + "ULTIMATE EDITION|DELUXE EDITION|COMPLETE EDITION|ENHANCED EDITION|"
                    + "ANNIVERSARY EDITION|REMASTERED|REMASTER)\\s*+$",
            Pattern.CASE_INSENSITIVE);

    private GameNameNormalizer() {
    }

    public static String stripEditionSuffix(String name) {
        Matcher matcher = EDITION_SUFFIX_PATTERN.matcher(name);
        if (matcher.find()) {
            return name.substring(0, matcher.start()).trim();
        }
        return name;
    }
}
