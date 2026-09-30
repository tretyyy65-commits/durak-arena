package com.nexora.durakarena;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Choreographer;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.google.android.filament.Engine;
import com.google.android.filament.Renderer;
import com.google.android.filament.View;
import com.google.android.filament.android.UiHelper;
import com.google.android.filament.utils.Float3;
import com.google.android.filament.utils.ModelViewer;
import com.google.android.filament.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Lightweight static 3D rank preview.
 *
 * Rank models on the Glory Path are static thumbnails. Rendering every rank at
 * display refresh rate makes a ScrollView extremely expensive, because every
 * Rank3DView owns its own Filament renderer. This view therefore renders only
 * while it is actually visible and only for a short warm-up window after a
 * model becomes visible. The last TextureView frame remains on screen.
 */
public class Rank3DView extends FrameLayout {

    static {
        Utils.INSTANCE.init();
    }

    private static final long FRAME_CHECK_DELAY_MS = 80L;
    private static final int MODEL_WARMUP_FRAMES = 40;
    private static final int REVEAL_WARMUP_FRAMES = 12;

    private TextureView textureView;
    private ModelViewer modelViewer;

    private boolean rendering = false;
    private boolean callbackPosted = false;
    private boolean wasVisible = false;
    private int framesRemaining = 0;

    private final Rect visibleRect = new Rect();

    private final Choreographer.FrameCallback frameCallback =
            new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    callbackPosted = false;

                    if (!rendering || modelViewer == null) {
                        return;
                    }

                    boolean visibleNow = isActuallyVisible();

                    if (visibleNow && !wasVisible) {
                        framesRemaining = Math.max(
                                framesRemaining,
                                REVEAL_WARMUP_FRAMES
                        );
                    }

                    wasVisible = visibleNow;

                    // Static rank badges do not need a permanent 60/90/120 FPS loop.
                    // Only visible badges receive a few frames so GLB resources can
                    // finish loading and the final image can settle in TextureView.
                    if (visibleNow && framesRemaining > 0) {
                        modelViewer.render(frameTimeNanos);
                        framesRemaining--;
                    }

                    postNextFrameCheck();
                }
            };

    public Rank3DView(Context context) {
        super(context);
        init3D();
    }

    private void init3D() {
        setBackgroundColor(Color.TRANSPARENT);
        setClipChildren(false);
        setClipToPadding(false);

        textureView = new TextureView(getContext());
        textureView.setOpaque(false);

        addView(
                textureView,
                new FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                )
        );

        UiHelper uiHelper =
                new UiHelper(
                        UiHelper.ContextErrorPolicy.DONT_CHECK
                );

        uiHelper.setOpaque(false);

        Engine engine = Engine.create();

        modelViewer = new ModelViewer(
                textureView,
                engine,
                uiHelper,
                null
        );

        Renderer.ClearOptions clearOptions =
                modelViewer.getRenderer().getClearOptions();

        clearOptions.clear = true;
        clearOptions.clearColor =
                new float[]{0.0f, 0.0f, 0.0f, 0.0f};

        modelViewer.getRenderer()
                .setClearOptions(clearOptions);

        modelViewer.getView()
                .setBlendMode(
                        View.BlendMode.TRANSLUCENT
                );
    }

    public void setRank(String rank) {
        String file;

        switch (rank) {
            case "PLAYER":
                file = "player.glb";
                break;

            case "PRO":
                file = "pro.glb";
                break;

            case "MASTER":
                file = "master.glb";
                break;

            case "ELITE_III":
                file = "elite_3.glb";
                break;

            case "ELITE_II":
                file = "elite_2.glb";
                break;

            case "ELITE_I":
                file = "elite_1.glb";
                break;

            case "LEGEND_III":
                file = "legend_3.glb";
                break;

            case "LEGEND_II":
                file = "legend_2.glb";
                break;

            case "LEGEND_I":
                file = "legend_1.glb";
                break;

            case "NOVICE":
            default:
                file = "novice.glb";
                break;
        }

        loadModel(file);
    }

    private void loadModel(String fileName) {
        try {
            String path = "models/ranks/" + fileName;

            InputStream input =
                    getContext()
                            .getAssets()
                            .open(path);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            byte[] buffer = new byte[16384];
            int count;

            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            input.close();

            byte[] bytes = output.toByteArray();
            output.close();

            ByteBuffer modelBuffer = ByteBuffer.wrap(bytes);

            modelViewer.loadModelGlb(modelBuffer);

            modelViewer.transformToUnitCube(
                    new Float3(
                            0.0f,
                            0.0f,
                            -4.0f
                    )
            );

            // Give the newly loaded GLB enough visible frames to finish its
            // asynchronous resource upload, then freeze the static thumbnail.
            framesRemaining = MODEL_WARMUP_FRAMES;
            wasVisible = false;

            if (rendering) {
                postNextFrameCheck();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isActuallyVisible() {
        if (!isShown() || getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }

        if (!getGlobalVisibleRect(visibleRect)) {
            return false;
        }

        // Ignore a tiny clipped sliver at the top/bottom of the ScrollView.
        return visibleRect.width() >= Math.max(1, getWidth() / 3)
                && visibleRect.height() >= Math.max(1, getHeight() / 3);
    }

    private void postNextFrameCheck() {
        if (!rendering || callbackPosted) {
            return;
        }

        callbackPosted = true;

        Choreographer.getInstance()
                .postFrameCallbackDelayed(
                        frameCallback,
                        FRAME_CHECK_DELAY_MS
                );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        rendering = true;
        framesRemaining = Math.max(
                framesRemaining,
                REVEAL_WARMUP_FRAMES
        );
        wasVisible = false;

        postNextFrameCheck();
    }

    @Override
    protected void onDetachedFromWindow() {
        rendering = false;
        callbackPosted = false;
        wasVisible = false;

        Choreographer.getInstance()
                .removeFrameCallback(frameCallback);

        super.onDetachedFromWindow();
    }
}
