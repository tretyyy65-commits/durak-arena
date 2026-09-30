package com.nexora.durakarena;

import android.content.Context;
import android.graphics.Color;
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

public class Rank3DView extends FrameLayout {

    static {
        Utils.INSTANCE.init();
    }

    private TextureView textureView;
    private ModelViewer modelViewer;
    private boolean rendering = false;

    private final Choreographer.FrameCallback frameCallback =
            new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    if (!rendering || modelViewer == null) {
                        return;
                    }

                    modelViewer.render(frameTimeNanos);

                    Choreographer.getInstance()
                            .postFrameCallback(this);
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

        // Прозорий фон
        uiHelper.setOpaque(false);

        Engine engine = Engine.create();

        modelViewer = new ModelViewer(
                textureView,
                engine,
                uiHelper,
                null
        );

        // Прозорий рендер
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

            String path =
                    "models/ranks/" + fileName;

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

            byte[] bytes =
                    output.toByteArray();

            output.close();

            ByteBuffer modelBuffer =
                    ByteBuffer.wrap(bytes);

            modelViewer.loadModelGlb(
                    modelBuffer
            );

            // Автоматично вписує модель у вікно
            modelViewer.transformToUnitCube(
                    new Float3(
                            0.0f,
                            0.0f,
                            -4.0f
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        rendering = true;

        Choreographer.getInstance()
                .postFrameCallback(frameCallback);
    }

    @Override
    protected void onDetachedFromWindow() {

        rendering = false;

        Choreographer.getInstance()
                .removeFrameCallback(frameCallback);

        super.onDetachedFromWindow();
    }
}
