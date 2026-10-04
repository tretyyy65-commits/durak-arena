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

/** Matte casino/card-room backdrop used by the premium UI. */
public final class ArenaBackdropDrawable extends Drawable {
    public enum Scene { SPLASH, LOGIN, LOBBY, INNER }

    private final Scene scene;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha = 255;

    public ArenaBackdropDrawable(Scene scene) {
        this.scene = scene == null ? Scene.INNER : scene;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.width() <= 0 || b.height() <= 0) return;

        float w = b.width();
        float h = b.height();
        float cx = b.exactCenterX();

        paint.setAlpha(alpha);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(
                0, 0, 0, h,
                new int[]{
                        Color.rgb(2, 9, 7),
                        Color.rgb(5, 24, 17),
                        Color.rgb(2, 11, 8),
                        Color.rgb(1, 4, 3)
                },
                new float[]{0f, 0.34f, 0.72f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(b, paint);
        paint.setShader(null);

        float glowY = scene == Scene.SPLASH ? h * 0.34f : h * 0.24f;
        paint.setShader(new RadialGradient(
                cx, glowY, Math.max(w, h) * 0.62f,
                new int[]{Color.argb(92, 29, 108, 71), Color.argb(18, 11, 47, 31), Color.TRANSPARENT},
                new float[]{0f, 0.48f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(b, paint);
        paint.setShader(null);

        if (scene == Scene.LOBBY || scene == Scene.INNER) {
            drawTable(canvas, w, h);
        }

        drawFrame(canvas, w, h);
        drawVignette(canvas, w, h);
    }

    private void drawTable(Canvas canvas, float w, float h) {
        float top = h * 0.38f;
        float bottom = h * 0.82f;
        RectF table = new RectF(-w * 0.18f, top, w * 1.18f, bottom);

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(
                w * 0.5f, h * 0.58f, w * 0.72f,
                new int[]{Color.rgb(16, 68, 47), Color.rgb(8, 39, 28), Color.rgb(3, 18, 13)},
                null, Shader.TileMode.CLAMP));
        canvas.drawOval(table, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, w * 0.004f));
        paint.setColor(Color.argb(150, 201, 154, 78));
        canvas.drawOval(table, paint);

        RectF inner = new RectF(table.left + w * 0.06f, table.top + h * 0.035f,
                table.right - w * 0.06f, table.bottom - h * 0.035f);
        paint.setStrokeWidth(Math.max(1f, w * 0.002f));
        paint.setColor(Color.argb(70, 241, 215, 155));
        canvas.drawOval(inner, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(34, 255, 255, 255));
        canvas.drawOval(new RectF(w * 0.22f, h * 0.50f, w * 0.78f, h * 0.67f), paint);
    }

    private void drawFrame(Canvas canvas, float w, float h) {
        float inset = Math.max(12f, w * 0.035f);
        RectF frame = new RectF(inset, inset, w - inset, h - inset);

        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, w * 0.0024f));
        paint.setColor(Color.argb(scene == Scene.LOGIN ? 105 : 70, 201, 154, 78));
        canvas.drawRoundRect(frame, w * 0.045f, w * 0.045f, paint);

        if (scene == Scene.SPLASH || scene == Scene.LOGIN) {
            paint.setColor(Color.argb(38, 241, 215, 155));
            Path p = new Path();
            p.moveTo(w * 0.14f, h * 0.12f);
            p.cubicTo(w * 0.34f, h * 0.08f, w * 0.66f, h * 0.08f, w * 0.86f, h * 0.12f);
            canvas.drawPath(p, paint);
        }
    }

    private void drawVignette(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(
                w * 0.5f, h * 0.46f, Math.max(w, h) * 0.72f,
                new int[]{Color.TRANSPARENT, Color.argb(35, 0, 0, 0), Color.argb(178, 0, 0, 0)},
                new float[]{0f, 0.68f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
    }

    @Override public void setAlpha(int alpha) { this.alpha = alpha; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter colorFilter) { paint.setColorFilter(colorFilter); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
