package com.nexora.durakarena;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.Locale;

public class MainActivity extends Activity {

    private void enableDurakFullscreen() {

        // Гра завжди вертикальна.
        setRequestedOrientation(
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );

        android.view.Window window = getWindow();

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false);
        }

        if (android.os.Build.VERSION.SDK_INT >= 28) {
            android.view.WindowManager.LayoutParams params =
                    window.getAttributes();

            params.layoutInDisplayCutoutMode =
                    android.view.WindowManager.LayoutParams
                            .LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

            window.setAttributes(params);
        }

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);

        window.getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }



    private static final int RC_SIGN_IN = 1001;
    private static final int GOLD = Color.rgb(235, 176, 65);
    private static final int GOLD_LIGHT = Color.rgb(255, 221, 140);
    private static final int RED = Color.rgb(132, 8, 12);
    private static final int BLACK_PANEL = Color.rgb(10, 14, 18);

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private SharedPreferences prefs;

    private String currentScreen = "login";
    private TextView supportChatLog;
    private ScrollView supportScroll;
    private EditText supportInput;

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        prefs = getSharedPreferences("durak_arena_settings", MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        showLoginScreen();
    }

    private GradientDrawable box(int color, int strokeColor, int strokeWidth, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        g.setStroke(dp(strokeWidth), strokeColor);
        return g;
    }

    private TextView label(String text, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(GOLD_LIGHT);
        v.setTextSize(size);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setTypeface(Typeface.SERIF, bold ? Typeface.BOLD : Typeface.NORMAL);
        v.setPadding(dp(12), dp(8), dp(12), dp(8));
        return v;
    }

    private Button actionButton(String text, boolean red) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(GOLD_LIGHT);
        b.setTextSize(17);
        b.setTypeface(Typeface.SERIF, Typeface.BOLD);
        b.setBackground(box(
                red ? Color.rgb(110, 5, 8) : Color.rgb(8, 14, 19),
                GOLD,
                2,
                10
        ));
        b.setPadding(dp(10), dp(4), dp(10), dp(4));
        return b;
    }

    private FrameLayout makeBackgroundRoot(boolean strongDim) {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView bg = new ImageView(this);
        bg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int bgResId = getResources().getIdentifier(
                "login_background",
                "drawable",
                getPackageName()
        );

        if (bgResId != 0) {
            bg.setImageResource(bgResId);
        }
        root.addView(bg, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        View dim = new View(this);
        dim.setBackgroundColor(Color.argb(strongDim ? 150 : 25, 0, 0, 0));
        root.addView(dim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return root;
    }

    private void showLoginScreen() {

        aceEnableFullscreen();

        final FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(5, 6, 9));

        // =====================================================
        // КОДОВИЙ ФОН NEXORA: ACE ARENA
        // Без PNG. Малюється Android Canvas.
        // =====================================================

        // NEXORA: ACE ARENA — адаптивний фон
        final android.widget.ImageView casinoBackground =
                new android.widget.ImageView(this);

        casinoBackground.setImageResource(R.drawable.login_background_new);
        casinoBackground.setScaleType(
                android.widget.ImageView.ScaleType.CENTER_CROP
        );
        casinoBackground.setAdjustViewBounds(false);

        FrameLayout.LayoutParams backgroundParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        root.addView(casinoBackground, backgroundParams);


        // Легке затемнення для читабельності UI.
        View overlay = new View(this);
        overlay.setBackgroundColor(Color.argb(25, 0, 0, 0));

        root.addView(
                overlay,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        // =====================================================
        // НАЗВА ГРИ — КОДОМ
        // =====================================================
        final LinearLayout logoBlock = new LinearLayout(this);
        logoBlock.setOrientation(LinearLayout.VERTICAL);
        logoBlock.setGravity(Gravity.CENTER);

        TextView nexora = new TextView(this);
        nexora.setText("");
        nexora.setTextColor(Color.rgb(243, 190, 72));
        nexora.setGravity(Gravity.CENTER);
        nexora.setTextSize(23);
        nexora.setLetterSpacing(0.15f);
        nexora.setTypeface(
                android.graphics.Typeface.create(
                        "sans-serif",
                        android.graphics.Typeface.BOLD
                )
        );
        nexora.setShadowLayer(
                aceDp(12),
                0,
                aceDp(2),
                Color.argb(210, 150, 65, 0)
        );

        TextView ace = new TextView(this);
        ace.setText("");
        ace.setTextColor(Color.rgb(255, 204, 93));
        ace.setGravity(Gravity.CENTER);
        ace.setTextSize(44);
        ace.setTypeface(
                android.graphics.Typeface.create(
                        "serif",
                        android.graphics.Typeface.BOLD
                )
        );
        ace.setShadowLayer(
                aceDp(14),
                0,
                aceDp(3),
                Color.argb(230, 150, 55, 0)
        );


        logoBlock.addView(
                nexora,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        logoBlock.addView(
                ace,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );


        FrameLayout.LayoutParams logoParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        logoParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        logoParams.leftMargin = aceDp(18);
        logoParams.rightMargin = aceDp(18);

        root.addView(logoBlock, logoParams);

        // =====================================================
        // СПРАВЖНЯ КОДОВА КНОПКА НАЛАШТУВАНЬ
        // =====================================================
        final TextView settingsButton = new TextView(this);

        settingsButton.setText("⚙");
        settingsButton.setGravity(Gravity.CENTER);
        settingsButton.setTextColor(Color.rgb(255, 206, 94));
        settingsButton.setTextSize(28);
        settingsButton.setClickable(true);
        settingsButton.setFocusable(true);
        settingsButton.setBackground(aceHexDrawable());

        // ЗАЛИШАЄМО СТАРУ РОБОЧУ ФУНКЦІЮ.
        settingsButton.setOnClickListener(v -> showSettingsScreen());

        FrameLayout.LayoutParams settingsParams =
                new FrameLayout.LayoutParams(
                        aceDp(64),
                        aceDp(64)
                );

        settingsParams.gravity = Gravity.TOP | Gravity.END;
        settingsParams.topMargin = aceDp(22);
        settingsParams.rightMargin = aceDp(18);

        root.addView(settingsButton, settingsParams);

        // =====================================================
        // РАМКА "СТАНЬ ЛЕГЕНДОЮ"
        // =====================================================
        final TextView legendCard = new TextView(this);

        legendCard.setText(aceLegendText());
        legendCard.setGravity(Gravity.CENTER);
        legendCard.setTextColor(Color.rgb(255, 222, 151));
        legendCard.setTextSize(18);
        legendCard.setTypeface(
                android.graphics.Typeface.create(
                        "serif",
                        android.graphics.Typeface.BOLD
                )
        );
        legendCard.setLineSpacing(aceDp(2), 1.0f);
        legendCard.setPadding(
                aceDp(18),
                aceDp(13),
                aceDp(18),
                aceDp(13)
        );

        GradientDrawable legendBackground = new GradientDrawable();
        legendBackground.setColor(Color.argb(205, 7, 10, 14));
        legendBackground.setStroke(
                aceDp(1),
                Color.rgb(222, 162, 52)
        );
        legendBackground.setCornerRadius(aceDp(14));

        legendCard.setBackground(legendBackground);
        legendCard.setShadowLayer(
                aceDp(7),
                0,
                aceDp(1),
                Color.BLACK
        );

        FrameLayout.LayoutParams legendParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        legendParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        legendCard.setVisibility(android.view.View.GONE);
        // REMOVED: legendCard

        // =====================================================
        // СПРАВЖНЯ КОДОВА КНОПКА "УВІЙТИ"
        // Elite Glow
        // =====================================================
        final TextView loginButton = new TextView(this);

        loginButton.setText(aceLoginText());
        loginButton.setGravity(Gravity.CENTER);
        loginButton.setTextColor(Color.rgb(255, 225, 158));
        loginButton.setTextSize(24);
        loginButton.setTypeface(
                android.graphics.Typeface.create(
                        "serif",
                        android.graphics.Typeface.BOLD
                )
        );

        loginButton.setPadding(
                aceDp(16),
                aceDp(8),
                aceDp(16),
                aceDp(8)
        );

        loginButton.setClickable(true);
        loginButton.setFocusable(true);
        loginButton.setBackground(aceLoginDrawable());

        loginButton.setShadowLayer(
                aceDp(9),
                0,
                aceDp(2),
                Color.rgb(110, 0, 0)
        );

        // НЕ МІНЯЄМО GOOGLE SIGN-IN.
        // Просто викликаємо існуючий робочий signIn().
        loginButton.setOnClickListener(v -> signIn());

        FrameLayout.LayoutParams loginParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        aceDp(72)
                );

        loginParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        root.addView(loginButton, loginParams);

        // =====================================================
        // АДАПТИВНЕ РОЗТАШУВАННЯ
        // =====================================================
        root.addOnLayoutChangeListener(
                (v, l, t, r, b, oldL, oldT, oldR, oldB) -> {

                    int screenW = root.getWidth();
                    int screenH = root.getHeight();

                    if (screenW <= 0 || screenH <= 0) {
                        return;
                    }

                    int maxContentWidth = aceDp(520);
                    int normalWidth = screenW - aceDp(36);
                    int contentWidth = Math.min(normalWidth, maxContentWidth);

                    // ЛОГО
                    FrameLayout.LayoutParams lp =
                            (FrameLayout.LayoutParams) logoBlock.getLayoutParams();

                    lp.width = contentWidth;
                    lp.leftMargin = (screenW - contentWidth) / 2;
                    lp.rightMargin = 0;
                    lp.topMargin = Math.round(screenH * 0.105f);

                    logoBlock.setLayoutParams(lp);

                    // LEGEND CARD

                    // LOGIN BUTTON
                    FrameLayout.LayoutParams buttonLp =
                            (FrameLayout.LayoutParams) loginButton.getLayoutParams();

                    buttonLp.width = Math.min(
                            contentWidth,
                            aceDp(470)
                    );

                    buttonLp.leftMargin =
                            (screenW - buttonLp.width) / 2;

                    buttonLp.rightMargin = 0;
                    buttonLp.topMargin =
                            Math.round(screenH * 0.735f);

                    loginButton.setLayoutParams(buttonLp);
                }
        );

        // ===== NEXORA: LOGIN HELP =====
        final TextView loginHelp = new TextView(this);
        loginHelp.setText("Не можу увійти     •     Забули пароль?");
        loginHelp.setTextColor(Color.rgb(220, 205, 170));
        loginHelp.setTextSize(14);
        loginHelp.setGravity(Gravity.CENTER);
        loginHelp.setClickable(true);
        loginHelp.setPadding(aceDp(12), aceDp(8), aceDp(12), aceDp(8));

        FrameLayout.LayoutParams helpLp =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        helpLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        int helpScreenH = root.getHeight();
        if (helpScreenH <= 0) {
            helpScreenH = getResources().getDisplayMetrics().heightPixels;
        }

        helpLp.topMargin = Math.round(helpScreenH * 0.815f);

        root.addView(loginHelp, helpLp);

        loginHelp.setOnClickListener(v -> {
            showPasswordRecoveryScreen();
        });
        // ===== END LOGIN HELP =====

        setContentView(root);
    }


    // =========================================================
    // NEXORA ARENA — PASSWORD RECOVERY
    // =========================================================
    private void showPasswordRecoveryScreen() {
        aceEnableFullscreen();

        final FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(5, 13, 10));

        android.graphics.drawable.GradientDrawable panel =
                new android.graphics.drawable.GradientDrawable();
        panel.setColor(Color.rgb(10, 28, 21));
        panel.setCornerRadius(aceDp(22));
        panel.setStroke(aceDp(1), Color.rgb(185, 145, 70));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(aceDp(24), aceDp(28), aceDp(24), aceDp(28));
        card.setBackground(panel);

        TextView title = new TextView(this);
        title.setText("ВІДНОВЛЕННЯ ДОСТУПУ");
        title.setTextColor(Color.rgb(231, 201, 132));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(
                android.graphics.Typeface.create(
                        "serif",
                        android.graphics.Typeface.BOLD
                )
        );

        card.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView info = new TextView(this);
        info.setText(
                "Введіть Gmail, прив'язаний до вашого акаунта NEXORA ARENA."
        );
        info.setTextColor(Color.rgb(205, 205, 195));
        info.setTextSize(14);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, aceDp(12), 0, aceDp(20));

        card.addView(info, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        final android.widget.EditText email =
                new android.widget.EditText(this);

        email.setHint("Введіть Gmail");
        email.setHintTextColor(Color.rgb(130, 145, 135));
        email.setTextColor(Color.WHITE);
        email.setSingleLine(true);
        email.setTextSize(16);
        email.setPadding(
                aceDp(16), aceDp(4),
                aceDp(16), aceDp(4)
        );

        email.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        android.graphics.drawable.GradientDrawable fieldBg =
                new android.graphics.drawable.GradientDrawable();
        fieldBg.setColor(Color.rgb(6, 19, 14));
        fieldBg.setCornerRadius(aceDp(12));
        fieldBg.setStroke(aceDp(1), Color.rgb(120, 96, 52));
        email.setBackground(fieldBg);

        LinearLayout.LayoutParams emailLp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        aceDp(56)
                );
        emailLp.bottomMargin = aceDp(18);
        card.addView(email, emailLp);

        TextView sendButton = new TextView(this);
        sendButton.setText("НАДІСЛАТИ ЛИСТ");
        sendButton.setTextColor(Color.rgb(244, 224, 170));
        sendButton.setTextSize(16);
        sendButton.setGravity(Gravity.CENTER);
        sendButton.setTypeface(
                android.graphics.Typeface.DEFAULT_BOLD
        );
        sendButton.setClickable(true);

        android.graphics.drawable.GradientDrawable sendBg =
                new android.graphics.drawable.GradientDrawable();
        sendBg.setColor(Color.rgb(22, 78, 53));
        sendBg.setCornerRadius(aceDp(14));
        sendBg.setStroke(aceDp(1), Color.rgb(194, 153, 74));
        sendButton.setBackground(sendBg);

        card.addView(sendButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                aceDp(58)
        ));

        final TextView status = new TextView(this);
        status.setText("");
        status.setTextColor(Color.rgb(220, 205, 170));
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, aceDp(14), 0, aceDp(8));

        card.addView(status, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView back = new TextView(this);
        back.setText("‹  НАЗАД");
        back.setTextColor(Color.rgb(215, 184, 112));
        back.setTextSize(15);
        back.setGravity(Gravity.CENTER);
        back.setPadding(aceDp(10), aceDp(14), aceDp(10), aceDp(10));
        back.setClickable(true);

        card.addView(back, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        FrameLayout.LayoutParams cardLp =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        cardLp.gravity = Gravity.CENTER;
        cardLp.leftMargin = aceDp(22);
        cardLp.rightMargin = aceDp(22);

        root.addView(card, cardLp);

        back.setOnClickListener(v -> showLoginScreen());

        sendButton.setOnClickListener(v -> {
            String address = email.getText().toString().trim();

            if (address.isEmpty()) {
                status.setText("Введіть Gmail.");
                return;
            }

            sendButton.setEnabled(false);
            status.setText("Надсилаємо лист...");

            firebaseAuth.sendPasswordResetEmail(address)
                    .addOnCompleteListener(task -> {
                        sendButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            status.setText(
                                    "Лист надіслано. Перевірте свою пошту."
                            );
                        } else {
                            status.setText(
                                    "Не вдалося надіслати лист. Перевірте Gmail."
                            );
                        }
                    });
        });

        setContentView(root);
    }


    // =========================================================
    // NEXORA ACE ARENA — LOADING SCREEN
    // =========================================================
    private void showLoadingScreen() {
        aceEnableFullscreen();

        final FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.loading_background);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);

        root.addView(
                background,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        LinearLayout loadingBox = new LinearLayout(this);
        loadingBox.setOrientation(LinearLayout.VERTICAL);
        loadingBox.setGravity(Gravity.CENTER);

        TextView loadingText = new TextView(this);
        loadingText.setText("ЗАВАНТАЖЕННЯ...");
        loadingText.setTextColor(Color.rgb(235, 205, 125));
        loadingText.setTextSize(16);
        loadingText.setGravity(Gravity.CENTER);
        loadingText.setTypeface(
                android.graphics.Typeface.DEFAULT_BOLD
        );

        loadingBox.addView(
                loadingText,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        final android.widget.ProgressBar progress =
                new android.widget.ProgressBar(
                        this,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

        progress.setMax(100);
        progress.setProgress(Math.min(getSharedPreferences("durak_arena_profile", MODE_PRIVATE).getInt("player_cups", 0), 1000));

        android.graphics.drawable.GradientDrawable progressBg =
                new android.graphics.drawable.GradientDrawable();
        progressBg.setColor(Color.rgb(28, 25, 19));
        progressBg.setCornerRadius(aceDp(8));

        android.graphics.drawable.GradientDrawable progressFill =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{
                                Color.rgb(145, 96, 25),
                                Color.rgb(255, 213, 100)
                        }
                );
        progressFill.setCornerRadius(aceDp(8));

        android.graphics.drawable.ClipDrawable clip =
                new android.graphics.drawable.ClipDrawable(
                        progressFill,
                        Gravity.LEFT,
                        android.graphics.drawable.ClipDrawable.HORIZONTAL
                );

        android.graphics.drawable.LayerDrawable layers =
                new android.graphics.drawable.LayerDrawable(
                        new android.graphics.drawable.Drawable[]{
                                progressBg,
                                clip
                        }
                );

        layers.setId(0, android.R.id.background);
        layers.setId(1, android.R.id.progress);
        progress.setProgressDrawable(layers);

        LinearLayout.LayoutParams progressLp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        aceDp(10)
                );

        progressLp.topMargin = aceDp(12);
        loadingBox.addView(progress, progressLp);

        final TextView percent = new TextView(this);
        percent.setText("0%");
        percent.setTextColor(Color.WHITE);
        percent.setTextSize(13);
        percent.setGravity(Gravity.CENTER);
        percent.setPadding(0, aceDp(8), 0, 0);

        loadingBox.addView(
                percent,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        FrameLayout.LayoutParams boxLp =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        boxLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        boxLp.leftMargin = aceDp(40);
        boxLp.rightMargin = aceDp(40);
        boxLp.bottomMargin = aceDp(65);

        root.addView(loadingBox, boxLp);
        setContentView(root);

        final android.os.Handler handler =
                new android.os.Handler(android.os.Looper.getMainLooper());

        final int[] value = {0};

        Runnable updater = new Runnable() {
            @Override
            public void run() {
                value[0] += 2;

                if (value[0] > 100) {
                    value[0] = 100;
                }

                progress.setProgress(value[0]);
                percent.setText(value[0] + "%");

                if (value[0] < 100) {
                    handler.postDelayed(this, 45);
                } else {
                    // Тут наступним кроком відкриємо головне меню.
                    percent.setText("100%");
                    handler.postDelayed(() -> showMainMenu(), 500);
                }
            }
        };

        handler.postDelayed(updater, 150);
    }


    // =========================================================
    // DURAK ARENA V4 — GOOGLE AVATAR
    // =========================================================
    private void loadGoogleAvatar(
            final android.widget.TextView avatar,
            final String photoUrl,
            final int sizeDp) {

        if (avatar == null || photoUrl == null || photoUrl.trim().isEmpty()) {
            return;
        }

        new Thread(() -> {
            java.io.InputStream input = null;

            try {
                java.net.URL url = new java.net.URL(photoUrl);
                java.net.HttpURLConnection connection =
                        (java.net.HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setDoInput(true);
                connection.connect();

                input = connection.getInputStream();

                final android.graphics.Bitmap bitmap =
                        android.graphics.BitmapFactory.decodeStream(input);

                if (bitmap == null) return;

                final android.graphics.Bitmap scaled =
                        android.graphics.Bitmap.createScaledBitmap(
                                bitmap,
                                dp(sizeDp),
                                dp(sizeDp),
                                true
                        );

                runOnUiThread(() -> {
                    try {
                        android.graphics.drawable.BitmapDrawable drawable =
                                new android.graphics.drawable.BitmapDrawable(
                                        getResources(),
                                        scaled
                                );

                        drawable.setBounds(
                                0,
                                0,
                                dp(sizeDp),
                                dp(sizeDp)
                        );

                        avatar.setText("");
                        avatar.setCompoundDrawables(
                                drawable,
                                null,
                                null,
                                null
                        );
                        avatar.setGravity(android.view.Gravity.CENTER);

                    } catch (Exception ignored) {
                    }
                });

            } catch (Exception ignored) {

            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }).start();
    }

    private void showMainMenu() {
        currentScreen = "main_menu";

        final int MATCH = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        final int WRAP  = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

        final android.content.SharedPreferences menuProfilePrefs =
                getSharedPreferences("durak_arena_profile", MODE_PRIVATE);

        final String menuPlayerName =
                menuProfilePrefs.getString("player_name", "ГРАВЕЦЬ");

        final int menuPlayerLevel =
                menuProfilePrefs.getInt("player_level", 1);

        final int menuPlayerCups =
                menuProfilePrefs.getInt("player_cups", 0);

        final String menuPlayerPhoto =
                menuProfilePrefs.getString("player_photo_url", "");

        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundColor(android.graphics.Color.rgb(4, 8, 7));

        // =========================================================
        // ФОН — залишаємо main_menu_background
        // =========================================================
        android.widget.ImageView background = new android.widget.ImageView(this);
        background.setImageResource(R.drawable.main_menu_background);
        background.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        root.addView(background, new android.widget.FrameLayout.LayoutParams(MATCH, MATCH));

        // Легке затемнення для читабельності UI
        android.view.View shade = new android.view.View(this);
        shade.setBackgroundColor(android.graphics.Color.argb(35, 0, 0, 0));
        root.addView(shade, new android.widget.FrameLayout.LayoutParams(MATCH, MATCH));

        // =========================================================
        // HELPERS
        // =========================================================

        // =========================================================
        // ВЕРХНЯ ПАНЕЛЬ
        // =========================================================

        // ПРОФІЛЬ
        android.widget.LinearLayout profile = new android.widget.LinearLayout(this);
        profile.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        profile.setGravity(android.view.Gravity.CENTER_VERTICAL);
        profile.setPadding(dp(8), dp(5), dp(9), dp(5));

        android.graphics.drawable.GradientDrawable profileBg =
                new android.graphics.drawable.GradientDrawable();
        profileBg.setColor(android.graphics.Color.argb(215, 3, 12, 10));
        profileBg.setStroke(dp(1), android.graphics.Color.rgb(210, 158, 66));
        profileBg.setCornerRadius(dp(3));
        profile.setBackground(profileBg);

        android.widget.TextView avatar = new android.widget.TextView(this);
        avatar.setText("♠");
        avatar.setTextSize(22);
        avatar.setTextColor(android.graphics.Color.rgb(235, 185, 83));
        avatar.setGravity(android.view.Gravity.CENTER);

        android.graphics.drawable.GradientDrawable avatarBg =
                new android.graphics.drawable.GradientDrawable();
        avatarBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        avatarBg.setColor(android.graphics.Color.rgb(9, 15, 14));
        avatarBg.setStroke(dp(2), android.graphics.Color.rgb(221, 171, 73));
        avatar.setBackground(avatarBg);

        // DURAK ARENA V4 — Google avatar
        loadGoogleAvatar(avatar, menuPlayerPhoto, 43);

        android.widget.LinearLayout.LayoutParams avatarLP =
                new android.widget.LinearLayout.LayoutParams(dp(47), dp(47));
        profile.addView(avatar, avatarLP);

        android.widget.LinearLayout profileText = new android.widget.LinearLayout(this);
        profileText.setOrientation(android.widget.LinearLayout.VERTICAL);
        profileText.setPadding(dp(8), 0, 0, 0);

        android.widget.TextView playerName = new android.widget.TextView(this);
        playerName.setText(menuPlayerName);
        playerName.setTextColor(android.graphics.Color.rgb(255, 220, 151));
        playerName.setTextSize(14);
        playerName.setTypeface(android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD);

        android.widget.TextView playerLevel = new android.widget.TextView(this);
        playerLevel.setText("LV. " + menuPlayerLevel + "    🏆 " + menuPlayerCups);
        playerLevel.setTextColor(android.graphics.Color.rgb(255, 215, 135));
        playerLevel.setTextSize(12);

        android.widget.ProgressBar xp = new android.widget.ProgressBar(
                this, null, android.R.attr.progressBarStyleHorizontal);
        xp.setMax(100);
        xp.setProgress(12);

        profileText.addView(playerName);
        profileText.addView(playerLevel);

        android.widget.LinearLayout.LayoutParams xpLP =
                new android.widget.LinearLayout.LayoutParams(dp(92), dp(5));
        xpLP.topMargin = dp(3);
        profileText.addView(xp, xpLP);

        profile.addView(profileText);

        android.widget.FrameLayout.LayoutParams profileLP =
                new android.widget.FrameLayout.LayoutParams(dp(190), dp(64));
        profileLP.gravity = android.view.Gravity.TOP | android.view.Gravity.LEFT;
        profileLP.leftMargin = dp(8);
        profileLP.topMargin = dp(10);
        profile.setClickable(true);
        profile.setOnClickListener(v -> showProfileScreen());
        root.addView(profile, profileLP);

        // ВАЛЮТА
        android.widget.LinearLayout moneyBox = new android.widget.LinearLayout(this);
        moneyBox.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        moneyBox.setGravity(android.view.Gravity.CENTER);
        moneyBox.setPadding(dp(8), 0, dp(8), 0);

        android.graphics.drawable.GradientDrawable moneyBg =
                new android.graphics.drawable.GradientDrawable();
        moneyBg.setColor(android.graphics.Color.argb(220, 3, 12, 10));
        moneyBg.setStroke(dp(1), android.graphics.Color.rgb(210, 158, 66));
        moneyBg.setCornerRadius(dp(3));
        moneyBox.setBackground(moneyBg);

        android.widget.TextView money = new android.widget.TextView(this);
        money.setText("🪙  0   +");
        money.setTextSize(14);
        money.setTextColor(android.graphics.Color.rgb(255, 218, 145));
        money.setGravity(android.view.Gravity.CENTER);
        money.setTypeface(android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD);

        android.widget.TextView gems = new android.widget.TextView(this);
        gems.setText("   💎  0   +");
        gems.setTextSize(14);
        gems.setTextColor(android.graphics.Color.rgb(255, 218, 145));
        gems.setGravity(android.view.Gravity.CENTER);
        gems.setTypeface(android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD);

        moneyBox.addView(money);
        moneyBox.addView(gems);

        android.widget.FrameLayout.LayoutParams moneyLP =
                new android.widget.FrameLayout.LayoutParams(dp(157), dp(50));
        moneyLP.gravity = android.view.Gravity.TOP | android.view.Gravity.RIGHT;
        moneyLP.rightMargin = dp(58);
        moneyLP.topMargin = dp(10);
        root.addView(moneyBox, moneyLP);

        // =========================================================
        // СПИСОК ЛІДЕРІВ — залишаємо окремою кнопкою
        // =========================================================
        android.widget.TextView leaders = new android.widget.TextView(this);
        leaders.setText("🏆\nЛІДЕРИ");
        leaders.setGravity(android.view.Gravity.CENTER);
        leaders.setTextColor(android.graphics.Color.rgb(255, 218, 145));
        leaders.setTextSize(9);
        leaders.setTypeface(android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD);

        android.graphics.drawable.GradientDrawable leadersBg =
                new android.graphics.drawable.GradientDrawable();
        leadersBg.setColor(android.graphics.Color.argb(225, 3, 12, 10));
        leadersBg.setStroke(dp(1), android.graphics.Color.rgb(220, 170, 73));
        leadersBg.setCornerRadius(dp(5));
        leaders.setBackground(leadersBg);

        android.widget.FrameLayout.LayoutParams leadersLP =
                new android.widget.FrameLayout.LayoutParams(dp(53), dp(58));
        leadersLP.gravity = android.view.Gravity.TOP | android.view.Gravity.RIGHT;
        leadersLP.rightMargin = dp(5);
        leadersLP.topMargin = dp(69);
        root.addView(leaders, leadersLP);

        // =========================================================
        // ШЕСТИГРАННИК НАЛАШТУВАНЬ
        // =========================================================
        android.widget.TextView settings = new android.widget.TextView(this);
        settings.setText("⚙");
        settings.setTextSize(25);
        settings.setGravity(android.view.Gravity.CENTER);
        settings.setTextColor(android.graphics.Color.rgb(246, 205, 122));

        android.graphics.drawable.GradientDrawable settingsBg =
                new android.graphics.drawable.GradientDrawable();
        settingsBg.setColor(android.graphics.Color.argb(235, 5, 12, 10));
        settingsBg.setStroke(dp(2), android.graphics.Color.rgb(224, 173, 76));
        settingsBg.setCornerRadius(dp(10));
        settings.setBackground(settingsBg);

        settings.setOnClickListener(v -> showSettingsScreen());

        android.widget.FrameLayout.LayoutParams settingsLP =
                new android.widget.FrameLayout.LayoutParams(dp(48), dp(48));
        settingsLP.gravity = android.view.Gravity.TOP | android.view.Gravity.RIGHT;
        settingsLP.rightMargin = dp(5);
        settingsLP.topMargin = dp(10);
        root.addView(settings, settingsLP);

        // =========================================================
        // КНОПКА ГРАТИ — PREMIUM
        // =========================================================
        android.widget.TextView play = new android.widget.TextView(this);
        play.setText("♠    ГРАТИ    ♠");
        play.setGravity(android.view.Gravity.CENTER);
        play.setTextSize(27);
        play.setTextColor(android.graphics.Color.rgb(255, 225, 158));
        play.setTypeface(android.graphics.Typeface.SERIF,
                android.graphics.Typeface.BOLD);
        play.setShadowLayer(9f, 0f, 0f,
                android.graphics.Color.rgb(224, 164, 55));

        android.graphics.drawable.GradientDrawable playBg =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {
                                android.graphics.Color.rgb(15, 92, 65),
                                android.graphics.Color.rgb(3, 48, 35),
                                android.graphics.Color.rgb(2, 31, 24)
                        });
        playBg.setStroke(dp(3), android.graphics.Color.rgb(232, 183, 76));
        playBg.setCornerRadius(dp(42));
        play.setBackground(playBg);

        android.widget.FrameLayout.LayoutParams playLP =
                new android.widget.FrameLayout.LayoutParams(dp(300), dp(75));
        playLP.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        playLP.bottomMargin = dp(211);
        root.addView(play, playLP);

        // Пошук гри починається ТІЛЬКИ після натискання ГРАТИ.
        play.setClickable(true);
        play.setFocusable(true);
        play.setOnClickListener(v -> startSelectedGameMode());


        // =========================================================
        // РЕЖИМ
        // =========================================================
        android.widget.TextView mode = new android.widget.TextView(this);
        android.content.SharedPreferences gamePrefs =
                getSharedPreferences("durak_arena_game", MODE_PRIVATE);

        String selectedModeName =
                gamePrefs.getString(
                        "selected_mode_name",
                        "Класичний"
                );

        String selectedModeIcon =
                gamePrefs.getString(
                        "selected_mode_icon",
                        "♠"
                );

        mode.setText(
                "‹     " + selectedModeName + " режим     ›\n" +
                selectedModeIcon + "  2–5 гравців"
        );
        mode.setGravity(android.view.Gravity.CENTER);
        mode.setTextColor(android.graphics.Color.rgb(255, 218, 151));
        mode.setTextSize(12);

        android.graphics.drawable.GradientDrawable modeBg =
                new android.graphics.drawable.GradientDrawable();
        modeBg.setColor(android.graphics.Color.argb(220, 3, 12, 10));
        modeBg.setStroke(dp(1), android.graphics.Color.rgb(214, 160, 67));
        modeBg.setCornerRadius(dp(8));
        mode.setBackground(modeBg);

        // DURAK ARENA — відкриваємо повний список режимів
        mode.setClickable(true);
        mode.setFocusable(true);
        mode.setOnClickListener(v -> showGameModesScreen());

        android.widget.FrameLayout.LayoutParams modeLP =
                new android.widget.FrameLayout.LayoutParams(dp(255), dp(57));
        modeLP.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        modeLP.bottomMargin = dp(148);
        root.addView(mode, modeLP);

        // =========================================================
        // АРЕНА / ПРОГРЕС
        // =========================================================
        android.widget.LinearLayout arena = new android.widget.LinearLayout(this);
        arena.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        arena.setGravity(android.view.Gravity.CENTER_VERTICAL);
        arena.setPadding(dp(12), dp(5), dp(12), dp(5));

        android.graphics.drawable.GradientDrawable arenaBg =
                new android.graphics.drawable.GradientDrawable();
        arenaBg.setColor(android.graphics.Color.argb(225, 2, 11, 9));
        arenaBg.setStroke(dp(1), android.graphics.Color.rgb(213, 160, 67));
        arenaBg.setCornerRadius(dp(5));
        arena.setBackground(arenaBg);

        android.widget.TextView shield = new android.widget.TextView(this);
        int leagueIndex = LeagueSystem.indexForCups(menuPlayerCups);

        String leagueMark =
                leagueIndex >= 13 ? "♛" :
                leagueIndex >= 10 ? "♦" :
                leagueIndex >= 7  ? "♠" :
                leagueIndex >= 4  ? "★" : "♣";

        shield.setText(leagueMark);
        shield.setTextSize(29);
        shield.setGravity(android.view.Gravity.CENTER);
        shield.setTextColor(android.graphics.Color.rgb(222, 171, 73));
        arena.addView(shield,
                new android.widget.LinearLayout.LayoutParams(dp(47), MATCH));

        android.widget.LinearLayout arenaText = new android.widget.LinearLayout(this);
        arenaText.setOrientation(android.widget.LinearLayout.VERTICAL);

        android.widget.TextView rank = new android.widget.TextView(this);
        rank.setText(LeagueSystem.nameForCups(menuPlayerCups));
        rank.setTextColor(android.graphics.Color.rgb(255, 220, 150));
        rank.setTextSize(13);
        rank.setTypeface(android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD);

        android.widget.TextView cups = new android.widget.TextView(this);
        cups.setText(
                LeagueSystem.isMaxLeague(menuPlayerCups)
                        ? "🏆 " + menuPlayerCups + " • MAX"
                        : "🏆 " + menuPlayerCups + " / " +
                          LeagueSystem.nextStartForCups(menuPlayerCups)
        );
        cups.setTextColor(android.graphics.Color.rgb(255, 214, 134));
        cups.setTextSize(12);

        android.widget.ProgressBar progress = new android.widget.ProgressBar(
                this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(
                LeagueSystem.progressMaxForCups(menuPlayerCups)
        );
        progress.setProgress(
                LeagueSystem.progressForCups(menuPlayerCups)
        );

        arenaText.addView(rank);
        arenaText.addView(cups);

        android.widget.LinearLayout.LayoutParams progressLP =
                new android.widget.LinearLayout.LayoutParams(dp(170), dp(6));
        progressLP.topMargin = dp(4);
        arenaText.addView(progress, progressLP);

        arena.addView(arenaText);

        android.widget.TextView nextArena = new android.widget.TextView(this);
        nextArena.setText(
                LeagueSystem.isMaxLeague(menuPlayerCups)
                        ? "♛\nMAX"
                        : "→\n" + LeagueSystem.nextNameForCups(menuPlayerCups)
        );
        nextArena.setGravity(android.view.Gravity.CENTER);
        nextArena.setTextSize(10);
        nextArena.setTextColor(android.graphics.Color.rgb(255, 218, 145));

        android.widget.LinearLayout.LayoutParams nextLP =
                new android.widget.LinearLayout.LayoutParams(dp(65), MATCH);
        nextLP.leftMargin = dp(6);
        arena.addView(nextArena, nextLP);
        arena.setClickable(true);
        arena.setOnClickListener(v -> showGloryPathScreen());


        android.widget.FrameLayout.LayoutParams arenaLP =
                new android.widget.FrameLayout.LayoutParams(dp(330), dp(78));
        arenaLP.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        arenaLP.bottomMargin = dp(65);
        root.addView(arena, arenaLP);

        // =========================================================
        // НИЖНЄ PREMIUM МЕНЮ
        // =========================================================
        android.widget.LinearLayout bottom = new android.widget.LinearLayout(this);
        bottom.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        bottom.setGravity(android.view.Gravity.CENTER);

        String[] bottomIcons = {"🛒", "🎒", "▣", "♣", "🎁"};
        String[] bottomNames = {"МАГАЗИН", "ІНВЕНТАР", "СЕЗОН", "КЛАН", "ПОДІЇ"};

        for (int i = 0; i < bottomNames.length; i++) {
            android.widget.LinearLayout item = new android.widget.LinearLayout(this);
            item.setOrientation(android.widget.LinearLayout.VERTICAL);
            item.setGravity(android.view.Gravity.CENTER);

            android.graphics.drawable.GradientDrawable itemBg =
                    new android.graphics.drawable.GradientDrawable(
                            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[] {
                                    android.graphics.Color.argb(245, 5, 17, 14),
                                    android.graphics.Color.argb(245, 2, 9, 8)
                            });
            itemBg.setStroke(dp(1), android.graphics.Color.rgb(197, 145, 55));
            item.setBackground(itemBg);

            android.widget.TextView icon = new android.widget.TextView(this);
            icon.setText(bottomIcons[i]);
            icon.setTextSize(21);
            icon.setGravity(android.view.Gravity.CENTER);
            icon.setTextColor(android.graphics.Color.rgb(235, 182, 78));

            android.widget.TextView title = new android.widget.TextView(this);
            title.setText(bottomNames[i]);
            title.setTextSize(8);
            title.setGravity(android.view.Gravity.CENTER);
            title.setTextColor(android.graphics.Color.rgb(255, 218, 145));
            title.setTypeface(android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.BOLD);

            item.addView(icon,
                    new android.widget.LinearLayout.LayoutParams(MATCH, dp(32)));
            item.addView(title,
                    new android.widget.LinearLayout.LayoutParams(MATCH, dp(20)));

            android.widget.LinearLayout.LayoutParams itemLP =
                    new android.widget.LinearLayout.LayoutParams(0, MATCH, 1f);
            itemLP.setMargins(dp(1), 0, dp(1), 0);
            bottom.addView(item, itemLP);
        }

        android.widget.FrameLayout.LayoutParams bottomLP =
                new android.widget.FrameLayout.LayoutParams(MATCH, dp(61));
        bottomLP.gravity = android.view.Gravity.BOTTOM;
        root.addView(bottom, bottomLP);


        // =========================================================
        // DURAK ARENA — ADAPTIVE PHONE LAYOUT V2
        //
        // Дизайн НЕ змінюємо.
        // Позиції центральних елементів рахуються від фактичної
        // ширини та висоти екрана.
        // =========================================================
        root.addOnLayoutChangeListener(
                (v, l, t, r, b, oldL, oldT, oldR, oldB) -> {

                    int screenW = root.getWidth();
                    int screenH = root.getHeight();

                    if (screenW <= 0 || screenH <= 0) {
                        return;
                    }

                    float density =
                            getResources().getDisplayMetrics().density;

                    float widthDp =
                            screenW / density;

                    float heightDp =
                            screenH / density;


                    // -------------------------------------------------
                    // ШИРИНА КОНТЕНТУ
                    //
                    // На стандартному телефоні залишаємо майже ті самі
                    // 300 / 255 / 330 dp.
                    // На вузькому — автоматично зменшуємо.
                    // На широкому — не розтягуємо UI безмежно.
                    // -------------------------------------------------
                    int sideSpace = dp(16);

                    int availableWidth =
                            screenW - (sideSpace * 2);

                    int playWidth =
                            Math.min(dp(300), availableWidth);

                    int modeWidth =
                            Math.min(dp(255), availableWidth);

                    int arenaWidth =
                            Math.min(dp(330), availableWidth);


                    // -------------------------------------------------
                    // ВИСОТА ЕЛЕМЕНТІВ
                    // -------------------------------------------------
                    int playHeight = dp(75);
                    int modeHeight = dp(57);
                    int arenaHeight = dp(78);


                    // Дуже маленькі телефони
                    if (heightDp < 650f) {
                        playHeight = dp(66);
                        modeHeight = dp(52);
                        arenaHeight = dp(70);

                        play.setTextSize(24);
                        mode.setTextSize(11);
                    }

                    // Дуже вузькі телефони
                    if (widthDp < 340f) {
                        play.setTextSize(23);
                        mode.setTextSize(10);
                    }


                    // -------------------------------------------------
                    // НИЖНЄ МЕНЮ
                    // -------------------------------------------------
                    int bottomHeight = dp(61);

                    android.widget.FrameLayout.LayoutParams adaptiveBottom =
                            (android.widget.FrameLayout.LayoutParams)
                                    bottom.getLayoutParams();

                    adaptiveBottom.height = bottomHeight;
                    adaptiveBottom.gravity =
                            android.view.Gravity.BOTTOM;

                    bottom.setLayoutParams(adaptiveBottom);


                    // -------------------------------------------------
                    // АРЕНА
                    // Завжди над нижнім меню.
                    // -------------------------------------------------
                    int arenaBottom =
                            bottomHeight + dp(4);

                    android.widget.FrameLayout.LayoutParams adaptiveArena =
                            (android.widget.FrameLayout.LayoutParams)
                                    arena.getLayoutParams();

                    adaptiveArena.width = arenaWidth;
                    adaptiveArena.height = arenaHeight;

                    adaptiveArena.gravity =
                            android.view.Gravity.BOTTOM |
                            android.view.Gravity.CENTER_HORIZONTAL;

                    adaptiveArena.bottomMargin =
                            arenaBottom;

                    arena.setLayoutParams(adaptiveArena);


                    // -------------------------------------------------
                    // РЕЖИМ
                    // Над ареною з нормальним проміжком.
                    // -------------------------------------------------
                    int modeBottom =
                            arenaBottom +
                            arenaHeight +
                            dp(5);

                    android.widget.FrameLayout.LayoutParams adaptiveMode =
                            (android.widget.FrameLayout.LayoutParams)
                                    mode.getLayoutParams();

                    adaptiveMode.width = modeWidth;
                    adaptiveMode.height = modeHeight;

                    adaptiveMode.gravity =
                            android.view.Gravity.BOTTOM |
                            android.view.Gravity.CENTER_HORIZONTAL;

                    adaptiveMode.bottomMargin =
                            modeBottom;

                    mode.setLayoutParams(adaptiveMode);


                    // -------------------------------------------------
                    // ГРАТИ
                    // Над вибором режиму.
                    // -------------------------------------------------
                    int playBottom =
                            modeBottom +
                            modeHeight +
                            dp(6);

                    android.widget.FrameLayout.LayoutParams adaptivePlay =
                            (android.widget.FrameLayout.LayoutParams)
                                    play.getLayoutParams();

                    adaptivePlay.width = playWidth;
                    adaptivePlay.height = playHeight;

                    adaptivePlay.gravity =
                            android.view.Gravity.BOTTOM |
                            android.view.Gravity.CENTER_HORIZONTAL;

                    adaptivePlay.bottomMargin =
                            playBottom;

                    play.setLayoutParams(adaptivePlay);


                    // -------------------------------------------------
                    // ВЕРХНЯ ПАНЕЛЬ
                    // Не даємо профілю вилізти за ширину екрана.
                    // -------------------------------------------------
                    android.widget.FrameLayout.LayoutParams adaptiveProfile =
                            (android.widget.FrameLayout.LayoutParams)
                                    profile.getLayoutParams();

                    adaptiveProfile.width =
                            Math.min(dp(190),
                                    Math.max(dp(145),
                                            screenW - dp(185)));

                    profile.setLayoutParams(adaptiveProfile);


                    // -------------------------------------------------
                    // ВАЛЮТА
                    // На вузьких телефонах трохи стискаємо.
                    // -------------------------------------------------
                    android.widget.FrameLayout.LayoutParams adaptiveMoney =
                            (android.widget.FrameLayout.LayoutParams)
                                    moneyBox.getLayoutParams();

                    if (widthDp < 370f) {
                        adaptiveMoney.width = dp(135);
                    } else {
                        adaptiveMoney.width = dp(157);
                    }

                    moneyBox.setLayoutParams(adaptiveMoney);
                }
        );


        setContentView(root);
    }


    // =========================================================
    // DURAK ARENA — GAME MODES V1
    // =========================================================

    // =========================================================
    // PLAY -> START SELECTED MODE
    // =========================================================
    private void startSelectedGameMode() {

        android.content.SharedPreferences prefs =
                getSharedPreferences(
                        "durak_arena_game",
                        MODE_PRIVATE
                );

        String selectedMode =
                prefs.getString(
                        "selected_mode_id",
                        "ranked"
                );

        if ("ranked".equals(selectedMode)) {

            startRankedMatchmaking();
            return;
        }

        // Інші режими вже можна вибирати й зберігати.
        // Їхній окремий matchmaking підключимо до цього switch,
        // коли реалізуємо відповідні типи матчів.
        String selectedName =
                prefs.getString(
                        "selected_mode_name",
                        "Рейтинговий"
                );

        android.widget.Toast.makeText(
                this,
                selectedName + ": режим вибрано",
                android.widget.Toast.LENGTH_SHORT
        ).show();
    }


    private void startRankedMatchmaking() {
currentScreen = "matchmaking";

            android.content.SharedPreferences matchmakingProfile =
                    getSharedPreferences("durak_arena_profile", MODE_PRIVATE);

            String matchmakingPlayerName =
                    matchmakingProfile.getString("player_name", "ГРАВЕЦЬ");

            int matchmakingCups =
                    matchmakingProfile.getInt("player_cups", 0);

            setContentView(
                    MatchmakingView.create(
                            this,
                            matchmakingPlayerName,
                            matchmakingCups,
                            new MatchmakingView.Listener() {
                                @Override
                                public void onCancel() {
                                    showGameModesScreen();
                                }

                                @Override
                                public void onMatchFound() {
                                    currentScreen = "ranked_game";

                                    setContentView(
                                            RankedGameView.create(
                                                    MainActivity.this,
                                                    matchmakingPlayerName,
                                                    matchmakingCups,
                                                    new RankedGameView.Listener() {
                                                        @Override
                                                        public void onExit() {
                                                            showGameModesScreen();
                                                        }
                                                    }
                                            )
                                    );
                                }
                            }
                    )
            );
        
    }

    private void showGameModesScreen() {

        currentScreen = "game_modes";

        final int MATCH =
                android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        final int WRAP =
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

        // -----------------------------------------------------
        // ROOT + ТОЙ САМИЙ ФОН, ЩО У НАШОМУ APK
        // -----------------------------------------------------
        android.widget.FrameLayout root =
                new android.widget.FrameLayout(this);

        root.setBackgroundColor(
                android.graphics.Color.rgb(4, 8, 7)
        );

        android.widget.ImageView background =
                new android.widget.ImageView(this);

        background.setImageResource(
                R.drawable.main_menu_background
        );

        background.setScaleType(
                android.widget.ImageView.ScaleType.CENTER_CROP
        );

        root.addView(
                background,
                new android.widget.FrameLayout.LayoutParams(
                        MATCH,
                        MATCH
                )
        );

        // Темне затемнення — як у профілі
        android.view.View shade =
                new android.view.View(this);

        shade.setBackgroundColor(
                android.graphics.Color.argb(
                        155,
                        0,
                        4,
                        3
                )
        );

        root.addView(
                shade,
                new android.widget.FrameLayout.LayoutParams(
                        MATCH,
                        MATCH
                )
        );


        // -----------------------------------------------------
        // SCROLL
        // -----------------------------------------------------
        android.widget.ScrollView scroll =
                new android.widget.ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);


        android.widget.LinearLayout page =
                new android.widget.LinearLayout(this);

        page.setOrientation(
                android.widget.LinearLayout.VERTICAL
        );

        page.setGravity(
                android.view.Gravity.CENTER_HORIZONTAL
        );

        page.setPadding(
                dp(14),
                dp(18),
                dp(14),
                dp(30)
        );

        // ADAPTIVE V2:
        // на вузькому телефоні картки займають доступну ширину,
        // а на дуже широкому екрані не розтягуються надмірно.
        page.setMinimumWidth(0);


        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------
        android.widget.LinearLayout header =
                new android.widget.LinearLayout(this);

        header.setOrientation(
                android.widget.LinearLayout.HORIZONTAL
        );

        header.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );


        // BACK
        android.widget.TextView back =
                new android.widget.TextView(this);

        back.setText("‹");
        back.setTextSize(34);

        back.setTextColor(
                android.graphics.Color.rgb(
                        240,
                        190,
                        90
                )
        );

        back.setGravity(
                android.view.Gravity.CENTER
        );

        android.graphics.drawable.GradientDrawable backBg =
                new android.graphics.drawable.GradientDrawable();

        backBg.setColor(
                android.graphics.Color.argb(
                        225,
                        4,
                        13,
                        11
                )
        );

        backBg.setStroke(
                dp(1),
                android.graphics.Color.rgb(
                        215,
                        165,
                        70
                )
        );

        backBg.setCornerRadius(
                dp(10)
        );

        back.setBackground(backBg);
        back.setClickable(true);

        back.setOnClickListener(
                v -> showMainMenu()
        );

        header.addView(
                back,
                new android.widget.LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                )
        );


        // TITLE
        android.widget.TextView title =
                new android.widget.TextView(this);

        title.setText("РЕЖИМИ ГРИ");
        title.setTextSize(25);

        title.setTextColor(
                android.graphics.Color.rgb(
                        255,
                        215,
                        130
                )
        );

        title.setTypeface(
                android.graphics.Typeface.SERIF,
                android.graphics.Typeface.BOLD
        );

        title.setGravity(
                android.view.Gravity.CENTER
        );

        title.setShadowLayer(
                7f,
                0f,
                2f,
                android.graphics.Color.BLACK
        );

        header.addView(
                title,
                new android.widget.LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1f
                )
        );


        android.widget.Space rightSpace =
                new android.widget.Space(this);

        header.addView(
                rightSpace,
                new android.widget.LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                )
        );

        page.addView(header);


        // -----------------------------------------------------
        // ПІДЗАГОЛОВОК
        // -----------------------------------------------------
        android.widget.TextView subtitle =
                new android.widget.TextView(this);

        subtitle.setText(
                "ОБЕРИ СВІЙ ШЛЯХ ДО ПЕРЕМОГИ"
        );

        subtitle.setTextSize(12);

        subtitle.setLetterSpacing(0.08f);

        subtitle.setTextColor(
                android.graphics.Color.rgb(
                        208,
                        174,
                        105
                )
        );

        subtitle.setGravity(
                android.view.Gravity.CENTER
        );

        subtitle.setPadding(
                0,
                dp(10),
                0,
                dp(18)
        );

        page.addView(
                subtitle,
                new android.widget.LinearLayout.LayoutParams(
                        MATCH,
                        WRAP
                )
        );


        // -----------------------------------------------------
        // MODE 1 — РЕЙТИНГОВИЙ
        // -----------------------------------------------------
        android.widget.LinearLayout ranked =
                makeGameModeCard(
                        "♠",
                        "РЕЙТИНГОВИЙ",
                        "Грай на кубки та піднімайся в рейтингу",
                        "🏆"
                );

        ranked.setOnClickListener(v -> {

            saveSelectedGameMode(
                    "ranked",
                    "Рейтинговий",
                    "🏆"
            );

            android.widget.Toast.makeText(
                    this,
                    "Обрано: Рейтинговий",
                    android.widget.Toast.LENGTH_SHORT
            ).show();

            // ВАЖЛИВО:
            // тут пошук матчу НЕ запускаємо.
            // Повертаємо користувача на головне меню.
            showMainMenu();
        });

        page.addView(
                ranked,
                gameModeCardParams()
        );


        // -----------------------------------------------------
        // MODE 2 — КЛАСИЧНИЙ
        // -----------------------------------------------------
        android.widget.LinearLayout classic =
                makeGameModeCard(
                        "♣",
                        "КЛАСИЧНИЙ",
                        "Класичний Дурак без кубків",
                        "♠"
                );

        classic.setOnClickListener(v -> {
            saveSelectedGameMode(
                    "classic",
                    "Класичний",
                    "♠"
            );

            android.widget.Toast.makeText(
                    this,
                    "Обрано: Класичний",
                    android.widget.Toast.LENGTH_SHORT
            ).show();

            showMainMenu();
        });

        page.addView(
                classic,
                gameModeCardParams()
        );


        // -----------------------------------------------------
        // MODE 3 — БИТВА КЛАНІВ 3x3
        // -----------------------------------------------------
        android.widget.LinearLayout clanBattle =
                makeGameModeCard(
                        "⚔",
                        "БИТВА КЛАНІВ 3×3",
                        "3 гравці твого клану проти 3 суперників",
                        "♣"
                );

        clanBattle.setOnClickListener(v -> {
            saveSelectedGameMode(
                    "clan_3x3",
                    "Битва кланів 3×3",
                    "⚔"
            );

            android.widget.Toast.makeText(
                    this,
                    "Обрано: Битва кланів 3×3",
                    android.widget.Toast.LENGTH_SHORT
            ).show();

            showMainMenu();
        });

        page.addView(
                clanBattle,
                gameModeCardParams()
        );


        // -----------------------------------------------------
        // MODE 4 — КЛАНОВА ЛІГА
        // -----------------------------------------------------
        android.widget.LinearLayout clanLeague =
                makeGameModeCard(
                        "♛",
                        "КЛАНОВА ЛІГА",
                        "Здобувай лігові кубки для свого клану",
                        "🏆"
                );

        clanLeague.setOnClickListener(v -> {
            saveSelectedGameMode(
                    "clan_league",
                    "Кланова ліга",
                    "♛"
            );

            android.widget.Toast.makeText(
                    this,
                    "Обрано: Кланова ліга",
                    android.widget.Toast.LENGTH_SHORT
            ).show();

            showMainMenu();
        });

        page.addView(
                clanLeague,
                gameModeCardParams()
        );


        // -----------------------------------------------------
        // MODE 5 — БІЙ КЛАНІВ 2x2
        // -----------------------------------------------------
        android.widget.LinearLayout clan2x2 =
                makeGameModeCard(
                        "♦",
                        "БІЙ КЛАНІВ 2×2",
                        "Швидкий командний бій — 2 проти 2",
                        "⚔"
                );

        clan2x2.setOnClickListener(v -> {
            saveSelectedGameMode(
                    "clan_2x2",
                    "Бій кланів 2×2",
                    "♦"
            );

            android.widget.Toast.makeText(
                    this,
                    "Обрано: Бій кланів 2×2",
                    android.widget.Toast.LENGTH_SHORT
            ).show();

            showMainMenu();
        });

        page.addView(
                clan2x2,
                gameModeCardParams()
        );


        // -----------------------------------------------------
        // FOOTER
        // -----------------------------------------------------
        android.widget.TextView footer =
                new android.widget.TextView(this);

        footer.setText(
                "NEXORA INTERACTIVE  •  DURAK ARENA"
        );

        footer.setTextSize(9);

        footer.setLetterSpacing(0.08f);

        footer.setTextColor(
                android.graphics.Color.rgb(
                        156,
                        132,
                        83
                )
        );

        footer.setGravity(
                android.view.Gravity.CENTER
        );

        footer.setPadding(
                0,
                dp(24),
                0,
                dp(10)
        );

        page.addView(
                footer,
                new android.widget.LinearLayout.LayoutParams(
                        MATCH,
                        WRAP
                )
        );


        scroll.addView(
                page,
                new android.widget.ScrollView.LayoutParams(
                        MATCH,
                        WRAP
                )
        );

        root.addView(
                scroll,
                new android.widget.FrameLayout.LayoutParams(
                        MATCH,
                        MATCH
                )
        );

        setContentView(root);
    }


    // =========================================================
    // DURAK ARENA — MODE CARD
    // =========================================================
    private android.widget.LinearLayout makeGameModeCard(
            String iconText,
            String titleText,
            String descriptionText,
            String rightIconText) {

        final int MATCH =
                android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        final int WRAP =
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


        android.widget.LinearLayout card =
                new android.widget.LinearLayout(this);

        card.setOrientation(
                android.widget.LinearLayout.HORIZONTAL
        );

        card.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(13),
                dp(11),
                dp(12),
                dp(11)
        );

        card.setClickable(true);
        card.setFocusable(true);
        card.setMinimumHeight(dp(91));


        // -----------------------------------------------------
        // PREMIUM DARK GREEN / GOLD BACKGROUND
        // -----------------------------------------------------
        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable
                                .Orientation.TOP_BOTTOM,
                        new int[]{
                                android.graphics.Color.argb(
                                        242,
                                        7,
                                        28,
                                        21
                                ),
                                android.graphics.Color.argb(
                                        242,
                                        3,
                                        15,
                                        12
                                )
                        }
                );

        bg.setStroke(
                dp(1),
                android.graphics.Color.rgb(
                        205,
                        154,
                        64
                )
        );

        bg.setCornerRadius(
                dp(13)
        );

        card.setBackground(bg);


        // -----------------------------------------------------
        // LEFT EMBLEM
        // -----------------------------------------------------
        android.widget.TextView emblem =
                new android.widget.TextView(this);

        emblem.setText(iconText);
        emblem.setTextSize(32);

        emblem.setTextColor(
                android.graphics.Color.rgb(
                        236,
                        181,
                        76
                )
        );

        emblem.setGravity(
                android.view.Gravity.CENTER
        );


        android.graphics.drawable.GradientDrawable emblemBg =
                new android.graphics.drawable.GradientDrawable();

        emblemBg.setShape(
                android.graphics.drawable.GradientDrawable.OVAL
        );

        emblemBg.setColor(
                android.graphics.Color.argb(
                        235,
                        4,
                        14,
                        11
                )
        );

        emblemBg.setStroke(
                dp(3),
                android.graphics.Color.rgb(
                        209,
                        158,
                        66
                )
        );

        emblem.setBackground(emblemBg);

        card.addView(
                emblem,
                new android.widget.LinearLayout.LayoutParams(
                        dp(62),
                        dp(62)
                )
        );


        // -----------------------------------------------------
        // TEXT
        // -----------------------------------------------------
        android.widget.LinearLayout textBox =
                new android.widget.LinearLayout(this);

        textBox.setOrientation(
                android.widget.LinearLayout.VERTICAL
        );

        textBox.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        textBox.setPadding(
                dp(13),
                0,
                dp(7),
                0
        );


        android.widget.TextView title =
                new android.widget.TextView(this);

        title.setText(titleText);

        title.setTextColor(
                android.graphics.Color.rgb(
                        255,
                        216,
                        132
                )
        );

        title.setTextSize(17);

        title.setTypeface(
                android.graphics.Typeface.SERIF,
                android.graphics.Typeface.BOLD
        );


        android.widget.TextView description =
                new android.widget.TextView(this);

        description.setText(descriptionText);

        description.setTextColor(
                android.graphics.Color.rgb(
                        201,
                        198,
                        181
                )
        );

        description.setTextSize(11);

        description.setMaxLines(2);

        description.setPadding(
                0,
                dp(3),
                0,
                0
        );


        textBox.addView(
                title,
                new android.widget.LinearLayout.LayoutParams(
                        MATCH,
                        WRAP
                )
        );

        textBox.addView(
                description,
                new android.widget.LinearLayout.LayoutParams(
                        MATCH,
                        WRAP
                )
        );


        card.addView(
                textBox,
                new android.widget.LinearLayout.LayoutParams(
                        0,
                        WRAP,
                        1f
                )
        );


        // -----------------------------------------------------
        // RIGHT ICON + ARROW
        // -----------------------------------------------------
        android.widget.LinearLayout right =
                new android.widget.LinearLayout(this);

        right.setOrientation(
                android.widget.LinearLayout.HORIZONTAL
        );

        right.setGravity(
                android.view.Gravity.CENTER
        );


        android.widget.TextView reward =
                new android.widget.TextView(this);

        reward.setText(rightIconText);
        reward.setTextSize(21);

        reward.setGravity(
                android.view.Gravity.CENTER
        );

        reward.setTextColor(
                android.graphics.Color.rgb(
                        236,
                        182,
                        76
                )
        );


        android.widget.TextView arrow =
                new android.widget.TextView(this);

        arrow.setText("›");
        arrow.setTextSize(29);

        arrow.setGravity(
                android.view.Gravity.CENTER
        );

        arrow.setTextColor(
                android.graphics.Color.rgb(
                        221,
                        169,
                        72
                )
        );


        right.addView(
                reward,
                new android.widget.LinearLayout.LayoutParams(
                        dp(34),
                        dp(55)
                )
        );

        right.addView(
                arrow,
                new android.widget.LinearLayout.LayoutParams(
                        dp(25),
                        dp(55)
                )
        );


        card.addView(
                right,
                new android.widget.LinearLayout.LayoutParams(
                        dp(59),
                        dp(62)
                )
        );

        return card;
    }


    // =========================================================
    // MODE CARD LAYOUT
    // =========================================================
    private android.widget.LinearLayout.LayoutParams gameModeCardParams() {

        android.widget.LinearLayout.LayoutParams lp =
                new android.widget.LinearLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                );

        lp.topMargin = dp(9);
        return lp;
    }


    // =========================================================
    // SAVE SELECTED MODE
    // =========================================================
    private void saveSelectedGameMode(
            String id,
            String name,
            String icon) {

        getSharedPreferences(
                "durak_arena_game",
                MODE_PRIVATE
        )
                .edit()
                .putString(
                        "selected_mode_id",
                        id
                )
                .putString(
                        "selected_mode_name",
                        name
                )
                .putString(
                        "selected_mode_icon",
                        icon
                )
                .apply();
    }


    private android.graphics.drawable.GradientDrawable makeGoldPanel() {
        android.graphics.drawable.GradientDrawable d =
                new android.graphics.drawable.GradientDrawable();
        d.setColor(android.graphics.Color.argb(220, 5, 13, 10));
        d.setStroke(dp(1), android.graphics.Color.rgb(184, 137, 62));
        d.setCornerRadius(dp(8));
        return d;
    }

    private android.graphics.drawable.GradientDrawable makePlayButton() {
        android.graphics.drawable.GradientDrawable d =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{
                                android.graphics.Color.rgb(18, 69, 49),
                                android.graphics.Color.rgb(3, 25, 18)
                        });
        d.setStroke(dp(3), android.graphics.Color.rgb(213, 165, 72));
        d.setCornerRadius(dp(38));
        return d;
    }



    // =========================================================
    // DURAK ARENA — PROFILE
    // =========================================================
    private void showProfileScreen() {
        currentScreen = "profile";

        final int MATCH = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        final int WRAP = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
        final android.content.SharedPreferences profilePrefs =
                getSharedPreferences("durak_arena_profile", MODE_PRIVATE);

        String playerName = profilePrefs.getString("player_name", "ГРАВЕЦЬ");
        String playerId = profilePrefs.getString("player_id", "");
        String playerPhotoUrl = profilePrefs.getString("player_photo_url", "");
        int playerLevel = profilePrefs.getInt("player_level", 1);
        int playerXp = profilePrefs.getInt("player_xp", 0);
        int playerCups = profilePrefs.getInt("player_cups", 0);
        int playerWins = profilePrefs.getInt("player_wins", 0);
        int playerLosses = profilePrefs.getInt("player_losses", 0);
        int playerDraws = profilePrefs.getInt("player_draws", 0);
        int playerMedals = profilePrefs.getInt("player_medals", 0);
        int winStreak = profilePrefs.getInt("player_win_streak", 0);
        String clanName = profilePrefs.getString("player_clan", "БЕЗ КЛАНУ");
        int bestSeasonCups = profilePrefs.getInt("best_season_cups", playerCups);

        FirebaseUser profileFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (profileFirebaseUser != null) {
            String accountIdKey = "player_id_" + profileFirebaseUser.getUid();
            String accountPlayerId = profilePrefs.getString(accountIdKey, "");
            if (accountPlayerId != null && !accountPlayerId.trim().isEmpty()) {
                playerId = accountPlayerId;
                profilePrefs.edit().putString("player_id", playerId).apply();
            }
        }
        if (playerId == null || playerId.trim().isEmpty()) {
            String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            java.util.Random random = new java.util.Random();
            StringBuilder generatedId = new StringBuilder("#");
            for (int i = 0; i < 8; i++) generatedId.append(chars.charAt(random.nextInt(chars.length())));
            playerId = generatedId.toString();
            profilePrefs.edit().putString("player_id", playerId).apply();
        }

        final String rankName = profileRankName(playerCups);
        final int rankStart = profileRankStart(playerCups);
        final int rankEnd = profileRankEnd(playerCups);
        final int leagueProgress = Math.max(0, Math.min(playerCups - rankStart, rankEnd - rankStart));
        final int leagueMax = Math.max(1, rankEnd - rankStart);
        final int xpNeed = Math.max(100, 500 + (playerLevel - 1) * 100);
        final int totalGames = playerWins + playerLosses + playerDraws;
        final int winRate = totalGames == 0 ? 0 : Math.round((playerWins * 100f) / totalGames);

        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundColor(android.graphics.Color.rgb(3, 8, 7));

        android.widget.ImageView bg = new android.widget.ImageView(this);
        bg.setImageResource(R.drawable.main_menu_background);
        bg.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        root.addView(bg, new android.widget.FrameLayout.LayoutParams(MATCH, MATCH));
        android.view.View shade = new android.view.View(this);
        shade.setBackgroundColor(android.graphics.Color.argb(205, 0, 4, 3));
        root.addView(shade, new android.widget.FrameLayout.LayoutParams(MATCH, MATCH));

        // Fixed top bar
        android.widget.LinearLayout top = new android.widget.LinearLayout(this);
        top.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        top.setGravity(android.view.Gravity.CENTER_VERTICAL);
        top.setPadding(dp(12), dp(8), dp(12), dp(8));
        top.setBackground(profilePanelBg(245, 8));

        android.widget.TextView back = profileText("‹", 34, true, android.view.Gravity.CENTER);
        back.setClickable(true);
        back.setOnClickListener(v -> showMainMenu());
        top.addView(back, new android.widget.LinearLayout.LayoutParams(dp(48), dp(48)));
        android.widget.TextView title = profileText("DURAK ARENA   •   ПРОФІЛЬ", 19, true, android.view.Gravity.CENTER);
        top.addView(title, new android.widget.LinearLayout.LayoutParams(0, dp(48), 1f));
        android.widget.TextView levelTop = profileText("LV " + playerLevel, 12, true, android.view.Gravity.CENTER);
        top.addView(levelTop, new android.widget.LinearLayout.LayoutParams(dp(52), dp(48)));
        android.widget.FrameLayout.LayoutParams topLp = new android.widget.FrameLayout.LayoutParams(MATCH, dp(64));
        topLp.gravity = android.view.Gravity.TOP;
        root.addView(top, topLp);

        // Fixed bottom navigation
        android.widget.LinearLayout bottom = new android.widget.LinearLayout(this);
        bottom.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        bottom.setGravity(android.view.Gravity.CENTER);
        bottom.setBackground(profilePanelBg(250, 0));
        String[] nav = {"МЕНЮ", "ГРА", "ПРОФІЛЬ", "МЕДАЛІ", "РЕЙТИНГ"};
        for (int i = 0; i < nav.length; i++) {
            final int index = i;
            android.widget.TextView item = profileText(nav[i], 10, i == 2, android.view.Gravity.CENTER);
            if (i == 2) item.setBackground(profileSelectedBg());
            item.setClickable(true);
            item.setOnClickListener(v -> {
                if (index == 0) showMainMenu();
                else if (index == 1) showGameModesScreen();
                else if (index == 2) showProfileScreen();
                else if (index == 3) showProfileSubScreen("МЕДАЛІ", "Ваші нагороди Durak Arena", "profile");
                else showProfileSubScreen("РЕЙТИНГ", "Список лідерів Durak Arena", "profile");
            });
            bottom.addView(item, new android.widget.LinearLayout.LayoutParams(0, MATCH, 1f));
        }
        android.widget.FrameLayout.LayoutParams bottomLp = new android.widget.FrameLayout.LayoutParams(MATCH, dp(66));
        bottomLp.gravity = android.view.Gravity.BOTTOM;
        root.addView(bottom, bottomLp);

        // Only the center content scrolls
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        android.widget.LinearLayout page = new android.widget.LinearLayout(this);
        page.setOrientation(android.widget.LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(12), dp(12), dp(26));

        // Player identity card
        android.widget.LinearLayout identity = new android.widget.LinearLayout(this);
        identity.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        identity.setGravity(android.view.Gravity.CENTER_VERTICAL);
        identity.setPadding(dp(14), dp(14), dp(14), dp(14));
        identity.setBackground(profilePanelBg(238, 16));

        android.widget.TextView avatar = profileText("DA", 20, true, android.view.Gravity.CENTER);
        avatar.setBackground(profileCircleBg());
        loadGoogleAvatar(avatar, playerPhotoUrl, 76);
        identity.addView(avatar, new android.widget.LinearLayout.LayoutParams(dp(82), dp(82)));

        android.widget.LinearLayout info = new android.widget.LinearLayout(this);
        info.setOrientation(android.widget.LinearLayout.VERTICAL);
        info.setPadding(dp(14), 0, dp(8), 0);
        info.addView(profileText(playerName, 21, true, android.view.Gravity.LEFT));
        info.addView(profileMutedText(playerId, 13));
        info.addView(profileMutedText(clanName, 12));

        android.widget.ProgressBar xpBar = profileProgress(playerXp, xpNeed);
        android.widget.LinearLayout.LayoutParams xpLp = new android.widget.LinearLayout.LayoutParams(MATCH, dp(9));
        xpLp.topMargin = dp(8);
        info.addView(xpBar, xpLp);
        info.addView(profileMutedText(playerXp + " / " + xpNeed + " XP", 11));
        identity.addView(info, new android.widget.LinearLayout.LayoutParams(0, WRAP, 1f));

        android.widget.LinearLayout rankBox = new android.widget.LinearLayout(this);
        rankBox.setOrientation(android.widget.LinearLayout.VERTICAL);
        rankBox.setGravity(android.view.Gravity.CENTER);
        android.widget.ImageView rankIcon = profileAsset("rank_" + profileRankAssetSuffix(playerCups), dp(62));
        rankBox.addView(rankIcon, new android.widget.LinearLayout.LayoutParams(dp(62), dp(62)));
        rankBox.addView(profileText(rankName, 10, true, android.view.Gravity.CENTER));
        identity.addView(rankBox, new android.widget.LinearLayout.LayoutParams(dp(78), WRAP));
        page.addView(identity, profileSectionParams());

        // Cups + streak
        android.widget.LinearLayout quick = new android.widget.LinearLayout(this);
        quick.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        android.widget.TextView cupsCard = makeProfileSection("КУБКИ", String.valueOf(playerCups));
        android.widget.TextView streakCard = makeProfileSection("СЕРІЯ ПЕРЕМОГ", String.valueOf(winStreak));
        android.widget.LinearLayout.LayoutParams half1 = new android.widget.LinearLayout.LayoutParams(0, dp(88), 1f);
        half1.rightMargin = dp(5);
        android.widget.LinearLayout.LayoutParams half2 = new android.widget.LinearLayout.LayoutParams(0, dp(88), 1f);
        half2.leftMargin = dp(5);
        quick.addView(cupsCard, half1); quick.addView(streakCard, half2);
        page.addView(quick, profileSectionParams());

        // Clan
        android.widget.TextView clan = makeProfileSection("КЛАН", clanName + "\nНатисніть, щоб відкрити клан");
        clan.setClickable(true);
        clan.setOnClickListener(v -> showProfileSubScreen("КЛАН", clanName, "profile"));
        page.addView(clan, profileSectionParams());

        // Statistics
        android.widget.TextView stats = makeProfileSection("СТАТИСТИКА",
                "Перемоги  " + playerWins + "     Поразки  " + playerLosses +
                "\nМатчі  " + totalGames + "     Відсоток перемог  " + winRate + "%");
        page.addView(stats, profileSectionParams());

        // League path + calculated progress
        android.widget.LinearLayout league = profilePanelLayout();
        league.addView(profileText("ШЛЯХ ЛІГИ", 16, true, android.view.Gravity.LEFT));
        league.addView(profileMutedText(rankName + "   •   " + playerCups + " кубків", 13));
        android.widget.ProgressBar leagueBar = profileProgress(leagueProgress, leagueMax);
        android.widget.LinearLayout.LayoutParams leagueBarLp = new android.widget.LinearLayout.LayoutParams(MATCH, dp(11));
        leagueBarLp.topMargin = dp(10); leagueBarLp.bottomMargin = dp(6);
        league.addView(leagueBar, leagueBarLp);
        league.addView(profileMutedText("Наступна межа: " + rankEnd + " кубків", 11));
        page.addView(league, profileSectionParams());

        // Medals: independent asset slots, never a screenshot
        android.widget.LinearLayout medalsPanel = profilePanelLayout();
        medalsPanel.addView(profileText("МЕДАЛІ   •   " + playerMedals, 16, true, android.view.Gravity.LEFT));
        android.widget.LinearLayout medalRow = new android.widget.LinearLayout(this);
        medalRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        String[] medalAssets = {"medal_first_win", "medal_streak_10", "medal_matches_100", "medal_master"};
        for (String asset : medalAssets) {
            android.widget.ImageView medal = profileAsset(asset, dp(56));
            medalRow.addView(medal, new android.widget.LinearLayout.LayoutParams(0, dp(62), 1f));
        }
        medalsPanel.addView(medalRow, new android.widget.LinearLayout.LayoutParams(MATCH, dp(66)));
        medalsPanel.setClickable(true);
        medalsPanel.setOnClickListener(v -> showProfileSubScreen("МЕДАЛІ", "Колекція нагород гравця", "profile"));
        page.addView(medalsPanel, profileSectionParams());

        android.widget.TextView bestSeason = makeProfileSection("НАЙКРАЩИЙ МИНУЛИЙ СЕЗОН",
                profileRankName(bestSeasonCups) + "   •   " + bestSeasonCups + " кубків");
        page.addView(bestSeason, profileSectionParams());

        page.addView(profileMenuButton("ІСТОРІЯ МАТЧІВ", v -> showProfileSubScreen("ІСТОРІЯ МАТЧІВ", "Перемоги, поразки та зіграні матчі", "profile")), profileSectionParams());
        page.addView(profileMenuButton("СПИСОК ЛІДЕРІВ", v -> showProfileSubScreen("РЕЙТИНГ", "Список лідерів Durak Arena", "profile")), profileSectionParams());
        page.addView(profileMenuButton("НАЛАШТУВАННЯ", v -> showSettingsScreen()), profileSectionParams());
        page.addView(profileMenuButton("ПІДТРИМКА", v -> showSupportScreen()), profileSectionParams());
        page.addView(profileMenuButton("АВТОР ГРИ", v -> showAuthorsScreen()), profileSectionParams());
        page.addView(profileMenuButton("РЕГІОН", v -> showProfileSubScreen("РЕГІОН", "Україна", "profile")), profileSectionParams());

        scroll.addView(page);
        android.widget.FrameLayout.LayoutParams scrollLp = new android.widget.FrameLayout.LayoutParams(MATCH, MATCH);
        scrollLp.topMargin = dp(64);
        scrollLp.bottomMargin = dp(66);
        root.addView(scroll, scrollLp);
        setContentView(root);
    }

    private android.graphics.drawable.GradientDrawable profilePanelBg(int alpha, int radius) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor(android.graphics.Color.argb(alpha, 3, 14, 11));
        g.setStroke(dp(1), android.graphics.Color.rgb(205, 154, 63));
        g.setCornerRadius(dp(radius));
        return g;
    }

    private android.graphics.drawable.GradientDrawable profileSelectedBg() {
        android.graphics.drawable.GradientDrawable g = profilePanelBg(255, 10);
        g.setStroke(dp(2), android.graphics.Color.rgb(242, 190, 82));
        return g;
    }

    private android.graphics.drawable.GradientDrawable profileCircleBg() {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        g.setColor(android.graphics.Color.rgb(7, 18, 15));
        g.setStroke(dp(2), android.graphics.Color.rgb(225, 174, 76));
        return g;
    }

    private android.widget.TextView profileText(String text, int size, boolean bold, int gravity) {
        android.widget.TextView v = new android.widget.TextView(this);
        v.setText(text); v.setTextSize(size); v.setGravity(gravity);
        v.setTextColor(android.graphics.Color.rgb(255, 218, 145));
        v.setTypeface(android.graphics.Typeface.create("sans-serif", bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
        return v;
    }

    private android.widget.TextView profileMutedText(String text, int size) {
        android.widget.TextView v = profileText(text, size, false, android.view.Gravity.LEFT);
        v.setTextColor(android.graphics.Color.rgb(195, 198, 188));
        return v;
    }

    private android.widget.LinearLayout profilePanelLayout() {
        android.widget.LinearLayout panel = new android.widget.LinearLayout(this);
        panel.setOrientation(android.widget.LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(14), dp(16), dp(14));
        panel.setBackground(profilePanelBg(238, 14));
        return panel;
    }

    private android.widget.ProgressBar profileProgress(int value, int max) {
        android.widget.ProgressBar p = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        p.setMax(Math.max(1, max)); p.setProgress(Math.max(0, Math.min(value, Math.max(1, max))));
        android.graphics.drawable.GradientDrawable track = new android.graphics.drawable.GradientDrawable();
        track.setColor(android.graphics.Color.rgb(18, 24, 22)); track.setCornerRadius(dp(8));
        android.graphics.drawable.GradientDrawable fill = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{android.graphics.Color.rgb(142, 91, 22), android.graphics.Color.rgb(245, 194, 78)});
        fill.setCornerRadius(dp(8));
        android.graphics.drawable.ClipDrawable clip = new android.graphics.drawable.ClipDrawable(fill, android.view.Gravity.LEFT, android.graphics.drawable.ClipDrawable.HORIZONTAL);
        android.graphics.drawable.LayerDrawable layers = new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{track, clip});
        layers.setId(0, android.R.id.background); layers.setId(1, android.R.id.progress); p.setProgressDrawable(layers);
        return p;
    }

    private android.widget.ImageView profileAsset(String drawableName, int sizePx) {
        android.widget.ImageView image = new android.widget.ImageView(this);
        image.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        image.setPadding(dp(4), dp(4), dp(4), dp(4));
        int id = getResources().getIdentifier(drawableName, "drawable", getPackageName());
        if (id != 0) image.setImageResource(id);
        else image.setBackground(profileCircleBg());
        image.setContentDescription(drawableName);
        return image;
    }

    private android.widget.TextView profileMenuButton(String text, android.view.View.OnClickListener listener) {
        android.widget.TextView v = profileText(text + "     ›", 14, true, android.view.Gravity.CENTER_VERTICAL);
        v.setPadding(dp(16), 0, dp(16), 0); v.setBackground(profilePanelBg(242, 11));
        v.setClickable(true); v.setFocusable(true); v.setOnClickListener(listener);
        return v;
    }

    private android.widget.TextView makeProfileSection(String title, String value) {
        android.widget.TextView view = profileText(title + "\n" + value, 14, false, android.view.Gravity.CENTER_VERTICAL);
        view.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL);
        view.setPadding(dp(16), dp(12), dp(16), dp(12));
        view.setBackground(profilePanelBg(238, 14));
        return view;
    }

    private android.widget.LinearLayout.LayoutParams profileSectionParams() {
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10); return lp;
    }

    private String profileRankName(int cups) {
        if (cups >= 5000) return "ЛЕГЕНДА";
        if (cups >= 3500) return "ЕЛІТА";
        if (cups >= 2200) return "МАЙСТЕР";
        if (cups >= 1200) return "ПРОФІ";
        if (cups >= 500) return "ГРАВЕЦЬ";
        return "НОВАЧОК";
    }

    private String profileRankAssetSuffix(int cups) {
        if (cups >= 5000) return "legend";
        if (cups >= 3500) return "elite";
        if (cups >= 2200) return "master";
        if (cups >= 1200) return "pro";
        if (cups >= 500) return "player";
        return "novice";
    }

    private int profileRankStart(int cups) {
        if (cups >= 5000) return 5000;
        if (cups >= 3500) return 3500;
        if (cups >= 2200) return 2200;
        if (cups >= 1200) return 1200;
        if (cups >= 500) return 500;
        return 0;
    }

    private int profileRankEnd(int cups) {
        if (cups >= 5000) return 6500;
        if (cups >= 3500) return 5000;
        if (cups >= 2200) return 3500;
        if (cups >= 1200) return 2200;
        if (cups >= 500) return 1200;
        return 500;
    }

    private void showProfileSubScreen(String titleText, String bodyText, String backTarget) {
        currentScreen = "profile_sub";
        android.widget.FrameLayout root = makeBackgroundRoot(true);
        android.widget.LinearLayout panel = profilePanelLayout();
        panel.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        android.widget.TextView title = profileText(titleText, 24, true, android.view.Gravity.CENTER);
        android.widget.TextView body = profileMutedText(bodyText, 15); body.setGravity(android.view.Gravity.CENTER);
        android.widget.TextView back = profileMenuButton("НАЗАД", v -> showProfileScreen());
        panel.addView(title, new android.widget.LinearLayout.LayoutParams(-1, dp(60)));
        panel.addView(body, new android.widget.LinearLayout.LayoutParams(-1, dp(120)));
        panel.addView(back, new android.widget.LinearLayout.LayoutParams(-1, dp(56)));
        android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(-1, -2);
        lp.gravity = android.view.Gravity.CENTER; lp.leftMargin = dp(18); lp.rightMargin = dp(18);
        root.addView(panel, lp); setContentView(root);
    }

    
    private void showGloryPathScreen() {
        currentScreen = "glory_path";

        android.content.SharedPreferences prefs =
                getSharedPreferences("durak_arena_profile", MODE_PRIVATE);

        int cups = prefs.getInt("player_cups", 0);

        setContentView(
                new GloryPathView(
                        this,
                        cups,
                        () -> showMainMenu()
                )
        );
    }

    private void showSettingsScreen() {
        currentScreen = "settings";

        FrameLayout root = makeBackgroundRoot(true);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(28), dp(18), dp(36));

        Button back = actionButton("‹", false);
        back.setTextSize(28);
        back.setOnClickListener(v -> showLoginScreen());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(62), dp(56));
        content.addView(back, backParams);

        TextView title = label(tr("НАЛАШТУВАННЯ", "SETTINGS", "EINSTELLUNGEN", "AJUSTES"), 30, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, dp(10), 0, dp(16));
        content.addView(title, titleParams);

        content.addView(makeLanguagePanel());
        content.addView(makeRegionPanel());
        content.addView(makeVolumePanel(
                tr("Звук", "Sound", "Ton", "Sonido"),
                "sound", 75
        ));
        content.addView(makeVolumePanel(
                tr("Музика", "Music", "Musik", "Música"),
                "music", 70
        ));

        Button support = actionButton("🎧  " + tr("Підтримка", "Support", "Support", "Soporte") + "   ›", false);
        support.setOnClickListener(v -> showSupportScreen());
        addWithMargins(content, support, 0, 8, 0, 8, 64);

        Button authors = actionButton("ⓘ  " + tr("Автори гри", "Game authors", "Spielautoren", "Autores del juego") + "   ›", false);
        authors.setOnClickListener(v -> showAuthorsScreen());
        addWithMargins(content, authors, 0, 0, 0, 8, 64);

        TextView note = label(tr(
                "Регіон збережено для майбутнього підбору суперників. Значення звуку та музики також зберігаються.",
                "Region is saved for future matchmaking. Sound and music values are also saved.",
                "Die Region wird für die zukünftige Spielersuche gespeichert. Ton- und Musikwerte werden ebenfalls gespeichert.",
                "La región se guarda para el futuro emparejamiento. También se guardan los valores de sonido y música."
        ), 13, false);
        note.setGravity(Gravity.CENTER);
        content.addView(note);

        scroll.addView(content);
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(root);
    }

    private View makeLanguagePanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(10), dp(10), dp(10));
        panel.setBackground(box(Color.argb(225, 12, 10, 9), GOLD, 2, 12));

        TextView heading = label("🌐  " + tr("Мова", "Language", "Sprache", "Idioma"), 21, true);
        panel.addView(heading);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        String[] codes = {"en", "de", "uk", "es"};
        String[] names = {"English", "Deutsch", "Українська", "Español"};
        String active = prefs.getString("language", "uk");

        for (int i = 0; i < codes.length; i++) {
            final String code = codes[i];
            Button b = actionButton(names[i], code.equals(active));
            b.setTextSize(13);
            b.setOnClickListener(v -> {
                prefs.edit().putString("language", code).apply();
                showSettingsScreen();
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(54), 1f);
            p.setMargins(dp(3), dp(4), dp(3), dp(4));
            row.addView(b, p);
        }
        panel.addView(row);

        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        outer.bottomMargin = dp(10);
        panel.setLayoutParams(outer);
        return panel;
    }

    private View makeRegionPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(10), dp(10), dp(10));
        panel.setBackground(box(Color.argb(225, 12, 10, 9), GOLD, 2, 12));

        TextView heading = label("📍  " + tr("Регіон", "Region", "Region", "Región"), 21, true);
        panel.addView(heading);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        String[] codes = {"EN", "DE", "UA", "ES"};
        String[] names = {
                tr("Англія", "England", "England", "Inglaterra"),
                tr("Німеччина", "Germany", "Deutschland", "Alemania"),
                tr("Україна", "Ukraine", "Ukraine", "Ucrania"),
                tr("Іспанія", "Spain", "Spanien", "España")
        };
        String active = prefs.getString("region", "UA");

        for (int i = 0; i < codes.length; i++) {
            final String code = codes[i];
            Button b = actionButton(names[i], code.equals(active));
            b.setTextSize(12);
            b.setOnClickListener(v -> {
                prefs.edit().putString("region", code).apply();
                showSettingsScreen();
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(54), 1f);
            p.setMargins(dp(3), dp(4), dp(3), dp(4));
            row.addView(b, p);
        }
        panel.addView(row);

        TextView note = label(tr(
                "Цей вибір буде використовуватися системою підбору гравців.",
                "This selection will be used by matchmaking.",
                "Diese Auswahl wird für die Spielersuche verwendet.",
                "Esta selección se usará para el emparejamiento."
        ), 13, false);
        panel.addView(note);

        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        outer.bottomMargin = dp(10);
        panel.setLayoutParams(outer);
        return panel;
    }

    private View makeVolumePanel(String title, String key, int defaultValue) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(8), dp(10), dp(8));
        panel.setBackground(box(Color.argb(225, 12, 10, 9), GOLD, 2, 12));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView heading = label((key.equals("music") ? "♫  " : "🔊  ") + title, 21, true);
        TextView value = label("", 16, true);
        value.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        header.addView(heading, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(value, new LinearLayout.LayoutParams(dp(70), ViewGroup.LayoutParams.WRAP_CONTENT));
        panel.addView(header);

        SeekBar bar = new SeekBar(this);
        bar.setMax(100);
        int current = prefs.getInt(key, defaultValue);
        bar.setProgress(current);
        value.setText(current + "%");
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value.setText(progress + "%");
                prefs.edit().putInt(key, progress).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        panel.addView(bar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        outer.bottomMargin = dp(10);
        panel.setLayoutParams(outer);
        return panel;
    }

    private void showSupportScreen() {
        currentScreen = "support";

        FrameLayout root = makeBackgroundRoot(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(22), dp(14), dp(18));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        Button back = actionButton("‹", false);
        back.setTextSize(28);
        back.setOnClickListener(v -> showSettingsScreen());
        top.addView(back, new LinearLayout.LayoutParams(dp(58), dp(54)));

        TextView title = label(tr("ПІДТРИМКА", "SUPPORT", "SUPPORT", "SOPORTE"), 28, true);
        title.setGravity(Gravity.CENTER);
        top.addView(title, new LinearLayout.LayoutParams(0, dp(58), 1f));
        page.addView(top);

        TextView botTitle = label("🤖  " + tr(
                "Бот підтримки Durak Arena",
                "Durak Arena Support Bot",
                "Durak Arena Support-Bot",
                "Bot de soporte Durak Arena"
        ), 20, true);
        botTitle.setBackground(box(Color.argb(230, 12, 14, 16), GOLD, 2, 12));
        page.addView(botTitle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.VERTICAL);
        quick.setPadding(0, dp(8), 0, dp(4));

        addQuickSupportButton(quick, "🔐 " + tr("Проблема з входом?", "Login problem?", "Anmeldeproblem?", "¿Problema de acceso?"), "login");
        addQuickSupportButton(quick, "🛒 " + tr("Не зарахувалась покупка?", "Purchase not credited?", "Kauf nicht gutgeschrieben?", "¿Compra no acreditada?"), "purchase");
        addQuickSupportButton(quick, "💳 " + tr("Проблема з оплатою?", "Payment problem?", "Zahlungsproblem?", "¿Problema de pago?"), "payment");
        addQuickSupportButton(quick, "🎮 " + tr("Гра не відкривається?", "Game won't open?", "Spiel startet nicht?", "¿El juego no abre?"), "open");
        page.addView(quick);

        supportScroll = new ScrollView(this);
        supportScroll.setFillViewport(true);
        supportChatLog = new TextView(this);
        supportChatLog.setTextColor(Color.WHITE);
        supportChatLog.setTextSize(15);
        supportChatLog.setPadding(dp(12), dp(12), dp(12), dp(12));
        supportChatLog.setBackground(box(Color.argb(235, 7, 10, 13), GOLD, 2, 12));
        supportChatLog.setText(tr(
                "🤖 Вітаю! Я бот підтримки Durak Arena. Оберіть проблему вище або напишіть своє питання нижче.\n",
                "🤖 Hello! I am the Durak Arena support bot. Choose a problem above or type your question below.\n",
                "🤖 Hallo! Ich bin der Durak Arena Support-Bot. Wähle oben ein Problem oder schreibe unten deine Frage.\n",
                "🤖 ¡Hola! Soy el bot de soporte de Durak Arena. Elige un problema arriba o escribe tu pregunta abajo.\n"
        ));
        supportScroll.addView(supportChatLog);
        page.addView(supportScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(0, dp(8), 0, 0);

        supportInput = new EditText(this);
        supportInput.setHint(tr("Опишіть вашу проблему...", "Describe your problem...", "Beschreibe dein Problem...", "Describe tu problema..."));
        supportInput.setTextColor(Color.WHITE);
        supportInput.setHintTextColor(Color.rgb(165, 165, 165));
        supportInput.setTextSize(15);
        supportInput.setSingleLine(false);
        supportInput.setMaxLines(3);
        supportInput.setBackground(box(Color.argb(240, 8, 10, 12), GOLD, 2, 10));
        supportInput.setPadding(dp(12), dp(8), dp(12), dp(8));

        Button send = actionButton("➤", true);
        send.setTextSize(24);
        send.setOnClickListener(v -> {
            String text = supportInput.getText().toString().trim();
            if (!text.isEmpty()) {
                sendSupportMessage(text);
                supportInput.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(supportInput.getWindowToken(), 0);
            }
        });

        inputRow.addView(supportInput, new LinearLayout.LayoutParams(0, dp(62), 1f));
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(dp(70), dp(62));
        sendParams.leftMargin = dp(8);
        inputRow.addView(send, sendParams);
        page.addView(inputRow);

        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(root);
    }

    private void addQuickSupportButton(LinearLayout parent, String text, String type) {
        Button b = actionButton(text + "   ›", true);
        b.setTextSize(14);
        b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        b.setOnClickListener(v -> quickSupport(type));
        addWithMargins(parent, b, 0, 3, 0, 3, 55);
    }

    private void quickSupport(String type) {
        String userText;
        switch (type) {
            case "login":
                userText = tr("Проблема з входом", "Login problem", "Anmeldeproblem", "Problema de acceso");
                break;
            case "purchase":
                userText = tr("Не зарахувалась покупка", "Purchase not credited", "Kauf nicht gutgeschrieben", "Compra no acreditada");
                break;
            case "payment":
                userText = tr("Проблема з оплатою", "Payment problem", "Zahlungsproblem", "Problema de pago");
                break;
            default:
                userText = tr("Гра не відкривається", "Game won't open", "Spiel startet nicht", "El juego no abre");
                break;
        }
        appendChat("👤 " + userText);
        appendChat("🤖 " + supportReply(type));
    }

    private void sendSupportMessage(String text) {
        appendChat("👤 " + text);
        String lower = text.toLowerCase(Locale.ROOT);
        String type = "other";
        if (lower.contains("вхід") || lower.contains("login") || lower.contains("anmeld") || lower.contains("acceso")) {
            type = "login";
        } else if (lower.contains("покуп") || lower.contains("purchase") || lower.contains("kauf") || lower.contains("compra")) {
            type = "purchase";
        } else if (lower.contains("оплат") || lower.contains("payment") || lower.contains("zahl") || lower.contains("pago")) {
            type = "payment";
        } else if (lower.contains("не відк") || lower.contains("crash") || lower.contains("open") || lower.contains("start") || lower.contains("abre")) {
            type = "open";
        }
        appendChat("🤖 " + supportReply(type));
    }

    private String supportReply(String type) {
        switch (type) {
            case "login":
                return tr(
                        "Спробуйте: 1) перевірити інтернет; 2) переконатися, що Google Play Services оновлені; 3) перезапустити гру; 4) повторити Google-вхід. Якщо не допоможе — напишіть код помилки, модель телефону та версію Android.",
                        "Try: 1) check your internet; 2) make sure Google Play Services are updated; 3) restart the game; 4) try Google sign-in again. If it still fails, send the error code, phone model and Android version.",
                        "Versuche: 1) Internet prüfen; 2) Google Play-Dienste aktualisieren; 3) Spiel neu starten; 4) Google-Anmeldung erneut versuchen. Falls es nicht klappt, sende Fehlercode, Handymodell und Android-Version.",
                        "Prueba: 1) revisar Internet; 2) actualizar Google Play Services; 3) reiniciar el juego; 4) intentar de nuevo el acceso con Google. Si falla, envía el código de error, modelo del teléfono y versión de Android."
                );
            case "purchase":
                return tr(
                        "Якщо покупка оплачена, але не зарахована: перевірте історію покупок Google Play, перезапустіть гру та зачекайте кілька хвилин. Для перевірки збережіть номер замовлення Google Play, дату, час і суму. Не надсилайте номер банківської картки, CVV або пароль.",
                        "If a paid purchase was not credited: check Google Play purchase history, restart the game and wait a few minutes. Keep the Google Play order number, date, time and amount for verification. Never send your card number, CVV or password.",
                        "Wenn ein bezahlter Kauf nicht gutgeschrieben wurde: prüfe den Google-Play-Kaufverlauf, starte das Spiel neu und warte einige Minuten. Halte Bestellnummer, Datum, Uhrzeit und Betrag bereit. Sende niemals Kartennummer, CVV oder Passwort.",
                        "Si una compra pagada no se acreditó: revisa el historial de Google Play, reinicia el juego y espera unos minutos. Guarda el número de pedido, fecha, hora e importe. Nunca envíes número de tarjeta, CVV ni contraseña."
                );
            case "payment":
                return tr(
                        "Перевірте спосіб оплати в Google Play, баланс/ліміти та країну платіжного профілю. Спробуйте ще раз лише після того, як попередня операція має статус завершено або відхилено. Не вводьте платіжні реквізити в чат підтримки.",
                        "Check your Google Play payment method, balance/limits and payment profile country. Retry only after the previous transaction is completed or declined. Do not enter payment credentials in support chat.",
                        "Prüfe Zahlungsmethode, Guthaben/Limits und Land des Google-Play-Zahlungsprofils. Versuche es erst erneut, wenn die vorherige Transaktion abgeschlossen oder abgelehnt wurde. Gib keine Zahlungsdaten im Support-Chat ein.",
                        "Revisa el método de pago, saldo/límites y país del perfil de Google Play. Reintenta solo cuando la transacción anterior esté completada o rechazada. No escribas datos de pago en el chat."
                );
            case "open":
                return tr(
                        "Спробуйте повністю закрити гру, перезавантажити телефон і запустити її знову. Якщо проблема лишилась — напишіть модель пристрою, версію Android і що саме бачите після запуску.",
                        "Fully close the game, restart the phone and launch it again. If the problem remains, send your device model, Android version and what you see after launch.",
                        "Schließe das Spiel vollständig, starte das Handy neu und öffne das Spiel erneut. Falls das Problem bleibt, sende Gerätemodell, Android-Version und was nach dem Start angezeigt wird.",
                        "Cierra completamente el juego, reinicia el teléfono y vuelve a abrirlo. Si el problema continúa, envía modelo del dispositivo, versión de Android y lo que aparece al iniciar."
                );
            default:
                return tr(
                        "Я можу допомогти з входом, запуском гри, оплатою та незарахованими покупками. Опишіть, що сталося, що ви натиснули перед проблемою, і чи бачите повідомлення про помилку.",
                        "I can help with sign-in, game startup, payments and missing purchases. Describe what happened, what you tapped before the problem, and whether you see an error message.",
                        "Ich kann bei Anmeldung, Spielstart, Zahlungen und fehlenden Käufen helfen. Beschreibe, was passiert ist, was du davor gedrückt hast und ob eine Fehlermeldung erscheint.",
                        "Puedo ayudar con acceso, inicio del juego, pagos y compras no acreditadas. Describe qué ocurrió, qué pulsaste antes del problema y si aparece un mensaje de error."
                );
        }
    }

    private void appendChat(String message) {
        if (supportChatLog == null) return;
        String old = supportChatLog.getText().toString();
        supportChatLog.setText(old + "\n" + message + "\n");
        if (supportScroll != null) {
            supportScroll.post(() -> supportScroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void showAuthorsScreen() {
        currentScreen = "authors";

        FrameLayout root = makeBackgroundRoot(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(20), dp(26), dp(20), dp(36));

        Button back = actionButton("‹", false);
        back.setTextSize(28);
        back.setOnClickListener(v -> showSettingsScreen());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(62), dp(56));
        backParams.gravity = Gravity.START;
        content.addView(back, backParams);

        Space s1 = new Space(this);
        content.addView(s1, new LinearLayout.LayoutParams(1, 0, 0.6f));

        TextView title = label(tr("АВТОРИ ГРИ", "GAME AUTHORS", "SPIELAUTOREN", "AUTORES DEL JUEGO"), 32, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView company = label("NEXORA\nINTERACTIVE", 30, true);
        company.setGravity(Gravity.CENTER);
        company.setBackground(box(Color.argb(235, 8, 10, 12), GOLD, 2, 14));
        LinearLayout.LayoutParams companyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(150));
        companyParams.setMargins(0, dp(20), 0, dp(14));
        content.addView(company, companyParams);

        TextView author = label(tr("Автор: ", "Author: ", "Autor: ", "Autor: ") + "Fox 🦊", 28, true);
        author.setGravity(Gravity.CENTER);
        author.setBackground(box(Color.argb(235, 8, 10, 12), GOLD, 2, 14));
        content.addView(author, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(120)));

        TextView project = label(tr("Проєкт: ", "Project: ", "Projekt: ", "Proyecto: ") + "DURAK ARENA", 22, true);
        project.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams projectParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(90));
        projectParams.setMargins(0, dp(14), 0, 0);
        content.addView(project, projectParams);

        Space s2 = new Space(this);
        content.addView(s2, new LinearLayout.LayoutParams(1, 0, 0.8f));

        root.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(root);
    }

    private void addWithMargins(LinearLayout parent, View view,
                                int left, int top, int right, int bottom, int heightDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(heightDp));
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        parent.addView(view, p);
    }

    private String lang() {
        return prefs.getString("language", "uk");
    }

    private String tr(String uk, String en, String de, String es) {
        String l = lang();
        if ("en".equals(l)) return en;
        if ("de".equals(l)) return de;
        if ("es".equals(l)) return es;
        return uk;
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this,
                        tr("Помилка Google: ", "Google error: ", "Google-Fehler: ", "Error de Google: ") + e.getStatusCode(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                    // ===== SAVE GOOGLE NAME TO DURAK ARENA PROFILE =====
                    if (user != null) {
                        String googleName = user.getDisplayName();

                        android.content.SharedPreferences profile =
                                getSharedPreferences(
                                        "durak_arena_profile",
                                        MODE_PRIVATE
                                );

                        android.content.SharedPreferences.Editor profileEditor =
                                profile.edit();

                        if (googleName != null &&
                                !googleName.trim().isEmpty()) {

                            profileEditor.putString(
                                    "player_name",
                                    googleName.trim()
                            );
                        }

                        profileEditor.putString(
                                "firebase_uid",
                                user.getUid()
                        );

                        android.net.Uri photoUri = user.getPhotoUrl();

                        if (photoUri != null) {
                            profileEditor.putString(
                                    "player_photo_url",
                                    photoUri.toString()
                            );
                        }

                        // Зберігаємо існуючий Player ID за Firebase UID.
                        String accountIdKey =
                                "player_id_" + user.getUid();

                        String accountPlayerId =
                                profile.getString(accountIdKey, "");

                        String oldPlayerId =
                                profile.getString("player_id", "");

                        if (accountPlayerId == null ||
                                accountPlayerId.trim().isEmpty()) {

                            if (oldPlayerId != null &&
                                    !oldPlayerId.trim().isEmpty()) {

                                accountPlayerId = oldPlayerId;

                            } else {

                                String chars =
                                        "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

                                java.util.Random random =
                                        new java.util.Random();

                                StringBuilder generated =
                                        new StringBuilder("#");

                                for (int i = 0; i < 8; i++) {
                                    generated.append(
                                            chars.charAt(
                                                    random.nextInt(
                                                            chars.length()
                                                    )
                                            )
                                    );
                                }

                                accountPlayerId =
                                        generated.toString();
                            }

                            profileEditor.putString(
                                    accountIdKey,
                                    accountPlayerId
                            );
                        }

                        // player_id залишається сумісним зі старим профілем.
                        profileEditor.putString(
                                "player_id",
                                accountPlayerId
                        );

                        profileEditor.apply();
                    }
                    // ==================================================

                        if (user != null) {
                            String name = user.getDisplayName();
                            if (name == null || name.trim().isEmpty()) name = "Гравець";
                            Toast.makeText(this,
                                    tr("Вхід успішний. Вітаємо, ", "Signed in. Welcome, ", "Anmeldung erfolgreich. Willkommen, ", "Acceso correcto. Bienvenido, ") + name,
                                    Toast.LENGTH_LONG).show();
                        showLoadingScreen();
                        }
                    } else {
                        Toast.makeText(this,
                                tr("Помилка Firebase", "Firebase error", "Firebase-Fehler", "Error de Firebase"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if ("support".equals(currentScreen) || "authors".equals(currentScreen)) {
            showSettingsScreen();
        } else if ("settings".equals(currentScreen)) {
            showLoginScreen();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            enableDurakFullscreen();
        }
    }



    // =========================================================
    // NEXORA: ACE ARENA — START SCREEN HELPERS
    // =========================================================

    private int aceDp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

     private void aceEnableFullscreen() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);

        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        if (android.os.Build.VERSION.SDK_INT >= 28) {
            android.view.WindowManager.LayoutParams p =
                    getWindow().getAttributes();

            p.layoutInDisplayCutoutMode =
                    android.view.WindowManager.LayoutParams
                            .LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

            getWindow().setAttributes(p);
        }
    }

    private String aceLanguageCode() {

        String[] preferenceFiles = {
                "settings",
                "app_settings",
                "durak_settings",
                "nexora_settings",
                "ace_arena_settings"
        };

        String[] keys = {
                "language",
                "lang",
                "selected_language",
                "selectedLanguage"
        };

        for (String file : preferenceFiles) {

            android.content.SharedPreferences sp =
                    getSharedPreferences(
                            file,
                            android.content.Context.MODE_PRIVATE
                    );

            for (String key : keys) {

                String value = sp.getString(key, "");

                if (value == null || value.trim().isEmpty()) {
                    continue;
                }

                String v = value.toLowerCase(java.util.Locale.ROOT);

                if (v.contains("ukr")
                        || v.contains("укра")
                        || v.equals("uk")
                        || v.equals("ua")) {
                    return "uk";
                }

                if (v.contains("deut")
                        || v.contains("нім")
                        || v.equals("de")) {
                    return "de";
                }

                if (v.contains("espa")
                        || v.contains("ісп")
                        || v.equals("es")) {
                    return "es";
                }

                if (v.contains("engl")
                        || v.contains("анг")
                        || v.equals("en")) {
                    return "en";
                }
            }
        }

        String system =
                java.util.Locale.getDefault().getLanguage();

        if ("de".equals(system)) return "de";
        if ("es".equals(system)) return "es";
        if ("en".equals(system)) return "en";

        return "uk";
    }

    private String aceLegendText() {

        switch (aceLanguageCode()) {

            case "en":
                return "BECOME A LEGEND\nIN NEXORA: ACE ARENA ♦";

            case "de":
                return "WERDE ZUR LEGENDE\nIN NEXORA: ACE ARENA ♦";

            case "es":
                return "CONVIÉRTETE EN LEYENDA\nEN NEXORA: ACE ARENA ♦";

            default:
                return "СТАНЬ ЛЕГЕНДОЮ\nВ NEXORA: ACE ARENA ♦";
        }
    }

    private String aceLoginText() {

        switch (aceLanguageCode()) {

            case "en":
                return "SIGN IN";

            case "de":
                return "ANMELDEN";

            case "es":
                return "INICIAR SESIÓN";

            default:
                return "УВІЙТИ";
        }
    }

    private android.graphics.drawable.Drawable aceHexDrawable() {

        return new android.graphics.drawable.Drawable() {

            private final android.graphics.Paint fill =
                    new android.graphics.Paint(
                            android.graphics.Paint.ANTI_ALIAS_FLAG
                    );

            private final android.graphics.Paint stroke =
                    new android.graphics.Paint(
                            android.graphics.Paint.ANTI_ALIAS_FLAG
                    );

            @Override
            public void draw(android.graphics.Canvas canvas) {

                android.graphics.Rect b = getBounds();

                float w = b.width();
                float h = b.height();

                float cx = b.left + w / 2f;
                float cy = b.top + h / 2f;

                float radius = Math.min(w, h) * 0.47f;

                android.graphics.Path path =
                        new android.graphics.Path();

                for (int i = 0; i < 6; i++) {

                    double angle = Math.toRadians(30 + i * 60);

                    float x =
                            cx + (float)Math.cos(angle) * radius;

                    float y =
                            cy + (float)Math.sin(angle) * radius;

                    if (i == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }

                path.close();

                fill.setStyle(android.graphics.Paint.Style.FILL);
                fill.setColor(Color.argb(235, 6, 10, 14));

                canvas.drawPath(path, fill);

                stroke.setStyle(android.graphics.Paint.Style.STROKE);
                stroke.setStrokeWidth(aceDp(2));
                stroke.setColor(Color.rgb(232, 177, 59));

                canvas.drawPath(path, stroke);
            }

            @Override
            public void setAlpha(int alpha) {}

            @Override
            public void setColorFilter(
                    android.graphics.ColorFilter colorFilter
            ) {}

            @Override
            public int getOpacity() {
                return android.graphics.PixelFormat.TRANSLUCENT;
            }
        };
    }

    private android.graphics.drawable.Drawable aceLoginDrawable() {
        // NEXORA — ELITE GLOW LOGIN BUTTON
        android.graphics.drawable.GradientDrawable normal =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[] {
                                Color.rgb(72, 3, 12),
                                Color.rgb(145, 8, 24),
                                Color.rgb(72, 3, 12)
                        }
                );

        normal.setCornerRadius(aceDp(22));
        normal.setStroke(
                aceDp(3),
                Color.rgb(255, 205, 72)
        );

        android.graphics.drawable.GradientDrawable pressed =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[] {
                                Color.rgb(110, 7, 18),
                                Color.rgb(205, 20, 35),
                                Color.rgb(110, 7, 18)
                        }
                );

        pressed.setCornerRadius(aceDp(22));
        pressed.setStroke(
                aceDp(4),
                Color.rgb(255, 232, 145)
        );

        android.graphics.drawable.StateListDrawable states =
                new android.graphics.drawable.StateListDrawable();

        states.addState(
                new int[] {android.R.attr.state_pressed},
                pressed
        );

        states.addState(
                new int[] {},
                normal
        );

        return states;
    }
}
