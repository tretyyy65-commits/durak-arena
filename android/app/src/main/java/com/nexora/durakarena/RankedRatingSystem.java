package com.nexora.durakarena;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Ranked trophy economy for Durak Arena.
 *
 * Goals:
 *  - early leagues feel responsive;
 *  - higher leagues become progressively harder;
 *  - even a perfect player cannot rush from 0 cups to Legend in a short session;
 *  - Profile, Glory Path and Main Menu continue to read the same player_cups value.
 */
public final class RankedRatingSystem {
    private RankedRatingSystem() {}

    // Per league family: Novice, Pro, Master, Elite, Legend.
    // Perfect run 0 -> Legend I (6000 cups) requires about 253 wins.
    private static final int[] WIN_GAIN = {28, 26, 24, 22, 18};
    private static final int[] LOSS_PENALTY = {8, 12, 16, 20, 22};

    public static int winGainForCups(int cups) {
        int family = LeagueSystem.familyForIndex(LeagueSystem.indexForCups(cups));
        return WIN_GAIN[Math.max(0, Math.min(WIN_GAIN.length - 1, family))];
    }

    public static int lossPenaltyForCups(int cups) {
        int family = LeagueSystem.familyForIndex(LeagueSystem.indexForCups(cups));
        return LOSS_PENALTY[Math.max(0, Math.min(LOSS_PENALTY.length - 1, family))];
    }

    public static Result apply(Context context, boolean won) {
        SharedPreferences prefs = context.getSharedPreferences(
                "durak_arena_profile",
                Context.MODE_PRIVATE
        );

        int oldCups = Math.max(0, prefs.getInt("player_cups", 0));
        int oldRankIndex = LeagueSystem.indexForCups(oldCups);

        int wins = Math.max(0, prefs.getInt("player_wins", 0));
        int losses = Math.max(0, prefs.getInt("player_losses", 0));
        int streak = Math.max(0, prefs.getInt("player_win_streak", 0));
        int bestSeason = Math.max(oldCups, prefs.getInt("best_season_cups", oldCups));

        int rawDelta;
        int newCups;

        if (won) {
            rawDelta = winGainForCups(oldCups);
            newCups = oldCups + rawDelta;
            wins += 1;
            streak += 1;
        } else {
            rawDelta = -lossPenaltyForCups(oldCups);
            newCups = Math.max(0, oldCups + rawDelta);
            losses += 1;
            streak = 0;
        }

        int actualDelta = newCups - oldCups;
        bestSeason = Math.max(bestSeason, newCups);

        int newRankIndex = LeagueSystem.indexForCups(newCups);

        prefs.edit()
                .putInt("player_cups", newCups)
                .putInt("player_wins", wins)
                .putInt("player_losses", losses)
                .putInt("player_win_streak", streak)
                .putInt("best_season_cups", bestSeason)
                .putInt("last_ranked_delta", actualDelta)
                .putBoolean("last_ranked_win", won)
                .apply();

        return new Result(
                won,
                oldCups,
                newCups,
                actualDelta,
                oldRankIndex,
                newRankIndex,
                streak
        );
    }

    public static final class Result {
        public final boolean won;
        public final int oldCups;
        public final int newCups;
        public final int delta;
        public final int oldRankIndex;
        public final int newRankIndex;
        public final int winStreak;

        Result(
                boolean won,
                int oldCups,
                int newCups,
                int delta,
                int oldRankIndex,
                int newRankIndex,
                int winStreak
        ) {
            this.won = won;
            this.oldCups = oldCups;
            this.newCups = newCups;
            this.delta = delta;
            this.oldRankIndex = oldRankIndex;
            this.newRankIndex = newRankIndex;
            this.winStreak = winStreak;
        }

        public boolean rankedUp() {
            return newRankIndex > oldRankIndex;
        }

        public boolean rankedDown() {
            return newRankIndex < oldRankIndex;
        }

        public String oldRankName() {
            return LeagueSystem.NAMES[oldRankIndex];
        }

        public String newRankName() {
            return LeagueSystem.NAMES[newRankIndex];
        }
    }
}
