package com.nexora.durakarena;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Single-style matte gold game icons for the V3 interface. */
public final class ArenaV3IconDrawable extends Drawable {
    private final String type;
    private final boolean active;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha = 255;

    public ArenaV3IconDrawable(String type, boolean active) {
        this.type = type == null ? "cards" : type;
        this.active = active;
    }

    @Override public void draw(Canvas c) {
        Rect b = getBounds();
        float w = b.width();
        float h = b.height();
        float cx = b.exactCenterX();
        float cy = b.exactCenterY();
        float s = Math.min(w, h);

        p.setAlpha(alpha);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{active ? Color.rgb(244, 216, 148) : Color.rgb(221, 185, 105),
                        active ? Color.rgb(186, 133, 52) : Color.rgb(142, 103, 54)},
                null, Shader.TileMode.CLAMP));

        if ("home".equals(type)) drawHome(c, cx, cy, s);
        else if ("trophy".equals(type)) drawTrophy(c, cx, cy, s);
        else if ("profile".equals(type)) drawProfile(c, cx, cy, s);
        else if ("coin".equals(type)) drawCoin(c, cx, cy, s, false);
        else if ("crystal".equals(type)) drawCoin(c, cx, cy, s, true);
        else if ("medal".equals(type)) drawMedal(c, cx, cy, s);
        else if ("shop".equals(type)) drawShop(c, cx, cy, s);
        else if ("back".equals(type)) drawBack(c, cx, cy, s);
        else if ("friends".equals(type)) drawFriends(c, cx, cy, s);
        else if ("clan".equals(type)) drawClan(c, cx, cy, s);
        else if ("play".equals(type)) drawPlay(c, cx, cy, s);
        else drawCards(c, cx, cy, s);

        p.setShader(null);
    }

    private void drawCards(Canvas c, float cx, float cy, float s) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(s * .075f);
        p.setStrokeCap(Paint.Cap.ROUND);
        RectF a = new RectF(cx - s * .31f, cy - s * .27f, cx + s * .05f, cy + s * .27f);
        RectF d = new RectF(cx - s * .04f, cy - s * .30f, cx + s * .31f, cy + s * .25f);
        c.save(); c.rotate(-8f, a.centerX(), a.centerY()); c.drawRoundRect(a, s*.05f, s*.05f, p); c.restore();
        c.save(); c.rotate(8f, d.centerX(), d.centerY()); c.drawRoundRect(d, s*.05f, s*.05f, p); c.restore();
        p.setStyle(Paint.Style.FILL);
    }

    private void drawHome(Canvas c, float cx, float cy, float s) {
        Path path = new Path();
        path.moveTo(cx, cy - s*.31f);
        path.lineTo(cx + s*.33f, cy - s*.02f);
        path.lineTo(cx + s*.22f, cy - s*.02f);
        path.lineTo(cx + s*.22f, cy + s*.28f);
        path.lineTo(cx - s*.22f, cy + s*.28f);
        path.lineTo(cx - s*.22f, cy - s*.02f);
        path.lineTo(cx - s*.33f, cy - s*.02f);
        path.close();
        c.drawPath(path, p);
    }

    private void drawTrophy(Canvas c, float cx, float cy, float s) {
        RectF cup = new RectF(cx-s*.20f, cy-s*.28f, cx+s*.20f, cy+s*.05f);
        c.drawRoundRect(cup, s*.05f, s*.05f, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.08f);
        c.drawArc(new RectF(cx-s*.37f,cy-s*.22f,cx-s*.04f,cy+s*.12f),90,180,false,p);
        c.drawArc(new RectF(cx+s*.04f,cy-s*.22f,cx+s*.37f,cy+s*.12f),-90,180,false,p);
        p.setStyle(Paint.Style.FILL);
        c.drawRect(cx-s*.05f,cy+s*.02f,cx+s*.05f,cy+s*.23f,p);
        c.drawRoundRect(new RectF(cx-s*.21f,cy+s*.20f,cx+s*.21f,cy+s*.30f),s*.04f,s*.04f,p);
    }

    private void drawProfile(Canvas c, float cx, float cy, float s) {
        c.drawCircle(cx, cy-s*.18f, s*.16f, p);
        c.drawOval(new RectF(cx-s*.29f,cy+s*.01f,cx+s*.29f,cy+s*.31f),p);
    }

    private void drawCoin(Canvas c, float cx, float cy, float s, boolean diamond) {
        if (diamond) {
            Path d = new Path();
            d.moveTo(cx,cy-s*.31f); d.lineTo(cx+s*.30f,cy-s*.05f); d.lineTo(cx,cy+s*.31f); d.lineTo(cx-s*.30f,cy-s*.05f); d.close();
            c.drawPath(d,p);
        } else {
            c.drawCircle(cx,cy,s*.29f,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.06f); p.setColor(Color.argb(140,30,20,8)); c.drawCircle(cx,cy,s*.18f,p); p.setStyle(Paint.Style.FILL);
        }
    }

    private void drawMedal(Canvas c, float cx, float cy, float s) {
        Path l = new Path(); l.moveTo(cx-s*.20f,cy-s*.32f); l.lineTo(cx-s*.03f,cy-s*.03f); l.lineTo(cx-s*.13f,cy+s*.05f); l.lineTo(cx-s*.31f,cy-s*.24f); l.close(); c.drawPath(l,p);
        Path r = new Path(); r.moveTo(cx+s*.20f,cy-s*.32f); r.lineTo(cx+s*.03f,cy-s*.03f); r.lineTo(cx+s*.13f,cy+s*.05f); r.lineTo(cx+s*.31f,cy-s*.24f); r.close(); c.drawPath(r,p);
        c.drawCircle(cx,cy+s*.13f,s*.23f,p);
    }

    private void drawShop(Canvas c, float cx, float cy, float s) {
        c.drawRoundRect(new RectF(cx-s*.27f,cy-s*.06f,cx+s*.27f,cy+s*.29f),s*.05f,s*.05f,p);
        Path roof = new Path(); roof.moveTo(cx-s*.34f,cy-s*.06f); roof.lineTo(cx-s*.25f,cy-s*.29f); roof.lineTo(cx+s*.25f,cy-s*.29f); roof.lineTo(cx+s*.34f,cy-s*.06f); roof.close(); c.drawPath(roof,p);
    }

    private void drawBack(Canvas c, float cx, float cy, float s) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.09f); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
        Path a = new Path(); a.moveTo(cx+s*.20f,cy-s*.28f); a.lineTo(cx-s*.16f,cy); a.lineTo(cx+s*.20f,cy+s*.28f); c.drawPath(a,p); p.setStyle(Paint.Style.FILL);
    }

    private void drawFriends(Canvas c, float cx, float cy, float s) {
        c.drawCircle(cx-s*.16f,cy-s*.14f,s*.12f,p); c.drawCircle(cx+s*.16f,cy-s*.14f,s*.12f,p);
        c.drawOval(new RectF(cx-s*.35f,cy+s*.01f,cx-s*.01f,cy+s*.28f),p); c.drawOval(new RectF(cx+s*.01f,cy+s*.01f,cx+s*.35f,cy+s*.28f),p);
    }

    private void drawClan(Canvas c, float cx, float cy, float s) {
        Path shield = new Path(); shield.moveTo(cx,cy-s*.34f); shield.lineTo(cx+s*.30f,cy-s*.21f); shield.lineTo(cx+s*.23f,cy+s*.18f); shield.lineTo(cx,cy+s*.34f); shield.lineTo(cx-s*.23f,cy+s*.18f); shield.lineTo(cx-s*.30f,cy-s*.21f); shield.close(); c.drawPath(shield,p);
    }

    private void drawPlay(Canvas c, float cx, float cy, float s) {
        Path tri = new Path(); tri.moveTo(cx-s*.18f,cy-s*.28f); tri.lineTo(cx+s*.31f,cy); tri.lineTo(cx-s*.18f,cy+s*.28f); tri.close(); c.drawPath(tri,p);
    }

    @Override public void setAlpha(int alpha) { this.alpha = alpha; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter colorFilter) { p.setColorFilter(colorFilter); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
