package com.nexora.durakarena;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Code-drawn icon system for the V2 interface.
 *
 * The redesign intentionally does not use the old photo-like PNG button icons.
 * Every UI symbol and league badge is rendered as a lightweight matte vector
 * directly on Canvas so the whole interface keeps one consistent visual style.
 */
public final class GameIconDrawable extends Drawable {
    private static final int GOLD = Color.rgb(196, 146, 60);
    private static final int GOLD_LIGHT = Color.rgb(238, 207, 140);
    private static final int DARK = Color.rgb(8, 13, 16);
    private static final int DARK_2 = Color.rgb(15, 21, 24);

    private final String name;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha = 255;

    public GameIconDrawable(String name) {
        this.name = name == null ? "" : name;
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 0 || b.height() <= 0) return;

        if (name.startsWith("rank_")) {
            drawRank(canvas, b);
            return;
        }

        float cx = b.exactCenterX();
        float cy = b.exactCenterY();
        float s = Math.min(b.width(), b.height());
        float sw = Math.max(1.5f, s * 0.065f);

        paint.setAlpha(alpha);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(sw);
        paint.setColor(GOLD_LIGHT);

        switch (name) {
            case "nexora_profile":
                drawProfile(canvas, cx, cy, s);
                break;
            case "nexora_coin":
                drawCoin(canvas, cx, cy, s);
                break;
            case "nexora_crystal":
                drawDiamond(canvas, cx, cy, s, GOLD_LIGHT);
                break;
            case "nexora_events":
                drawGift(canvas, cx, cy, s);
                break;
            case "nexora_friends":
                drawFriends(canvas, cx, cy, s);
                break;
            case "nexora_achievement":
                drawMedal(canvas, cx, cy, s);
                break;
            case "nexora_clan":
                drawShield(canvas, cx, cy, s, GOLD_LIGHT);
                drawClub(canvas, cx, cy, s * 0.34f, GOLD_LIGHT);
                break;
            case "nexora_shop":
                drawShop(canvas, cx, cy, s);
                break;
            case "nexora_inventory":
                drawInventory(canvas, cx, cy, s);
                break;
            case "nexora_season":
                drawCrown(canvas, cx, cy, s, GOLD_LIGHT);
                break;
            case "nexora_trophy":
            case "nexora_leaders":
                drawTrophy(canvas, cx, cy, s);
                break;
            case "nexora_lock":
                drawLock(canvas, cx, cy, s);
                break;
            case "nexora_cards":
                drawCards(canvas, cx, cy, s);
                break;
            case "nexora_play":
            case "nexora_arena":
                drawSpade(canvas, cx, cy, s * 0.70f, GOLD_LIGHT);
                break;
            case "nexora_settings":
                drawGear(canvas, cx, cy, s);
                break;
            case "nexora_region":
                drawPin(canvas, cx, cy, s);
                break;
            case "nexora_history":
                drawClock(canvas, cx, cy, s);
                break;
            case "nexora_support":
            case "nexora_help":
                drawHeadset(canvas, cx, cy, s);
                break;
            case "nexora_info":
                drawInfo(canvas, cx, cy, s);
                break;
            case "nexora_back":
                drawChevron(canvas, cx, cy, s, false);
                break;
            case "nexora_forward":
                drawChevron(canvas, cx, cy, s, true);
                break;
            case "nexora_sound":
            case "nexora_mute":
                drawSpeaker(canvas, cx, cy, s, name.endsWith("mute"));
                break;
            default:
                drawSpade(canvas, cx, cy, s * 0.68f, GOLD_LIGHT);
                break;
        }
    }

    private void drawRank(Canvas canvas, Rect bounds) {
        String lower = name.toLowerCase();
        int family;
        if (lower.contains("novice")) family = 0;
        else if (lower.contains("pro_")) family = 1;
        else if (lower.contains("master")) family = 2;
        else if (lower.contains("elite")) family = 3;
        else family = 4;

        int tier = lower.endsWith("_3") ? 3 : lower.endsWith("_2") ? 2 : 1;
        int accent = rankColor(family);

        float cx = bounds.exactCenterX();
        float cy = bounds.exactCenterY();
        float s = Math.min(bounds.width(), bounds.height());
        float r = s * 0.43f;

        paint.setAlpha(alpha);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(6, 11, 14));
        canvas.drawCircle(cx, cy, r, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, s * 0.055f));
        paint.setColor(accent);
        canvas.drawCircle(cx, cy, r, paint);

        paint.setStrokeWidth(Math.max(1f, s * 0.022f));
        paint.setColor(withAlpha(GOLD_LIGHT, 205));
        canvas.drawCircle(cx, cy, r * 0.78f, paint);

        // Tier ornament: each stage grows visually instead of reusing the same medal.
        if (tier >= 2) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(accent);
            float wing = s * 0.10f;
            canvas.drawCircle(cx - r * 0.91f, cy, wing, paint);
            canvas.drawCircle(cx + r * 0.91f, cy, wing, paint);
        }
        if (tier >= 3) {
            Path crown = new Path();
            crown.moveTo(cx - s * 0.20f, cy - r * 0.93f);
            crown.lineTo(cx - s * 0.10f, cy - r * 1.20f);
            crown.lineTo(cx, cy - r * 0.96f);
            crown.lineTo(cx + s * 0.10f, cy - r * 1.20f);
            crown.lineTo(cx + s * 0.20f, cy - r * 0.93f);
            crown.close();
            paint.setColor(GOLD_LIGHT);
            canvas.drawPath(crown, paint);
        }

        // Every family has a different symbol, and tiers change the symbol detail.
        switch (family) {
            case 0:
                if (tier == 1) drawSpade(canvas, cx, cy + s * 0.02f, s * 0.42f, accent);
                else if (tier == 2) drawClub(canvas, cx, cy + s * 0.02f, s * 0.40f, accent);
                else drawStar(canvas, cx, cy, s * 0.22f, accent, 5);
                break;
            case 1:
                if (tier == 1) drawDiamond(canvas, cx, cy, s * 0.42f, accent);
                else if (tier == 2) drawLaurel(canvas, cx, cy, s, accent);
                else drawCrown(canvas, cx, cy + s * 0.03f, s * 0.52f, accent);
                break;
            case 2:
                if (tier == 1) drawCards(canvas, cx, cy, s * 0.52f);
                else if (tier == 2) drawStar(canvas, cx, cy, s * 0.23f, accent, 6);
                else drawTrophy(canvas, cx, cy, s * 0.56f);
                break;
            case 3:
                if (tier == 1) drawDiamond(canvas, cx, cy, s * 0.50f, accent);
                else if (tier == 2) drawStar(canvas, cx, cy, s * 0.23f, accent, 8);
                else drawCrown(canvas, cx, cy, s * 0.56f, accent);
                break;
            default:
                if (tier == 1) drawFlame(canvas, cx, cy, s * 0.50f, accent);
                else if (tier == 2) {
                    drawFlame(canvas, cx, cy + s * 0.03f, s * 0.46f, accent);
                    drawCrown(canvas, cx, cy - s * 0.15f, s * 0.30f, GOLD_LIGHT);
                } else {
                    drawCrown(canvas, cx, cy - s * 0.03f, s * 0.58f, GOLD_LIGHT);
                    drawStar(canvas, cx, cy + s * 0.18f, s * 0.10f, accent, 8);
                }
                break;
        }

        // Small tier pips make I / II / III readable without text baked into an image.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(GOLD_LIGHT);
        float pipR = Math.max(1.5f, s * 0.027f);
        float y = cy + r * 0.66f;
        float gap = s * 0.085f;
        for (int i = 0; i < tier; i++) {
            float x = cx + (i - (tier - 1) / 2f) * gap;
            canvas.drawCircle(x, y, pipR, paint);
        }
    }

    private int rankColor(int family) {
        switch (family) {
            case 0: return Color.rgb(187, 118, 57);
            case 1: return Color.rgb(56, 170, 104);
            case 2: return Color.rgb(64, 132, 196);
            case 3: return Color.rgb(151, 79, 190);
            default: return Color.rgb(176, 48, 52);
        }
    }

    private void drawProfile(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(GOLD_LIGHT);
        paint.setStrokeWidth(s * 0.06f);
        c.drawCircle(cx, cy - s * 0.14f, s * 0.13f, paint);
        RectF body = new RectF(cx - s * 0.23f, cy + s * 0.02f, cx + s * 0.23f, cy + s * 0.31f);
        c.drawArc(body, 200, 140, false, paint);
        drawShield(c, cx, cy, s, GOLD);
    }

    private void drawCoin(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(GOLD);
        c.drawCircle(cx, cy, s * 0.33f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.045f);
        paint.setColor(GOLD_LIGHT);
        c.drawCircle(cx, cy, s * 0.25f, paint);
        drawSpade(c, cx, cy, s * 0.28f, DARK);
    }

    private void drawGift(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.06f);
        paint.setColor(GOLD_LIGHT);
        RectF box = new RectF(cx - s * 0.28f, cy - s * 0.05f, cx + s * 0.28f, cy + s * 0.28f);
        c.drawRoundRect(box, s * 0.04f, s * 0.04f, paint);
        c.drawLine(cx, cy - s * 0.05f, cx, cy + s * 0.28f, paint);
        c.drawLine(cx - s * 0.31f, cy - s * 0.07f, cx + s * 0.31f, cy - s * 0.07f, paint);
        c.drawCircle(cx - s * 0.08f, cy - s * 0.19f, s * 0.09f, paint);
        c.drawCircle(cx + s * 0.08f, cy - s * 0.19f, s * 0.09f, paint);
    }

    private void drawFriends(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        c.drawCircle(cx, cy - s * 0.15f, s * 0.11f, paint);
        c.drawCircle(cx - s * 0.23f, cy - s * 0.08f, s * 0.09f, paint);
        c.drawCircle(cx + s * 0.23f, cy - s * 0.08f, s * 0.09f, paint);
        c.drawArc(new RectF(cx - s * 0.20f, cy, cx + s * 0.20f, cy + s * 0.32f), 200, 140, false, paint);
        c.drawArc(new RectF(cx - s * 0.39f, cy + s * 0.03f, cx - s * 0.08f, cy + s * 0.29f), 210, 120, false, paint);
        c.drawArc(new RectF(cx + s * 0.08f, cy + s * 0.03f, cx + s * 0.39f, cy + s * 0.29f), 210, 120, false, paint);
    }

    private void drawMedal(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.06f);
        paint.setColor(GOLD_LIGHT);
        c.drawCircle(cx, cy + s * 0.02f, s * 0.22f, paint);
        c.drawLine(cx - s * 0.10f, cy - s * 0.20f, cx - s * 0.20f, cy - s * 0.38f, paint);
        c.drawLine(cx + s * 0.10f, cy - s * 0.20f, cx + s * 0.20f, cy - s * 0.38f, paint);
        drawStar(c, cx, cy + s * 0.02f, s * 0.11f, GOLD, 5);
    }

    private void drawShop(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        RectF bag = new RectF(cx - s * 0.25f, cy - s * 0.08f, cx + s * 0.25f, cy + s * 0.30f);
        c.drawRoundRect(bag, s * 0.05f, s * 0.05f, paint);
        c.drawArc(new RectF(cx - s * 0.16f, cy - s * 0.25f, cx + s * 0.16f, cy + s * 0.03f), 200, 140, false, paint);
    }

    private void drawInventory(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        RectF chest = new RectF(cx - s * 0.31f, cy - s * 0.13f, cx + s * 0.31f, cy + s * 0.27f);
        c.drawRoundRect(chest, s * 0.06f, s * 0.06f, paint);
        c.drawLine(cx - s * 0.31f, cy + s * 0.01f, cx + s * 0.31f, cy + s * 0.01f, paint);
        c.drawRect(cx - s * 0.055f, cy - s * 0.04f, cx + s * 0.055f, cy + s * 0.10f, paint);
    }

    private void drawTrophy(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        RectF cup = new RectF(cx - s * 0.20f, cy - s * 0.28f, cx + s * 0.20f, cy + s * 0.09f);
        c.drawArc(cup, 0, 180, false, paint);
        c.drawLine(cx, cy + s * 0.09f, cx, cy + s * 0.27f, paint);
        c.drawLine(cx - s * 0.14f, cy + s * 0.27f, cx + s * 0.14f, cy + s * 0.27f, paint);
        c.drawArc(new RectF(cx - s * 0.36f, cy - s * 0.22f, cx - s * 0.10f, cy + s * 0.04f), 90, 170, false, paint);
        c.drawArc(new RectF(cx + s * 0.10f, cy - s * 0.22f, cx + s * 0.36f, cy + s * 0.04f), -80, 170, false, paint);
    }

    private void drawLock(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.06f);
        paint.setColor(GOLD_LIGHT);
        RectF body = new RectF(cx - s * 0.23f, cy - s * 0.03f, cx + s * 0.23f, cy + s * 0.29f);
        c.drawRoundRect(body, s * 0.05f, s * 0.05f, paint);
        c.drawArc(new RectF(cx - s * 0.17f, cy - s * 0.28f, cx + s * 0.17f, cy + s * 0.08f), 195, 150, false, paint);
        c.drawCircle(cx, cy + s * 0.10f, s * 0.035f, paint);
    }

    private void drawCards(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.05f);
        paint.setColor(GOLD_LIGHT);
        RectF back = new RectF(cx - s * 0.30f, cy - s * 0.27f, cx + s * 0.08f, cy + s * 0.27f);
        c.save();
        c.rotate(-12, cx, cy);
        c.drawRoundRect(back, s * 0.04f, s * 0.04f, paint);
        c.restore();
        RectF front = new RectF(cx - s * 0.07f, cy - s * 0.29f, cx + s * 0.30f, cy + s * 0.27f);
        c.save();
        c.rotate(9, cx, cy);
        c.drawRoundRect(front, s * 0.04f, s * 0.04f, paint);
        c.restore();
    }

    private void drawGear(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        c.drawCircle(cx, cy, s * 0.19f, paint);
        c.drawCircle(cx, cy, s * 0.07f, paint);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            float x1 = cx + (float) Math.cos(a) * s * 0.24f;
            float y1 = cy + (float) Math.sin(a) * s * 0.24f;
            float x2 = cx + (float) Math.cos(a) * s * 0.34f;
            float y2 = cy + (float) Math.sin(a) * s * 0.34f;
            c.drawLine(x1, y1, x2, y2, paint);
        }
    }

    private void drawPin(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        Path p = new Path();
        p.moveTo(cx, cy + s * 0.32f);
        p.cubicTo(cx - s * 0.35f, cy - s * 0.03f, cx - s * 0.25f, cy - s * 0.33f, cx, cy - s * 0.33f);
        p.cubicTo(cx + s * 0.25f, cy - s * 0.33f, cx + s * 0.35f, cy - s * 0.03f, cx, cy + s * 0.32f);
        c.drawPath(p, paint);
        c.drawCircle(cx, cy - s * 0.09f, s * 0.08f, paint);
    }

    private void drawClock(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        c.drawCircle(cx, cy, s * 0.30f, paint);
        c.drawLine(cx, cy, cx, cy - s * 0.16f, paint);
        c.drawLine(cx, cy, cx + s * 0.13f, cy + s * 0.08f, paint);
    }

    private void drawHeadset(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.06f);
        paint.setColor(GOLD_LIGHT);
        c.drawArc(new RectF(cx - s * 0.30f, cy - s * 0.30f, cx + s * 0.30f, cy + s * 0.27f), 195, 150, false, paint);
        c.drawLine(cx - s * 0.30f, cy, cx - s * 0.30f, cy + s * 0.20f, paint);
        c.drawLine(cx + s * 0.30f, cy, cx + s * 0.30f, cy + s * 0.20f, paint);
        c.drawLine(cx + s * 0.30f, cy + s * 0.20f, cx + s * 0.13f, cy + s * 0.25f, paint);
    }

    private void drawInfo(Canvas c, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        c.drawCircle(cx, cy, s * 0.30f, paint);
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(cx, cy - s * 0.13f, s * 0.035f, paint);
        c.drawRoundRect(new RectF(cx - s * 0.025f, cy - s * 0.03f, cx + s * 0.025f, cy + s * 0.18f), s * 0.02f, s * 0.02f, paint);
    }

    private void drawChevron(Canvas c, float cx, float cy, float s, boolean right) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.085f);
        paint.setColor(GOLD_LIGHT);
        Path p = new Path();
        float sign = right ? 1f : -1f;
        p.moveTo(cx - sign * s * 0.12f, cy - s * 0.22f);
        p.lineTo(cx + sign * s * 0.11f, cy);
        p.lineTo(cx - sign * s * 0.12f, cy + s * 0.22f);
        c.drawPath(p, paint);
    }

    private void drawSpeaker(Canvas c, float cx, float cy, float s, boolean mute) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.055f);
        paint.setColor(GOLD_LIGHT);
        Path p = new Path();
        p.moveTo(cx - s * 0.28f, cy - s * 0.10f);
        p.lineTo(cx - s * 0.10f, cy - s * 0.10f);
        p.lineTo(cx + s * 0.08f, cy - s * 0.25f);
        p.lineTo(cx + s * 0.08f, cy + s * 0.25f);
        p.lineTo(cx - s * 0.10f, cy + s * 0.10f);
        p.lineTo(cx - s * 0.28f, cy + s * 0.10f);
        p.close();
        c.drawPath(p, paint);
        if (mute) {
            c.drawLine(cx + s * 0.18f, cy - s * 0.17f, cx + s * 0.35f, cy + s * 0.17f, paint);
            c.drawLine(cx + s * 0.35f, cy - s * 0.17f, cx + s * 0.18f, cy + s * 0.17f, paint);
        } else {
            c.drawArc(new RectF(cx - s * 0.02f, cy - s * 0.22f, cx + s * 0.28f, cy + s * 0.22f), -60, 120, false, paint);
        }
    }

    private void drawShield(Canvas c, float cx, float cy, float s, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.045f);
        paint.setColor(color);
        Path p = new Path();
        p.moveTo(cx, cy - s * 0.35f);
        p.lineTo(cx + s * 0.28f, cy - s * 0.23f);
        p.lineTo(cx + s * 0.23f, cy + s * 0.13f);
        p.quadTo(cx, cy + s * 0.37f, cx - s * 0.23f, cy + s * 0.13f);
        p.lineTo(cx - s * 0.28f, cy - s * 0.23f);
        p.close();
        c.drawPath(p, paint);
    }

    private void drawSpade(Canvas c, float cx, float cy, float size, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        Path p = new Path();
        p.moveTo(cx, cy - size * 0.52f);
        p.cubicTo(cx - size * 0.10f, cy - size * 0.28f, cx - size * 0.42f, cy - size * 0.05f, cx - size * 0.30f, cy + size * 0.18f);
        p.cubicTo(cx - size * 0.20f, cy + size * 0.36f, cx - size * 0.04f, cy + size * 0.24f, cx, cy + size * 0.13f);
        p.cubicTo(cx + size * 0.04f, cy + size * 0.24f, cx + size * 0.20f, cy + size * 0.36f, cx + size * 0.30f, cy + size * 0.18f);
        p.cubicTo(cx + size * 0.42f, cy - size * 0.05f, cx + size * 0.10f, cy - size * 0.28f, cx, cy - size * 0.52f);
        p.close();
        c.drawPath(p, paint);
        c.drawRect(cx - size * 0.045f, cy + size * 0.10f, cx + size * 0.045f, cy + size * 0.43f, paint);
        c.drawRect(cx - size * 0.14f, cy + size * 0.39f, cx + size * 0.14f, cy + size * 0.46f, paint);
    }

    private void drawClub(Canvas c, float cx, float cy, float size, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        float r = size * 0.15f;
        c.drawCircle(cx, cy - size * 0.15f, r, paint);
        c.drawCircle(cx - size * 0.15f, cy + size * 0.02f, r, paint);
        c.drawCircle(cx + size * 0.15f, cy + size * 0.02f, r, paint);
        c.drawRect(cx - size * 0.045f, cy + size * 0.04f, cx + size * 0.045f, cy + size * 0.36f, paint);
        c.drawRect(cx - size * 0.13f, cy + size * 0.31f, cx + size * 0.13f, cy + size * 0.38f, paint);
    }

    private void drawDiamond(Canvas c, float cx, float cy, float size, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        Path p = new Path();
        p.moveTo(cx, cy - size * 0.42f);
        p.lineTo(cx + size * 0.31f, cy);
        p.lineTo(cx, cy + size * 0.42f);
        p.lineTo(cx - size * 0.31f, cy);
        p.close();
        c.drawPath(p, paint);
    }

    private void drawCrown(Canvas c, float cx, float cy, float size, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        Path p = new Path();
        p.moveTo(cx - size * 0.40f, cy + size * 0.20f);
        p.lineTo(cx - size * 0.32f, cy - size * 0.24f);
        p.lineTo(cx - size * 0.10f, cy - size * 0.03f);
        p.lineTo(cx, cy - size * 0.38f);
        p.lineTo(cx + size * 0.10f, cy - size * 0.03f);
        p.lineTo(cx + size * 0.32f, cy - size * 0.24f);
        p.lineTo(cx + size * 0.40f, cy + size * 0.20f);
        p.close();
        c.drawPath(p, paint);
        c.drawRect(cx - size * 0.38f, cy + size * 0.16f, cx + size * 0.38f, cy + size * 0.28f, paint);
    }

    private void drawLaurel(Canvas c, float cx, float cy, float s, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.035f);
        paint.setColor(color);
        c.drawArc(new RectF(cx - s * 0.30f, cy - s * 0.28f, cx + s * 0.04f, cy + s * 0.30f), 110, 140, false, paint);
        c.drawArc(new RectF(cx - s * 0.04f, cy - s * 0.28f, cx + s * 0.30f, cy + s * 0.30f), -70, 140, false, paint);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 4; i++) {
            float y = cy - s * 0.18f + i * s * 0.12f;
            c.drawOval(new RectF(cx - s * 0.28f, y, cx - s * 0.17f, y + s * 0.07f), paint);
            c.drawOval(new RectF(cx + s * 0.17f, y, cx + s * 0.28f, y + s * 0.07f), paint);
        }
    }

    private void drawFlame(Canvas c, float cx, float cy, float size, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        Path p = new Path();
        p.moveTo(cx, cy - size * 0.48f);
        p.cubicTo(cx + size * 0.20f, cy - size * 0.20f, cx + size * 0.30f, cy + size * 0.02f, cx + size * 0.18f, cy + size * 0.28f);
        p.cubicTo(cx + size * 0.08f, cy + size * 0.45f, cx - size * 0.13f, cy + size * 0.45f, cx - size * 0.23f, cy + size * 0.22f);
        p.cubicTo(cx - size * 0.34f, cy - size * 0.02f, cx - size * 0.13f, cy - size * 0.18f, cx, cy - size * 0.48f);
        p.close();
        c.drawPath(p, paint);
    }

    private void drawStar(Canvas c, float cx, float cy, float radius, int color, int points) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        Path p = new Path();
        double step = Math.PI / points;
        for (int i = 0; i < points * 2; i++) {
            double a = -Math.PI / 2 + i * step;
            float r = (i % 2 == 0) ? radius : radius * 0.46f;
            float x = cx + (float) Math.cos(a) * r;
            float y = cy + (float) Math.sin(a) * r;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.close();
        c.drawPath(p, paint);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
