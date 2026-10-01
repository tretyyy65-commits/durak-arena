package com.nexora.durakarena;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Exact-reference Glory Path screen.
 *
 * The approved artwork is rendered as the full-screen visual so the APK matches
 * the reference composition instead of rebuilding it with generic Android cards.
 * Interactive zones are transparent and sit above the artwork.
 */
public class GloryPathView extends FrameLayout {

    private final Context context;
    private final int playerCups;
    private final Runnable onBack;

    public GloryPathView(Context context, int playerCups, Runnable onBack) {
        super(context);
        this.context = context;
        this.playerCups = Math.max(0, playerCups);
        this.onBack = onBack;
        build();
    }

    private void build() {
        removeAllViews();
        setBackgroundColor(Color.BLACK);
        setClipChildren(false);
        setClipToPadding(false);

        // -----------------------------------------------------------------
        // APPROVED FULL-SCREEN ARTWORK
        // Resource lives locally at:
        // app/src/main/res/drawable-nodpi/glory_path_reference.png
        // getIdentifier keeps the Java source build-safe even before the binary
        // artwork is committed to the repository.
        // -----------------------------------------------------------------
        ImageView artwork = new ImageView(context);
        artwork.setBackgroundColor(Color.BLACK);
        artwork.setScaleType(ImageView.ScaleType.FIT_XY);
        artwork.setAdjustViewBounds(false);

        int artworkId = context.getResources().getIdentifier(
                "glory_path_reference",
                "drawable",
                context.getPackageName()
        );

        if (artworkId != 0) {
            artwork.setImageResource(artworkId);
        }

        addView(
                artwork,
                new FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                )
        );

        // -----------------------------------------------------------------
        // INVISIBLE INTERACTIVE HOTSPOTS
        // They keep the approved image visually untouched.
        // -----------------------------------------------------------------
        View backZone = new View(context);
        backZone.setBackgroundColor(Color.TRANSPARENT);
        backZone.setClickable(true);
        backZone.setOnClickListener(v -> {
            if (onBack != null) onBack.run();
        });

        FrameLayout.LayoutParams backLp = new FrameLayout.LayoutParams(
                dp(110),
                dp(110)
        );
        backLp.gravity = Gravity.TOP | Gravity.LEFT;
        addView(backZone, backLp);

        // Left rank navigation hotspot.
        View leftRankZone = new View(context);
        leftRankZone.setBackgroundColor(Color.TRANSPARENT);
        leftRankZone.setClickable(true);
        FrameLayout.LayoutParams leftLp = new FrameLayout.LayoutParams(
                dp(110),
                dp(170)
        );
        leftLp.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        leftLp.leftMargin = dp(8);
        addView(leftRankZone, leftLp);

        // Right rank navigation hotspot.
        View rightRankZone = new View(context);
        rightRankZone.setBackgroundColor(Color.TRANSPARENT);
        rightRankZone.setClickable(true);
        FrameLayout.LayoutParams rightLp = new FrameLayout.LayoutParams(
                dp(110),
                dp(170)
        );
        rightLp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        rightLp.rightMargin = dp(8);
        addView(rightRankZone, rightLp);

        // Info hotspot in the upper-right corner.
        View infoZone = new View(context);
        infoZone.setBackgroundColor(Color.TRANSPARENT);
        infoZone.setClickable(true);
        FrameLayout.LayoutParams infoLp = new FrameLayout.LayoutParams(
                dp(110),
                dp(110)
        );
        infoLp.gravity = Gravity.TOP | Gravity.RIGHT;
        addView(infoZone, infoLp);
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }
}
