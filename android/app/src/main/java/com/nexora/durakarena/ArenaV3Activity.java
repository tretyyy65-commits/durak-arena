package com.nexora.durakarena;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

/**
 * V3 launcher/lobby. This is intentionally separate from MainActivity so the
 * existing card-game logic stays untouched while the presentation is rebuilt.
 */
public final class ArenaV3Activity extends Activity {
    private static final int RC_SIGN_IN = 6403;

    private static final int GOLD = Color.rgb(211, 169, 86);
    private static final int GOLD_LIGHT = Color.rgb(246, 220, 160);
    private static final int TEXT = Color.rgb(247, 244, 235);
    private static final int MUTED = Color.rgb(158, 172, 163);
    private static final int PANEL = Color.argb(232, 3, 17, 12);
    private static final int PANEL_SOFT = Color.argb(214, 6, 27, 19);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences profile;
    private FirebaseAuth auth;
    private GoogleSignInClient google;
    private boolean registrationMode;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        profile = getSharedPreferences("durak_arena_profile", MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        google = GoogleSignIn.getClient(this, gso);

        portrait();
        showLoading();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void portrait() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);
        if (android.os.Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private FrameLayout scene(ArenaV3BackdropDrawable.Scene scene) {
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new ArenaV3BackdropDrawable(scene));
        return root;
    }

    private TextView text(String value, int size, boolean bold, int gravity, int color) {
        TextView t = new TextView(this);
        t.setText(value == null ? "" : value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(gravity);
        t.setIncludeFontPadding(false);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    private android.graphics.drawable.GradientDrawable panel(int fill, int stroke, int radius) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) g.setStroke(dp(1), stroke);
        return g;
    }

