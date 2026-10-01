package com.nexora.durakarena;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Temporary ranked match shell.
 *
 * Until the full Durak gameplay resolves a match automatically, the two result
 * buttons let us verify the real ranked economy end-to-end. A result can only
 * be applied once per opened match, preventing accidental repeated trophy gain.
 */
public final class RankedGameView {
    private RankedGameView() {}

    public interface Listener { void onExit(); }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static GradientDrawable panel(Context context, int color, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setStroke(dp(context, 1), stroke);
        drawable.setCornerRadius(dp(context, radius));
        return drawable;
    }

    public static View create(Context context, String playerName, int cups, Listener listener) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(
                dp(context, 22),
                dp(context, 30),
                dp(context, 22),
                dp(context, 24)
        );
        root.setBackgroundColor(Color.rgb(4, 18, 13));

        TextView title = new TextView(context);
        title.setText("РЕЙТИНГОВИЙ МАТЧ");
        title.setTextColor(Color.rgb(240, 196, 82));
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        final int[] currentCups = {Math.max(0, cups)};
        final boolean[] resultApplied = {false};

        TextView player = new TextView(context);
        player.setTextColor(Color.WHITE);
        player.setTextSize(16);
        player.setGravity(Gravity.CENTER);
        player.setPadding(0, dp(context, 18), 0, dp(context, 10));
        root.addView(player, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView economy = new TextView(context);
        economy.setTextColor(Color.rgb(214, 197, 158));
        economy.setTextSize(13);
        economy.setGravity(Gravity.CENTER);
        economy.setPadding(
                dp(context, 10),
                dp(context, 10),
                dp(context, 10),
                dp(context, 10)
        );
        economy.setBackground(panel(
                context,
                Color.rgb(7, 30, 22),
                Color.rgb(163, 125, 53),
                12
        ));
        root.addView(economy, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView table = new TextView(context);
        table.setText("♠     ♦     ♣     ♥\n\nСТІЛ DURAK ARENA");
        table.setTextColor(Color.WHITE);
        table.setTextSize(24);
        table.setGravity(Gravity.CENTER);
        table.setPadding(0, dp(context, 62), 0, dp(context, 62));
        root.addView(table, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        TextView result = new TextView(context);
        result.setText("");
        result.setTextSize(18);
        result.setTypeface(Typeface.DEFAULT_BOLD);
        result.setGravity(Gravity.CENTER);
        result.setPadding(0, dp(context, 8), 0, dp(context, 12));
        root.addView(result, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout resultButtons = new LinearLayout(context);
        resultButtons.setOrientation(LinearLayout.HORIZONTAL);
        resultButtons.setGravity(Gravity.CENTER);

        Button win = new Button(context);
        win.setText("ПЕРЕМОГА");
        win.setAllCaps(false);
        win.setTextColor(Color.WHITE);
        win.setTypeface(Typeface.DEFAULT_BOLD);
        win.setBackground(panel(
                context,
                Color.rgb(18, 104, 63),
                Color.rgb(232, 183, 76),
                12
        ));

        Button loss = new Button(context);
        loss.setText("ПОРАЗКА");
        loss.setAllCaps(false);
        loss.setTextColor(Color.WHITE);
        loss.setTypeface(Typeface.DEFAULT_BOLD);
        loss.setBackground(panel(
                context,
                Color.rgb(112, 28, 31),
                Color.rgb(232, 183, 76),
                12
        ));

        LinearLayout.LayoutParams halfLeft = new LinearLayout.LayoutParams(
                0,
                dp(context, 58),
                1f
        );
        halfLeft.rightMargin = dp(context, 5);
        LinearLayout.LayoutParams halfRight = new LinearLayout.LayoutParams(
                0,
                dp(context, 58),
                1f
        );
        halfRight.leftMargin = dp(context, 5);

        resultButtons.addView(win, halfLeft);
        resultButtons.addView(loss, halfRight);
        root.addView(resultButtons, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button exit = new Button(context);
        exit.setText("ВИЙТИ");
        exit.setAllCaps(false);
        LinearLayout.LayoutParams exitParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 54)
        );
        exitParams.topMargin = dp(context, 10);
        root.addView(exit, exitParams);

        Runnable refresh = () -> {
            int c = currentCups[0];
            player.setText(
                    playerName + "  •  " +
                    LeagueSystem.nameForCups(c) + "  •  " +
                    c + " 🏆"
            );

            economy.setText(
                    "За перемогу: +" + RankedRatingSystem.winGainForCups(c) + " 🏆" +
                    "     •     За поразку: -" + RankedRatingSystem.lossPenaltyForCups(c) + " 🏆"
            );
            economy.setTextColor(LeagueSystem.colorForCups(c));
        };
        refresh.run();

        View.OnClickListener applyWin = v -> {
            if (resultApplied[0]) return;
            resultApplied[0] = true;

            RankedRatingSystem.Result rankedResult =
                    RankedRatingSystem.apply(context, true);

            currentCups[0] = rankedResult.newCups;
            refresh.run();

            StringBuilder text = new StringBuilder();
            text.append("ПЕРЕМОГА   +")
                    .append(rankedResult.delta)
                    .append(" 🏆\n")
                    .append(rankedResult.oldCups)
                    .append(" → ")
                    .append(rankedResult.newCups);

            if (rankedResult.rankedUp()) {
                text.append("\nНОВИЙ РАНГ: ")
                        .append(rankedResult.newRankName());
            }

            if (rankedResult.winStreak >= 2) {
                text.append("\nСерія перемог: ")
                        .append(rankedResult.winStreak);
            }

            result.setText(text.toString());
            result.setTextColor(Color.rgb(80, 230, 126));
            win.setEnabled(false);
            loss.setEnabled(false);
            exit.setText("ПРОДОВЖИТИ");
        };

        View.OnClickListener applyLoss = v -> {
            if (resultApplied[0]) return;
            resultApplied[0] = true;

            RankedRatingSystem.Result rankedResult =
                    RankedRatingSystem.apply(context, false);

            currentCups[0] = rankedResult.newCups;
            refresh.run();

            StringBuilder text = new StringBuilder();
            text.append("ПОРАЗКА   ")
                    .append(rankedResult.delta)
                    .append(" 🏆\n")
                    .append(rankedResult.oldCups)
                    .append(" → ")
                    .append(rankedResult.newCups);

            if (rankedResult.rankedDown()) {
                text.append("\nРАНГ: ")
                        .append(rankedResult.newRankName());
            }

            result.setText(text.toString());
            result.setTextColor(Color.rgb(238, 105, 105));
            win.setEnabled(false);
            loss.setEnabled(false);
            exit.setText("ПРОДОВЖИТИ");
        };

        win.setOnClickListener(applyWin);
        loss.setOnClickListener(applyLoss);
        exit.setOnClickListener(v -> listener.onExit());

        return root;
    }
}
