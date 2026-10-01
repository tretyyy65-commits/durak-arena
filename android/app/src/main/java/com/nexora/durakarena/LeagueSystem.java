package com.nexora.durakarena;

import android.graphics.Color;

/**
 * Single source of truth for Durak Arena league progression.
 * Glory Path, Profile and future leaderboards should read league data only from here.
 */
public final class LeagueSystem {
    private LeagueSystem() {}

    public static final String[] NAMES = {
            "НОВАЧОК I","НОВАЧОК II","НОВАЧОК III",
            "ПРОФІ I","ПРОФІ II","ПРОФІ III",
            "МАЙСТЕР I","МАЙСТЕР II","МАЙСТЕР III",
            "ЕЛІТА I","ЕЛІТА II","ЕЛІТА III",
            "ЛЕГЕНДА I","ЛЕГЕНДА II","ЛЕГЕНДА III"
    };

    public static final String[] FAMILY_NAMES = {
            "НОВАЧОК", "ПРОФІ", "МАЙСТЕР", "ЕЛІТА", "ЛЕГЕНДА"
    };

    public static final int[] STARTS = {
            0,100,250,
            500,800,1200,
            1700,2200,2800,
            3500,4200,5000,
            6000,7000,8500
    };

    public static int indexForCups(int cups) {
        int c = Math.max(0, cups);
        int idx = 0;
        for (int i = 0; i < STARTS.length; i++) {
            if (c >= STARTS[i]) idx = i;
            else break;
        }
        return idx;
    }

    public static int familyForIndex(int index) {
        return Math.max(0, Math.min(4, index / 3));
    }

    public static int tierForIndex(int index) {
        return Math.max(0, Math.min(2, index % 3));
    }

    public static String tierRomanForIndex(int index) {
        return new String[]{"I", "II", "III"}[tierForIndex(index)];
    }

    public static String familyNameForIndex(int index) {
        return FAMILY_NAMES[familyForIndex(index)];
    }

    public static String nameForCups(int cups) {
        return NAMES[indexForCups(cups)];
    }

    public static int startForCups(int cups) {
        return STARTS[indexForCups(cups)];
    }

    public static int nextStartForCups(int cups) {
        int i = indexForCups(cups);
        return i >= STARTS.length - 1 ? STARTS[i] : STARTS[i + 1];
    }

    public static String nextNameForCups(int cups) {
        int i = indexForCups(cups);
        return i >= NAMES.length - 1 ? NAMES[i] : NAMES[i + 1];
    }

    public static boolean isMaxLeague(int cups) {
        return indexForCups(cups) == NAMES.length - 1;
    }

    /** Cups earned inside the current tier. */
    public static int progressForCups(int cups) {
        int c = Math.max(0, cups);
        if (isMaxLeague(c)) return progressMaxForCups(c);
        return Math.max(0, c - startForCups(c));
    }

    /** Cups needed to fill the current tier progress bar. */
    public static int progressMaxForCups(int cups) {
        int c = Math.max(0, cups);
        if (isMaxLeague(c)) return Math.max(1, STARTS[STARTS.length - 1]);
        return Math.max(1, nextStartForCups(c) - startForCups(c));
    }

    /** Displayed path value such as 765 / 800. */
    public static int displayProgressValue(int cups) {
        return Math.max(0, cups);
    }

    /** Displayed target value. At max league it stays on the last threshold. */
    public static int displayProgressTarget(int cups) {
        if (isMaxLeague(cups)) return STARTS[STARTS.length - 1];
        return nextStartForCups(cups);
    }

    /** League colors match the approved mockup: bronze, green, blue, purple, red. */
    public static int familyColorForIndex(int index) {
        switch (familyForIndex(index)) {
            case 0: return Color.rgb(205, 128, 58);   // Novice bronze
            case 1: return Color.rgb(46, 218, 96);    // Pro green
            case 2: return Color.rgb(55, 151, 244);   // Master blue
            case 3: return Color.rgb(185, 66, 239);   // Elite purple
            default: return Color.rgb(238, 62, 50);   // Legend red
        }
    }

    public static int colorForCups(int cups) {
        return familyColorForIndex(indexForCups(cups));
    }

    public static String assetForIndex(int index) {
        int safe = Math.max(0, Math.min(NAMES.length - 1, index));
        int tier = tierForIndex(safe) + 1;
        String family = safe < 3 ? "novice"
                : safe < 6 ? "pro"
                : safe < 9 ? "master"
                : safe < 12 ? "elite"
                : "legend";
        return "rank_" + family + "_" + tier;
    }

    /** Asset suffix kept for older Profile code: novice_1, pro_2, etc. */
    public static String assetSuffixForCups(int cups) {
        String full = assetForIndex(indexForCups(cups));
        return full.startsWith("rank_") ? full.substring(5) : full;
    }
}
