package com.nexora.durakarena;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Cinematic matte backdrop for the V3 game UI. */
public final class ArenaV3BackdropDrawable extends Drawable {
    public enum Scene { SPLASH, LOGIN, LOBBY, INNER }

    private final Scene scene;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha = 255;

    public ArenaV3BackdropDrawable(Scene scene) {
        this.scene = scene == null ? Scene.INNER : scene;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 0 || b.height() <= 0) return;
        float w = b.width();
        float h = b.height();
        float cx = w * 0.5f;

        p.setAlpha(alpha);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{
                        Color.rgb(1, 7, 6),
                        Color.rgb(3, 17, 13),
                        Color.rgb(2, 10, 8),
                        Color.rgb(1, 4, 4)
                },
                new float[]{0f, .30f, .70f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(b, p);
        p.setShader(null);

        p.setShader(new RadialGradient(cx, h * .20f, Math.max(w, h) * .55f,
                new int[]{Color.argb(100, 24, 95, 62), Color.argb(24, 8, 37, 25), Color.TRANSPARENT},
                new float[]{0f, .48f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(b, p);
        p.setShader(null);

        drawSpotlights(canvas, w, h);

        if (scene == Scene.LOBBY || scene == Scene.INNER) {
            drawDealer(canvas, w, h);
            drawTable(canvas, w, h);
            drawCards(canvas, w, h);
        } else if (scene == Scene.LOGIN) {
            drawLoginCards(canvas, w, h);
        }

        drawGoldFrame(canvas, w, h);
        drawVignette(canvas, w, h);
    }

    private void drawSpotlights(Canvas c, float w, float h) {
        p.setStyle(Paint.Style.FILL);
        Path left = new Path();
        left.moveTo(w * .06f, 0);
        left.lineTo(w * .40f, h * .56f);
        left.lineTo(w * .18f, h * .56f);
        left.close();
        p.setShader(new LinearGradient(0, 0, w * .35f, h * .55f,
                new int[]{Color.argb(45, 239, 205, 128), Color.TRANSPARENT},
                null, Shader.TileMode.CLAMP));
        c.drawPath(left, p);

        Path right = new Path();
        right.moveTo(w * .94f, 0);
        right.lineTo(w * .60f, h * .56f);
        right.lineTo(w * .82f, h * .56f);
        right.close();
        p.setShader(new LinearGradient(w, 0, w * .65f, h * .55f,
                new int[]{Color.argb(38, 239, 205, 128), Color.TRANSPARENT},
                null, Shader.TileMode.CLAMP));
        c.drawPath(right, p);
        p.setShader(null);
    }

    private void drawDealer(Canvas c, float w, float h) {
        float y = h * .255f;
        p.setShader(new RadialGradient(w * .5f, y, w * .18f,
                new int[]{Color.argb(56, 230, 198, 124), Color.TRANSPARENT},
                null, Shader.TileMode.CLAMP));
        c.drawCircle(w * .5f, y, w * .20f, p);
        p.setShader(null);

        p.setColor(Color.rgb(10, 14, 13));
        c.drawCircle(w * .5f, y - h * .015f, w * .052f, p);

        Path body = new Path();
        body.moveTo(w * .43f, y + h * .035f);
        body.quadTo(w * .50f, y + h * .01f, w * .57f, y + h * .035f);
        body.lineTo(w * .61f, y + h * .145f);
        body.lineTo(w * .39f, y + h * .145f);
        body.close();
        p.setShader(new LinearGradient(0, y, 0, y + h * .16f,
                new int[]{Color.rgb(28, 31, 29), Color.rgb(6, 9, 8)},
                null, Shader.TileMode.CLAMP));
        c.drawPath(body, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f, w * .004f));
        p.setColor(Color.argb(135, 226, 189, 105));
        c.drawLine(w * .50f, y + h * .045f, w * .50f, y + h * .13f, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void drawTable(Canvas c, float w, float h) {
        RectF rail = new RectF(-w * .22f, h * .40f, w * 1.22f, h * .90f);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(w * .5f, h * .61f, w * .78f,
                new int[]{Color.rgb(18, 78, 52), Color.rgb(8, 42, 30), Color.rgb(2, 18, 13)},
                new float[]{0f, .58f, 1f}, Shader.TileMode.CLAMP));
        c.drawOval(rail, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(5f, w * .012f));
        p.setColor(Color.argb(175, 111, 77, 39));
        c.drawOval(rail, p);

        RectF gold = new RectF(rail.left + w * .035f, rail.top + h * .018f,
                rail.right - w * .035f, rail.bottom - h * .018f);
        p.setStrokeWidth(Math.max(1.5f, w * .004f));
        p.setColor(Color.argb(165, 221, 180, 93));
        c.drawOval(gold, p);

        p.setStrokeWidth(Math.max(1f, w * .002f));
        p.setColor(Color.argb(52, 238, 221, 172));
        RectF inner = new RectF(rail.left + w * .09f, rail.top + h * .055f,
                rail.right - w * .09f, rail.bottom - h * .055f);
        c.drawOval(inner, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void drawCards(Canvas c, float w, float h) {
        float cy = h * .56f;
        drawCard(c, w * .42f, cy, w * .115f, h * .105f, -8f, false);
        drawCard(c, w * .50f, cy - h * .012f, w * .115f, h * .105f, 2f, true);
        drawCard(c, w * .58f, cy, w * .115f, h * .105f, 10f, false);
    }

    private void drawLoginCards(Canvas c, float w, float h) {
        float cy = h * .31f;
        drawCard(c, w * .45f, cy, w * .13f, h * .12f, -10f, false);
        drawCard(c, w * .55f, cy, w * .13f, h * .12f, 10f, true);
    }

    private void drawCard(Canvas c, float cx, float cy, float cw, float ch, float degrees, boolean ace) {
        c.save();
        c.rotate(degrees, cx, cy);
        RectF r = new RectF(cx - cw / 2f, cy - ch / 2f, cx + cw / 2f, cy + ch / 2f);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(230, 225, 210));
        c.drawRoundRect(r, cw * .10f, cw * .10f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1.2f, cw * .025f));
        p.setColor(Color.rgb(184, 145, 69));
        c.drawRoundRect(r, cw * .10f, cw * .10f, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(ace ? Color.rgb(35, 40, 37) : Color.rgb(136, 28, 28));
        c.drawCircle(cx, cy, Math.min(cw, ch) * .11f, p);
        c.restore();
    }

    private void drawGoldFrame(Canvas c, float w, float h) {
        float inset = Math.max(12f, w * .032f);
        RectF r = new RectF(inset, inset, w - inset, h - inset);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f, w * .0023f));
        p.setColor(Color.argb(scene == Scene.SPLASH ? 105 : 72, 219, 174, 90));
        c.drawRoundRect(r, w * .04f, w * .04f, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void drawVignette(Canvas c, float w, float h) {
        p.setShader(new RadialGradient(w * .5f, h * .48f, Math.max(w, h) * .74f,
                new int[]{Color.TRANSPARENT, Color.argb(26, 0, 0, 0), Color.argb(185, 0, 0, 0)},
                new float[]{0f, .66f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);
    }

    @Override public void setAlpha(int alpha) { this.alpha = alpha; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter colorFilter) { p.setColorFilter(colorFilter); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