    private android.graphics.drawable.GradientDrawable gradient(int top, int bottom, int stroke, int radius) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{top, bottom});
        g.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) g.setStroke(dp(1), stroke);
        return g;
    }

    private ImageView icon(String name, boolean active) {
        ImageView v = new ImageView(this);
        v.setImageDrawable(new ArenaV3IconDrawable(name, active));
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setPadding(dp(2), dp(2), dp(2), dp(2));
        return v;
    }

    private TextView primaryButton(String title) {
        TextView b = text(title, 18, true, Gravity.CENTER, GOLD_LIGHT);
        b.setClickable(true);
        b.setFocusable(true);
        b.setBackground(gradient(Color.rgb(31, 103, 68), Color.rgb(8, 43, 30), GOLD, 18));
        b.setElevation(dp(8));
        b.setPadding(dp(14), dp(8), dp(14), dp(8));
        return b;
    }

    private TextView secondaryButton(String title) {
        TextView b = text(title, 14, true, Gravity.CENTER, TEXT);
        b.setClickable(true);
        b.setFocusable(true);
        b.setBackground(panel(Color.argb(232, 4, 18, 13), Color.rgb(105, 82, 45), 14));
        b.setElevation(dp(3));
        return b;
    }

    private LinearLayout iconAction(String iconName, String title, boolean active) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(6), dp(8), dp(6), dp(5));
        box.setBackground(gradient(
                active ? Color.argb(245, 18, 58, 40) : Color.argb(238, 4, 20, 14),
                Color.argb(242, 2, 11, 8),
                active ? GOLD_LIGHT : Color.rgb(91, 73, 44), 14));
        box.setElevation(dp(active ? 6 : 3));

        ImageView i = icon(iconName, active);
        box.addView(i, new LinearLayout.LayoutParams(dp(32), dp(32)));
        TextView label = text(title, 9, true, Gravity.CENTER, active ? GOLD_LIGHT : TEXT);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, dp(20));
        llp.topMargin = dp(3);
        box.addView(label, llp);
        return box;
    }

    private void showLoading() {
        portrait();
        FrameLayout root = scene(ArenaV3BackdropDrawable.Scene.SPLASH);

        LinearLayout stack = new LinearLayout(this);
        stack.setOrientation(LinearLayout.VERTICAL);
        stack.setGravity(Gravity.CENTER_HORIZONTAL);
        stack.setPadding(dp(28), dp(64), dp(28), dp(46));

        TextView studio = text("NEXORA  INTERACTIVE", 11, true, Gravity.CENTER, GOLD);
        studio.setLetterSpacing(.20f);
        stack.addView(studio, new LinearLayout.LayoutParams(-1, dp(42)));
        stack.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1f));

        ImageView cards = icon("cards", true);
        stack.addView(cards, new LinearLayout.LayoutParams(dp(104), dp(104)));

        TextView title = text("DURAK", 43, true, Gravity.CENTER, TEXT);
        title.setLetterSpacing(.10f);
        stack.addView(title, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView arena = text("ARENA", 23, true, Gravity.CENTER, GOLD_LIGHT);
        arena.setLetterSpacing(.28f);
        stack.addView(arena, new LinearLayout.LayoutParams(-1, dp(44)));

        TextView line = text("КАРТИ  •  ХАРАКТЕР  •  РЕЙТИНГ", 9, true, Gravity.CENTER, MUTED);
        line.setLetterSpacing(.08f);
        stack.addView(line, new LinearLayout.LayoutParams(-1, dp(34)));
        stack.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1f));

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(0);
        bar.setProgressTintList(ColorStateList.valueOf(GOLD));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(22, 35, 29)));
        stack.addView(bar, new LinearLayout.LayoutParams(-1, dp(5)));

        TextView status = text("ЗАВАНТАЖЕННЯ  0%", 10, true, Gravity.CENTER, MUTED);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(40));
        sp.topMargin = dp(8);
        stack.addView(status, sp);

        root.addView(stack, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        final int[] value = {0};
        Runnable tick = new Runnable() {
            @Override public void run() {
                value[0] = Math.min(100, value[0] + 4);
                bar.setProgress(value[0]);
                status.setText("ЗАВАНТАЖЕННЯ  " + value[0] + "%");
                if (value[0] < 100) handler.postDelayed(this, 34);
                else handler.postDelayed(() -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) showLogin();
                    else {
                        saveProfile(user);
                        showMain();
                    }
                }, 220);
            }
        };
        handler.postDelayed(tick, 120);
    }

    private void showLogin() {
        portrait();
        FrameLayout root = scene(ArenaV3BackdropDrawable.Scene.LOGIN);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(24), dp(54), dp(24), dp(38));

        TextView studio = text("NEXORA  INTERACTIVE", 10, true, Gravity.CENTER, GOLD);
        studio.setLetterSpacing(.20f);
        page.addView(studio, new LinearLayout.LayoutParams(-1, dp(38)));

        TextView title = text("DURAK ARENA", 31, true, Gravity.CENTER, TEXT);
        title.setLetterSpacing(.08f);
        page.addView(title, new LinearLayout.LayoutParams(-1, dp(62)));

        TextView subtitle = text("Твоя арена. Твій рейтинг. Твоя гра.", 11, false, Gravity.CENTER, MUTED);
        page.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(36)));
        page.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1f));

        LinearLayout authCard = new LinearLayout(this);
        authCard.setOrientation(LinearLayout.VERTICAL);
        authCard.setPadding(dp(14), dp(15), dp(14), dp(15));
        authCard.setBackground(gradient(Color.argb(246, 8, 32, 22), Color.argb(246, 2, 12, 9), GOLD, 20));
        authCard.setElevation(dp(10));

        TextView login = primaryButton("УВІЙТИ ЧЕРЕЗ GOOGLE");
        login.setOnClickListener(v -> {
            registrationMode = false;
            startGoogleSignIn();
        });
        authCard.addView(login, new LinearLayout.LayoutParams(-1, dp(62)));

        TextView or = text("АБО", 9, true, Gravity.CENTER, MUTED);
        authCard.addView(or, new LinearLayout.LayoutParams(-1, dp(34)));

        TextView register = secondaryButton("СТВОРИТИ ПРОФІЛЬ");
        register.setOnClickListener(v -> {
            registrationMode = true;
            startGoogleSignIn();
        });
        authCard.addView(register, new LinearLayout.LayoutParams(-1, dp(56)));

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, dp(182));
        cardLp.bottomMargin = dp(14);
        page.addView(authCard, cardLp);

        TextView note = text("Профіль синхронізується через Google", 9, false, Gravity.CENTER, MUTED);
        page.addView(note, new LinearLayout.LayoutParams(-1, dp(30)));

        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void startGoogleSignIn() {
        startActivityForResult(google.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_SIGN_IN) return;
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential).addOnCompleteListener(this, result -> {
                if (result.isSuccessful() && auth.getCurrentUser() != null) {
                    saveProfile(auth.getCurrentUser());
                    if (registrationMode && !profile.contains("registered_at")) {
                        profile.edit().putLong("registered_at", System.currentTimeMillis()).apply();
                    }
                    showMain();
                } else {
                    Toast.makeText(this, "Не вдалося увійти через Google", Toast.LENGTH_LONG).show();
                }
            });
        } catch (ApiException e) {
            Toast.makeText(this, "Google: помилка " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveProfile(FirebaseUser user) {
        SharedPreferences.Editor e = profile.edit();
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            e.putString("player_name", user.getDisplayName().trim());
        }
        e.putString("firebase_uid", user.getUid());
        if (user.getPhotoUrl() != null) e.putString("player_photo_url", user.getPhotoUrl().toString());

        String key = "player_id_" + user.getUid();
        String id = profile.getString(key, "");
        if (id == null || id.trim().isEmpty()) {
            id = profile.getString("player_id", "");
            if (id == null || id.trim().isEmpty()) id = createPlayerId();
            e.putString(key, id);
        }
        e.putString("player_id", id);
        e.apply();
    }

    private String createPlayerId() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder b = new StringBuilder("#");
        for (int i = 0; i < 8; i++) b.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return b.toString();
    }

    private String playerName() {
        String s = profile.getString("player_name", "Фокс");
        return s == null || s.trim().isEmpty() ? "Фокс" : s.trim();
    }

    private String playerId() {
        String s = profile.getString("player_id", "#QR12657R");
        return s == null || s.trim().isEmpty() ? "#QR12657R" : s.trim();
    }

    private int cups() { return Math.max(0, profile.getInt("player_cups", 0)); }
    private int level() { return Math.max(1, profile.getInt("player_level", 1)); }

    private String amount(int n) {
        return NumberFormat.getIntegerInstance(new Locale("uk", "UA")).format(Math.max(0, n));
    }

    private LinearLayout topHud() {
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(6), dp(8), dp(6));
        top.setBackground(gradient(Color.argb(241, 5, 24, 17), Color.argb(241, 2, 12, 9), Color.rgb(92, 72, 42), 16));
        top.setElevation(dp(6));
        top.setOnClickListener(v -> showProfile());

        FrameLayout avatarWrap = new FrameLayout(this);
        avatarWrap.setBackground(panel(Color.rgb(15, 48, 34), GOLD, 13));
        ImageView avatar = icon("profile", true);
        FrameLayout.LayoutParams av = new FrameLayout.LayoutParams(dp(32), dp(32), Gravity.CENTER);
        avatarWrap.addView(avatar, av);
        top.addView(avatarWrap, new LinearLayout.LayoutParams(dp(50), dp(50)));

        LinearLayout identity = new LinearLayout(this);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setPadding(dp(8), 0, dp(4), 0);
        identity.addView(text(playerName(), 13, true, Gravity.LEFT, TEXT), new LinearLayout.LayoutParams(-1, dp(25)));
        identity.addView(text("LV." + level() + "  •  " + playerId(), 9, false, Gravity.LEFT, MUTED), new LinearLayout.LayoutParams(-1, dp(19)));
        top.addView(identity, new LinearLayout.LayoutParams(0, -1, 1f));

        LinearLayout wallet = new LinearLayout(this);
        wallet.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        ImageView coin = icon("coin", true);
        wallet.addView(coin, new LinearLayout.LayoutParams(dp(22), dp(22)));
        wallet.addView(text(amount(GloryRewards.balance(this, "coins")), 10, true, Gravity.CENTER, GOLD_LIGHT), new LinearLayout.LayoutParams(dp(52), dp(28)));
        ImageView crystal = icon("crystal", false);
        wallet.addView(crystal, new LinearLayout.LayoutParams(dp(20), dp(20)));
        wallet.addView(text(amount(GloryRewards.balance(this, "crystals")), 10, true, Gravity.CENTER, TEXT), new LinearLayout.LayoutParams(dp(42), dp(28)));
        top.addView(wallet, new LinearLayout.LayoutParams(dp(138), -1));
        return top;
    }

    private void showMain() {
        portrait();
        FrameLayout root = scene(ArenaV3BackdropDrawable.Scene.LOBBY);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(14), dp(12), dp(14));
        page.addView(topHud(), new LinearLayout.LayoutParams(-1, dp(64)));

        LinearLayout branding = new LinearLayout(this);
        branding.setOrientation(LinearLayout.VERTICAL);
        branding.setGravity(Gravity.CENTER_HORIZONTAL);
        branding.setPadding(0, dp(10), 0, 0);
        TextView studio = text("NEXORA  INTERACTIVE", 9, true, Gravity.CENTER, GOLD);
        studio.setLetterSpacing(.18f);
        branding.addView(studio, new LinearLayout.LayoutParams(-1, dp(24)));
        TextView title = text("DURAK ARENA", 25, true, Gravity.CENTER, TEXT);
        title.setLetterSpacing(.08f);
        branding.addView(title, new LinearLayout.LayoutParams(-1, dp(38)));
        TextView league = text(UiKit.arenaTitleForCups(cups()) + "  •  " + cups() + " РЕЙТИНГ", 9, true, Gravity.CENTER, GOLD_LIGHT);
        branding.addView(league, new LinearLayout.LayoutParams(-1, dp(24)));
        page.addView(branding, new LinearLayout.LayoutParams(-1, dp(90)));

        page.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1f));

        LinearLayout arenaChip = new LinearLayout(this);
        arenaChip.setGravity(Gravity.CENTER_VERTICAL);
        arenaChip.setPadding(dp(12), dp(8), dp(12), dp(8));
        arenaChip.setBackground(gradient(Color.argb(224, 6, 31, 21), Color.argb(226, 2, 14, 10), Color.rgb(116, 88, 46), 16));
        ImageView trophy = icon("trophy", true);
        arenaChip.addView(trophy, new LinearLayout.LayoutParams(dp(38), dp(38)));
        LinearLayout rankText = new LinearLayout(this);
        rankText.setOrientation(LinearLayout.VERTICAL);
        rankText.setPadding(dp(10), 0, 0, 0);
        rankText.addView(text("ШЛЯХ СЛАВИ", 9, true, Gravity.LEFT, GOLD), new LinearLayout.LayoutParams(-1, dp(18)));
        rankText.addView(text(currentRankName() + "  •  " + cups() + " / " + nextRankTarget(), 14, true, Gravity.LEFT, TEXT), new LinearLayout.LayoutParams(-1, dp(25)));
        arenaChip.addView(rankText, new LinearLayout.LayoutParams(0, -1, 1f));
        arenaChip.setOnClickListener(v -> showGlory());
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(-1, dp(62));
        chipLp.leftMargin = dp(12); chipLp.rightMargin = dp(12); chipLp.bottomMargin = dp(10);
        page.addView(arenaChip, chipLp);

        TextView play = primaryButton("ГРАТИ");
        play.setOnClickListener(v -> showModes());
        LinearLayout.LayoutParams playLp = new LinearLayout.LayoutParams(-1, dp(68));
        playLp.leftMargin = dp(20); playLp.rightMargin = dp(20); playLp.bottomMargin = dp(12);
        page.addView(play, playLp);

        LinearLayout quick = new LinearLayout(this);
        quick.setGravity(Gravity.CENTER);
        String[] qi = {"trophy", "cards", "medal", "shop"};
        String[] qt = {"СЛАВА", "РЕЖИМИ", "МЕДАЛІ", "МАГАЗИН"};
        for (int i = 0; i < qt.length; i++) {
            LinearLayout item = iconAction(qi[i], qt[i], false);
            final int idx = i;
            item.setOnClickListener(v -> {
                if (idx == 0) showGlory();
                else if (idx == 1) showModes();
                else if (idx == 2) toast("Медалі: екран буде підключений до досягнень");
                else toast("Магазин: скіни, столи, аватари та емоції");
            });
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, dp(66), 1f);
            if (i > 0) ip.leftMargin = dp(6);
            quick.addView(item, ip);
        }
        LinearLayout.LayoutParams quickLp = new LinearLayout.LayoutParams(-1, dp(66));
        quickLp.bottomMargin = dp(10);
        page.addView(quick, quickLp);

        page.addView(bottomNav(0), new LinearLayout.LayoutParams(-1, dp(62)));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private LinearLayout bottomNav(int active) {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(4), dp(4), dp(4), dp(4));
        nav.setBackground(gradient(Color.argb(242, 4, 20, 14), Color.argb(242, 2, 10, 8), Color.rgb(88, 70, 42), 15));
        String[] icons = {"home", "trophy", "cards", "profile"};
        String[] labels = {"ГОЛОВНА", "СЛАВА", "РЕЖИМИ", "ПРОФІЛЬ"};
        for (int i = 0; i < labels.length; i++) {
            LinearLayout item = iconAction(icons[i], labels[i], i == active);
            final int idx = i;
            item.setOnClickListener(v -> {
                if (idx == 0) showMain();
                else if (idx == 1) showGlory();
                else if (idx == 2) showModes();
                else showProfile();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            if (i > 0) lp.leftMargin = dp(4);
            nav.addView(item, lp);
        }
        return nav;
    }

    private LinearLayout header(String title, Runnable back) {
        LinearLayout h = new LinearLayout(this);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(dp(7), dp(5), dp(7), dp(5));
        h.setBackground(gradient(Color.argb(242, 4, 21, 15), Color.argb(242, 2, 11, 8), Color.rgb(94, 73, 43), 15));
        ImageView b = icon("back", true);
        b.setOnClickListener(v -> back.run());
        h.addView(b, new LinearLayout.LayoutParams(dp(40), dp(40)));
        TextView t = text(title, 17, true, Gravity.CENTER, TEXT);
        t.setLetterSpacing(.05f);
        h.addView(t, new LinearLayout.LayoutParams(0, -1, 1f));
        ImageView cards = icon("cards", false);
        h.addView(cards, new LinearLayout.LayoutParams(dp(40), dp(40)));
        return h;
    }

    private LinearLayout modeCard(String iconName, String title, String subtitle, boolean featured) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(13), dp(10), dp(13), dp(10));
        row.setBackground(gradient(
                featured ? Color.argb(242, 17, 61, 41) : Color.argb(236, 4, 21, 15),
                Color.argb(242, 2, 11, 8),
                featured ? GOLD_LIGHT : Color.rgb(92, 72, 43), 18));
        row.setElevation(dp(featured ? 7 : 3));

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setBackground(panel(Color.argb(210, 6, 34, 23), featured ? GOLD : Color.rgb(76, 65, 47), 14));
        ImageView i = icon(iconName, featured);
        iconWrap.addView(i, new FrameLayout.LayoutParams(dp(34), dp(34), Gravity.CENTER));
        row.addView(iconWrap, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(dp(12), 0, dp(6), 0);
        words.addView(text(title, 15, true, Gravity.LEFT, featured ? GOLD_LIGHT : TEXT), new LinearLayout.LayoutParams(-1, dp(27)));
        words.addView(text(subtitle, 10, false, Gravity.LEFT, MUTED), new LinearLayout.LayoutParams(-1, dp(23)));
        row.addView(words, new LinearLayout.LayoutParams(0, -1, 1f));

        TextView arrow = text("›", 30, false, Gravity.CENTER, GOLD_LIGHT);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(28), -1));
        return row;
    }

    private void showModes() {
        portrait();
        FrameLayout root = scene(ArenaV3BackdropDrawable.Scene.INNER);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(14), dp(12), dp(14));
        page.addView(header("РЕЖИМИ ГРИ", this::showMain), new LinearLayout.LayoutParams(-1, dp(58)));

        TextView intro = text("ОБЕРИ СТІЛ", 10, true, Gravity.CENTER, GOLD);
        intro.setLetterSpacing(.18f);
        LinearLayout.LayoutParams introLp = new LinearLayout.LayoutParams(-1, dp(48));
        introLp.topMargin = dp(6);
        page.addView(intro, introLp);

        LinearLayout classic = modeCard("cards", "КЛАСИКА", "2–5 гравців  •  стандартні правила", true);
        classic.setOnClickListener(v -> launchGame("classic"));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, dp(82));
        cardLp.bottomMargin = dp(10);
        page.addView(classic, cardLp);

        LinearLayout fast = modeCard("play", "ШВИДКА ГРА", "короткий стіл  •  швидкий матч", false);
        fast.setOnClickListener(v -> launchGame("fast"));
        LinearLayout.LayoutParams fastLp = new LinearLayout.LayoutParams(-1, dp(82));
        fastLp.bottomMargin = dp(10);
        page.addView(fast, fastLp);

        LinearLayout ranked = modeCard("trophy", "РЕЙТИНГОВА АРЕНА", "матч за рейтинг  •  сезонні нагороди", false);
        ranked.setOnClickListener(v -> launchGame("ranked"));
        page.addView(ranked, new LinearLayout.LayoutParams(-1, dp(82)));

        page.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1f));
        page.addView(bottomNav(2), new LinearLayout.LayoutParams(-1, dp(62)));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void launchGame(String mode) {
        getSharedPreferences("durak_arena_game", MODE_PRIVATE).edit().putString("selected_mode", mode).apply();
        startActivity(new Intent(this, MainActivity.class));
    }

    private void showGlory() {
        portrait();
        FrameLayout root = scene(ArenaV3BackdropDrawable.Scene.INNER);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(14), dp(12), dp(14));
        page.addView(header("ШЛЯХ СЛАВИ", this::showMain), new LinearLayout.LayoutParams(-1, dp(58)));

        LinearLayout summary = new LinearLayout(this);
        summary.setGravity(Gravity.CENTER_VERTICAL);
        summary.setPadding(dp(13), dp(9), dp(13), dp(9));
        summary.setBackground(gradient(Color.argb(232, 7, 34, 23), Color.argb(232, 2, 13, 9), GOLD, 16));
        ImageView tr = icon("trophy", true);
        summary.addView(tr, new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout sWords = new LinearLayout(this);
        sWords.setOrientation(LinearLayout.VERTICAL);
        sWords.setPadding(dp(10), 0, 0, 0);
        sWords.addView(text(currentRankName(), 16, true, Gravity.LEFT, GOLD_LIGHT), new LinearLayout.LayoutParams(-1, dp(27)));
        sWords.addView(text(cups() + " рейтинг  •  далі " + nextRankTarget(), 10, false, Gravity.LEFT, MUTED), new LinearLayout.LayoutParams(-1, dp(22)));
        summary.addView(sWords, new LinearLayout.LayoutParams(0, -1, 1f));
        LinearLayout.LayoutParams sumLp = new LinearLayout.LayoutParams(-1, dp(66));
        sumLp.topMargin = dp(10); sumLp.bottomMargin = dp(10);
        page.addView(summary, sumLp);

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.VERTICAL);
        track.setPadding(dp(5), dp(2), dp(5), dp(8));
        String[] names = {"НОВАЧОК", "ГРАВЕЦЬ", "ПРОФІ", "МАЙСТЕР", "ЕЛІТА", "ЛЕГЕНДА"};
        int[] need = {0, 300, 900, 1800, 3000, 4500};
        for (int i = 0; i < names.length; i++) {
            boolean open = cups() >= need[i];
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(7), dp(10), dp(7));
            row.setBackground(gradient(
                    open ? Color.argb(238, 11, 48, 32) : Color.argb(226, 3, 17, 12),
                    Color.argb(236, 2, 11, 8), open ? GOLD : Color.rgb(67, 62, 52), 16));
            FrameLayout badge = new FrameLayout(this);
            badge.setBackground(panel(open ? Color.rgb(18, 61, 41) : Color.rgb(17, 22, 20), open ? GOLD : Color.rgb(75, 72, 64), 13));
            ImageView ti = icon("trophy", open);
            badge.addView(ti, new FrameLayout.LayoutParams(dp(31), dp(31), Gravity.CENTER));
            row.addView(badge, new LinearLayout.LayoutParams(dp(52), dp(52)));
            LinearLayout words = new LinearLayout(this);
            words.setOrientation(LinearLayout.VERTICAL);
            words.setPadding(dp(11), 0, 0, 0);
            words.addView(text(names[i], 14, true, Gravity.LEFT, open ? GOLD_LIGHT : TEXT), new LinearLayout.LayoutParams(-1, dp(26)));
            words.addView(text(need[i] + "+ рейтингу", 9, false, Gravity.LEFT, MUTED), new LinearLayout.LayoutParams(-1, dp(20)));
            row.addView(words, new LinearLayout.LayoutParams(0, -1, 1f));
            row.addView(text(open ? "ВІДКРИТО" : "ЗАКРИТО", 8, true, Gravity.CENTER, open ? GOLD_LIGHT : MUTED), new LinearLayout.LayoutParams(dp(68), dp(30)));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(68));
            rp.bottomMargin = dp(7);
            track.addView(row, rp);
        }
        scroll.addView(track, new ScrollView.LayoutParams(-1, -2));
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        page.addView(bottomNav(1), new LinearLayout.LayoutParams(-1, dp(62)));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void showProfile() {
        portrait();
        FrameLayout root = scene(ArenaV3BackdropDrawable.Scene.INNER);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(14), dp(12), dp(14));
        page.addView(header("ПРОФІЛЬ", this::showMain), new LinearLayout.LayoutParams(-1, dp(58)));

        LinearLayout hero = new LinearLayout(this);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(14), dp(12), dp(14), dp(12));
        hero.setBackground(gradient(Color.argb(238, 9, 40, 27), Color.argb(238, 2, 13, 9), GOLD, 18));
        FrameLayout avatar = new FrameLayout(this);
        avatar.setBackground(panel(Color.rgb(18, 58, 40), GOLD_LIGHT, 18));
        avatar.addView(icon("profile", true), new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.CENTER));
        hero.addView(avatar, new LinearLayout.LayoutParams(dp(74), dp(74)));
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(13), 0, 0, 0);
        info.addView(text(playerName(), 20, true, Gravity.LEFT, TEXT), new LinearLayout.LayoutParams(-1, dp(31)));
        info.addView(text(playerId() + "  •  LV." + level(), 10, false, Gravity.LEFT, MUTED), new LinearLayout.LayoutParams(-1, dp(23)));
        info.addView(text(currentRankName() + "  •  " + cups() + " рейтинг", 10, true, Gravity.LEFT, GOLD_LIGHT), new LinearLayout.LayoutParams(-1, dp(23)));
        hero.addView(info, new LinearLayout.LayoutParams(0, -1, 1f));
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(-1, dp(102));
        heroLp.topMargin = dp(10); heroLp.bottomMargin = dp(12);
        page.addView(hero, heroLp);

        LinearLayout stats = new LinearLayout(this);
        stats.setGravity(Gravity.CENTER);
        stats.addView(statBox("РЕЙТИНГ", String.valueOf(cups())), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams middle = new LinearLayout.LayoutParams(0, dp(76), 1f); middle.leftMargin = dp(7); middle.rightMargin = dp(7);
        stats.addView(statBox("ПЕРЕМОГИ", String.valueOf(profile.getInt("wins", 0))), middle);
        stats.addView(statBox("СЕРІЯ", String.valueOf(profile.getInt("win_streak", 0))), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams statLp = new LinearLayout.LayoutParams(-1, dp(76)); statLp.bottomMargin = dp(12);
        page.addView(stats, statLp);

        String[] items = {"МЕДАЛІ ТА ДОСЯГНЕННЯ", "ІСТОРІЯ МАТЧІВ", "СПИСОК ЛІДЕРІВ", "КЛАН", "НАЛАШТУВАННЯ"};
        for (String item : items) {
            TextView b = secondaryButton(item + "   ›");
            b.setOnClickListener(v -> toast(item + ": підключення розділу в наступному етапі"));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(50)); bp.bottomMargin = dp(7);
            page.addView(b, bp);
        }

        page.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1f));
        page.addView(bottomNav(3), new LinearLayout.LayoutParams(-1, dp(62)));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private LinearLayout statBox(String title, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackground(gradient(Color.argb(235, 5, 27, 19), Color.argb(235, 2, 12, 9), Color.rgb(88, 71, 44), 14));
        box.addView(text(value, 19, true, Gravity.CENTER, GOLD_LIGHT), new LinearLayout.LayoutParams(-1, dp(36)));
        box.addView(text(title, 8, true, Gravity.CENTER, MUTED), new LinearLayout.LayoutParams(-1, dp(22)));
        return box;
    }

    private String currentRankName() {
        int c = cups();
        if (c >= 4500) return "ЛЕГЕНДА";
        if (c >= 3000) return "ЕЛІТА";
        if (c >= 1800) return "МАЙСТЕР";
        if (c >= 900) return "ПРОФІ";
        if (c >= 300) return "ГРАВЕЦЬ";
        return "НОВАЧОК";
    }

    private int nextRankTarget() {
        int c = cups();
        if (c < 300) return 300;
        if (c < 900) return 900;
        if (c < 1800) return 1800;
        if (c < 3000) return 3000;
        if (c < 4500) return 4500;
        return 4500;
    }

    private void toast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }
}
