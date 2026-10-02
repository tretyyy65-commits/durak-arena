package com.nexora.durakarena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Landscape ranked table shell. Keeps the existing ranked result economy but presents it
 * as the new arena interface. The table theme upgrades automatically with the league.
 */
public final class RankedGameView {
    private RankedGameView() {}

    public interface Listener { void onExit(); }

    private static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    private static GradientDrawable box(Context c, int fill, int stroke, int width, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(c, radius));
        d.setStroke(dp(c, width), stroke);
        return d;
    }

    private static TextView text(Context c, String value, int size, boolean bold, int gravity, int color) {
        TextView v = new TextView(c);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(gravity);
        v.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return v;
    }

    public static View create(Context context, String playerName, int cups, Listener listener) {
        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Color.BLACK);

        final int[] currentCups = {Math.max(0, cups)};
        final boolean[] resultApplied = {false};

        ArenaSurface arena = new ArenaSurface(context, currentCups[0]);
        root.addView(arena, new FrameLayout.LayoutParams(-1, -1));

        TextView exit = text(context, "‹", 30, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        exit.setBackground(box(context, Color.argb(225, 5, 10, 13), UiKit.GOLD, 1, 10));
        exit.setClickable(true);
        exit.setOnClickListener(v -> listener.onExit());
        FrameLayout.LayoutParams exitLp = new FrameLayout.LayoutParams(dp(context, 48), dp(context, 48));
        exitLp.leftMargin = dp(context, 12);
        exitLp.topMargin = dp(context, 10);
        root.addView(exit, exitLp);

        TextView title = text(context,
                UiKit.arenaTitleForCups(currentCups[0]) + "  •  " + LeagueSystem.nameForCups(currentCups[0]),
                14, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        title.setBackground(box(context, Color.argb(215, 5, 10, 13),
                LeagueSystem.colorForCups(currentCups[0]), 1, 12));
        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(dp(context, 330), dp(context, 44));
        titleLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        titleLp.topMargin = dp(context, 10);
        root.addView(title, titleLp);

        LinearLayout opponents = new LinearLayout(context);
        opponents.setGravity(Gravity.CENTER);
        opponents.addView(playerTag(context, "Ігор", "🏆 1200"));
        opponents.addView(playerTag(context, "Олексій", "🏆 980"));
        opponents.addView(playerTag(context, "Дмитро", "🏆 1120"));
        opponents.addView(playerTag(context, "Сергій", "🏆 1050"));

        FrameLayout.LayoutParams opponentsLp = new FrameLayout.LayoutParams(-2, dp(context, 54));
        opponentsLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        opponentsLp.topMargin = dp(context, 58);
        root.addView(opponents, opponentsLp);

        TextView turn = text(context, "ВАШ ХІД\nПокладіть карту", 11, true, Gravity.CENTER, Color.WHITE);
        turn.setBackground(box(context, Color.argb(190, 4, 18, 17), UiKit.GOLD, 1, 10));
        FrameLayout.LayoutParams turnLp = new FrameLayout.LayoutParams(dp(context, 150), dp(context, 52));
        turnLp.gravity = Gravity.CENTER;
        turnLp.topMargin = dp(context, 10);
        root.addView(turn, turnLp);

        TextView hand = text(context,
                "8♥    K♠    10♦    A♠    Q♦    J♣",
                22, true, Gravity.CENTER, Color.WHITE);
        hand.setBackground(box(context, Color.argb(225, 9, 10, 11), UiKit.GOLD, 1, 14));
        FrameLayout.LayoutParams handLp = new FrameLayout.LayoutParams(dp(context, 470), dp(context, 64));
        handLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        handLp.bottomMargin = dp(context, 74);
        root.addView(hand, handLp);

        LinearLayout controls = new LinearLayout(context);
        controls.setGravity(Gravity.CENTER);

        TextView take = action(context, "ВЗЯТИ", Color.rgb(16, 71, 112));
        TextView beat = action(context, "БИТО", Color.rgb(120, 26, 22));
        TextView pass = action(context, "ПАС", Color.rgb(22, 102, 56));

        controls.addView(take, actionLp(context));
        controls.addView(beat, actionLp(context));
        controls.addView(pass, actionLp(context));

        FrameLayout.LayoutParams controlsLp = new FrameLayout.LayoutParams(dp(context, 420), dp(context, 58));
        controlsLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        controlsLp.bottomMargin = dp(context, 10);
        root.addView(controls, controlsLp);

        LinearLayout playerCard = new LinearLayout(context);
        playerCard.setGravity(Gravity.CENTER_VERTICAL);
        playerCard.setPadding(dp(context, 8), dp(context, 4), dp(context, 8), dp(context, 4));
        playerCard.setBackground(box(context, Color.argb(230, 5, 10, 13), UiKit.GOLD, 1, 12));
        ImageView rankIcon = UiKit.icon(context,
                LeagueSystem.assetForIndex(LeagueSystem.indexForCups(currentCups[0])),
                "Ранг");
        playerCard.addView(rankIcon, new LinearLayout.LayoutParams(dp(context, 44), dp(context, 44)));
        TextView playerText = text(context, "", 11, true, Gravity.CENTER_VERTICAL, UiKit.GOLD_LIGHT);
        playerCard.addView(playerText, new LinearLayout.LayoutParams(dp(context, 150), -1));

        FrameLayout.LayoutParams playerLp = new FrameLayout.LayoutParams(dp(context, 210), dp(context, 56));
        playerLp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        playerLp.leftMargin = dp(context, 14);
        playerLp.bottomMargin = dp(context, 12);
        root.addView(playerCard, playerLp);

        TextView menu = text(context, "⋮", 28, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        menu.setBackground(box(context, Color.argb(225, 5, 10, 13), UiKit.GOLD, 1, 10));
        FrameLayout.LayoutParams menuLp = new FrameLayout.LayoutParams(dp(context, 46), dp(context, 46));
        menuLp.gravity = Gravity.TOP | Gravity.RIGHT;
        menuLp.rightMargin = dp(context, 12);
        menuLp.topMargin = dp(context, 10);
        root.addView(menu, menuLp);

        LinearLayout resultPanel = new LinearLayout(context);
        resultPanel.setOrientation(LinearLayout.VERTICAL);
        resultPanel.setGravity(Gravity.CENTER);
        resultPanel.setPadding(dp(context, 10), dp(context, 8), dp(context, 10), dp(context, 8));
        resultPanel.setBackground(box(context, Color.argb(240, 4, 8, 11), UiKit.GOLD, 1, 12));
        resultPanel.setVisibility(View.GONE);

        TextView resultTitle = text(context, "ТЕСТ РЕЗУЛЬТАТУ МАТЧУ", 10, true, Gravity.CENTER, UiKit.MUTED);
        resultPanel.addView(resultTitle, new LinearLayout.LayoutParams(-1, dp(context, 24)));

        TextView resultText = text(context, "", 11, true, Gravity.CENTER, UiKit.TEXT);
        resultPanel.addView(resultText, new LinearLayout.LayoutParams(-1, dp(context, 48)));

        LinearLayout resultButtons = new LinearLayout(context);
        resultButtons.setGravity(Gravity.CENTER);
        TextView win = action(context, "ПЕРЕМОГА", Color.rgb(22, 98, 55));
        TextView loss = action(context, "ПОРАЗКА", Color.rgb(108, 30, 32));
        resultButtons.addView(win, actionLp(context));
        resultButtons.addView(loss, actionLp(context));
        resultPanel.addView(resultButtons, new LinearLayout.LayoutParams(-1, dp(context, 50)));

        FrameLayout.LayoutParams resultLp = new FrameLayout.LayoutParams(dp(context, 330), dp(context, 138));
        resultLp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        resultLp.rightMargin = dp(context, 12);
        root.addView(resultPanel, resultLp);

        menu.setClickable(true);
        menu.setOnClickListener(v -> resultPanel.setVisibility(
                resultPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
        ));

        Runnable refresh = () -> {
            int c = currentCups[0];
            playerText.setText(playerName + "\n🏆 " + c);
            title.setText(UiKit.arenaTitleForCups(c) + "  •  " + LeagueSystem.nameForCups(c));
            title.setBackground(box(context, Color.argb(215, 5, 10, 13),
                    LeagueSystem.colorForCups(c), 1, 12));
            arena.setCups(c);
            resultTitle.setText("ПЕРЕМОГА +" + RankedRatingSystem.winGainForCups(c)
                    + "   •   ПОРАЗКА -" + RankedRatingSystem.lossPenaltyForCups(c));
        };
        refresh.run();

        win.setOnClickListener(v -> {
            if (resultApplied[0]) return;
            resultApplied[0] = true;
            RankedRatingSystem.Result rr = RankedRatingSystem.apply(context, true);
            currentCups[0] = rr.newCups;
            refresh.run();
            String text = "ПЕРЕМОГА  +" + rr.delta + " 🏆\n" + rr.oldCups + " → " + rr.newCups;
            if (rr.rankedUp()) text += "\nНОВИЙ РАНГ: " + rr.newRankName();
            if (rr.winStreak >= 2) text += "\nСерія перемог: " + rr.winStreak;
            resultText.setText(text);
            resultText.setTextColor(Color.rgb(92, 231, 137));
            win.setEnabled(false);
            loss.setEnabled(false);
        });

        loss.setOnClickListener(v -> {
            if (resultApplied[0]) return;
            resultApplied[0] = true;
            RankedRatingSystem.Result rr = RankedRatingSystem.apply(context, false);
            currentCups[0] = rr.newCups;
            refresh.run();
            String text = "ПОРАЗКА  " + rr.delta + " 🏆\n" + rr.oldCups + " → " + rr.newCups;
            if (rr.rankedDown()) text += "\nРАНГ: " + rr.newRankName();
            resultText.setText(text);
            resultText.setTextColor(Color.rgb(238, 105, 105));
            win.setEnabled(false);
            loss.setEnabled(false);
        });

        take.setOnClickListener(v -> turn.setText("КАРТУ ВЗЯТО\nВаш хід"));
        beat.setOnClickListener(v -> turn.setText("БИТО\nНаступний раунд"));
        pass.setOnClickListener(v -> turn.setText("ПАС\nОчікування суперника"));

        return root;
    }

    private static LinearLayout playerTag(Context context, String name, String score) {
        LinearLayout tag = new LinearLayout(context);
        tag.setGravity(Gravity.CENTER_VERTICAL);
        tag.setPadding(dp(context, 6), dp(context, 2), dp(context, 6), dp(context, 2));
        tag.setBackground(box(context, Color.argb(205, 4, 11, 14), UiKit.GOLD, 1, 20));

        TextView avatar = text(context, "●", 18, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        tag.addView(avatar, new LinearLayout.LayoutParams(dp(context, 30), -1));

        TextView txt = text(context, name + "\n" + score, 9, true, Gravity.LEFT | Gravity.CENTER_VERTICAL, Color.WHITE);
        tag.addView(txt, new LinearLayout.LayoutParams(dp(context, 76), -1));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(context, 112), dp(context, 50));
        lp.setMargins(dp(context, 4), 0, dp(context, 4), 0);
        tag.setLayoutParams(lp);
        return tag;
    }

    private static TextView action(Context context, String title, int fill) {
        TextView b = text(context, title, 13, true, Gravity.CENTER, Color.WHITE);
        b.setBackground(box(context, fill, UiKit.GOLD, 1, 10));
        b.setClickable(true);
        return b;
    }

    private static LinearLayout.LayoutParams actionLp(Context context) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(context, 48), 1f);
        p.setMargins(dp(context, 4), 0, dp(context, 4), 0);
        return p;
    }

    private static final class ArenaSurface extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF oval = new RectF();
        private int cups;

        ArenaSurface(Context context, int cups) {
            super(context);
            this.cups = cups;
        }

        void setCups(int cups) {
            this.cups = cups;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            int family = LeagueSystem.familyForIndex(LeagueSystem.indexForCups(cups));
            int bgTop, bgBottom, felt, rail, glow;

            switch (family) {
                case 0:
                    bgTop = Color.rgb(30, 20, 13);
                    bgBottom = Color.rgb(6, 9, 9);
                    felt = Color.rgb(30, 73, 52);
                    rail = Color.rgb(111, 75, 42);
                    glow = Color.rgb(177, 113, 48);
                    break;
                case 1:
                    bgTop = Color.rgb(5, 44, 31);
                    bgBottom = Color.rgb(3, 10, 10);
                    felt = Color.rgb(15, 92, 66);
                    rail = Color.rgb(142, 105, 43);
                    glow = Color.rgb(44, 199, 106);
                    break;
                case 2:
                    bgTop = Color.rgb(7, 29, 58);
                    bgBottom = Color.rgb(3, 8, 15);
                    felt = Color.rgb(22, 63, 101);
                    rail = Color.rgb(154, 122, 58);
                    glow = Color.rgb(68, 142, 220);
                    break;
                case 3:
                    bgTop = Color.rgb(52, 16, 65);
                    bgBottom = Color.rgb(7, 4, 11);
                    felt = Color.rgb(72, 35, 89);
                    rail = Color.rgb(175, 132, 67);
                    glow = Color.rgb(170, 72, 226);
                    break;
                default:
                    bgTop = Color.rgb(72, 9, 12);
                    bgBottom = Color.rgb(10, 3, 4);
                    felt = Color.rgb(69, 21, 25);
                    rail = Color.rgb(211, 160, 72);
                    glow = Color.rgb(233, 70, 44);
                    break;
            }

            paint.setShader(new LinearGradient(0, 0, 0, h, bgTop, bgBottom, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            paint.setColor(Color.argb(115, 8, 8, 10));
            int pillars = 5 + family * 2;
            for (int i = 0; i < pillars; i++) {
                float x = (i + 0.5f) * w / pillars;
                canvas.drawRect(x - 7, 0, x + 7, h * 0.42f, paint);
            }

            paint.setShader(new RadialGradient(w * 0.5f, h * 0.50f, Math.min(w, h) * 0.62f,
                    Color.argb(90, Color.red(glow), Color.green(glow), Color.blue(glow)),
                    Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            float left = w * 0.12f;
            float right = w * 0.88f;
            float top = h * 0.20f;
            float bottom = h * 0.82f;

            oval.set(left - 22, top - 22, right + 22, bottom + 22);
            paint.setColor(Color.argb(180, 0, 0, 0));
            canvas.drawOval(oval, paint);

            oval.set(left - 12, top - 12, right + 12, bottom + 12);
            paint.setColor(rail);
            canvas.drawOval(oval, paint);

            oval.set(left - 5, top - 5, right + 5, bottom + 5);
            paint.setColor(Color.rgb(24, 19, 15));
            canvas.drawOval(oval, paint);

            oval.set(left, top, right, bottom);
            paint.setColor(felt);
            canvas.drawOval(oval, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2, dp(getContext(), 2)));
            paint.setColor(Color.argb(170, 222, 182, 104));
            oval.set(left + 13, top + 13, right - 13, bottom - 13);
            canvas.drawOval(oval, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.argb(33, 255, 255, 255));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(h * 0.18f);
            canvas.drawText("♠", w * 0.5f, h * 0.58f, paint);

            drawDeck(canvas, w * 0.24f, h * 0.44f, rail);
            drawDeck(canvas, w * 0.76f, h * 0.44f, rail);

            float cw = h * 0.105f;
            float ch = h * 0.145f;
            paint.setColor(Color.rgb(235, 231, 220));
            RectF card = new RectF(w * 0.5f - cw / 2, h * 0.43f - ch / 2,
                    w * 0.5f + cw / 2, h * 0.43f + ch / 2);
            canvas.drawRoundRect(card, 8, 8, paint);
            paint.setColor(Color.rgb(20, 20, 22));
            paint.setTextSize(ch * 0.34f);
            canvas.drawText("A♠", w * 0.5f, h * 0.45f, paint);

            int ornaments = 8 + family * 4;
            for (int i = 0; i < ornaments; i++) {
                double a = (Math.PI * 2 * i) / ornaments;
                float cx = w * 0.5f + (float)Math.cos(a) * (right - left) * 0.49f;
                float cy = h * 0.51f + (float)Math.sin(a) * (bottom - top) * 0.52f;
                paint.setColor(Color.argb(210, Color.red(glow), Color.green(glow), Color.blue(glow)));
                canvas.drawCircle(cx, cy, 2.5f + family * 0.4f, paint);
            }
        }

        private void drawDeck(Canvas canvas, float cx, float cy, int border) {
            float w = dp(getContext(), 52);
            float h = dp(getContext(), 72);
            RectF r = new RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2);
            paint.setColor(Color.rgb(20, 24, 30));
            canvas.drawRoundRect(r, 8, 8, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(getContext(), 2));
            paint.setColor(border);
            canvas.drawRoundRect(r, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(80, 255, 255, 255));
            paint.setTextSize(dp(getContext(), 22));
            canvas.drawText("♠", cx, cy + dp(getContext(), 8), paint);
        }
    }
}
