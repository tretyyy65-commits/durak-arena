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
import android.view.ViewGroup;
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

import java.util.Random;

/**
 * Premium launch/lobby flow for Durak Arena.
 * The legacy game remains in MainActivity; this activity owns only the presentation
 * and sends the player into the existing game when PLAY is pressed.
 */
public class PremiumActivity extends Activity {
    private static final int RC_SIGN_IN = 4402;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private SharedPreferences profile;
    private boolean registrationMode;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        profile = getSharedPreferences("durak_arena_profile", MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        portrait();
        showLoading();
    }

    private void portrait() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private FrameLayout scene(ArenaBackdropDrawable.Scene scene) {
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new ArenaBackdropDrawable(scene));
        return root;
    }

    private TextView label(String value, int size, boolean bold, int gravity, int color) {
        TextView v = UiKit.text(this, value, size, bold, gravity, color);
        v.setIncludeFontPadding(false);
        return v;
    }

    private TextView button(String title, boolean primary) {
        TextView b = UiKit.button(this, title, primary);
        b.setElevation(UiKit.dp(this, primary ? 8 : 3));
        return b;
    }

    private LinearLayout iconTile(String iconName, String title) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(UiKit.dp(this, 7), UiKit.dp(this, 8), UiKit.dp(this, 7), UiKit.dp(this, 6));
        tile.setBackground(UiKit.panel(this, Color.argb(236, 4, 17, 12), Color.rgb(91, 72, 42), 1, 14));
        tile.setClickable(true);
        tile.setFocusable(true);

        ImageView icon = UiKit.icon(this, iconName, title);
        tile.addView(icon, new LinearLayout.LayoutParams(UiKit.dp(this, 38), UiKit.dp(this, 38)));

        TextView text = label(title, 10, true, Gravity.CENTER, UiKit.TEXT);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 25));
        tlp.topMargin = UiKit.dp(this, 4);
        tile.addView(text, tlp);
        return tile;
    }

    private void showLoading() {
        portrait();
        FrameLayout root = scene(ArenaBackdropDrawable.Scene.SPLASH);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);
        center.setPadding(UiKit.dp(this, 28), UiKit.dp(this, 64), UiKit.dp(this, 28), UiKit.dp(this, 45));

        TextView studio = label("NEXORA  INTERACTIVE", 12, true, Gravity.CENTER, UiKit.GOLD);
        studio.setLetterSpacing(0.20f);
        center.addView(studio, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 48)));

        View spacer1 = new View(this);
        center.addView(spacer1, new LinearLayout.LayoutParams(1, 0, 1f));

        ImageView mark = UiKit.icon(this, "nexora_cards", "Durak Arena");
        center.addView(mark, new LinearLayout.LayoutParams(UiKit.dp(this, 112), UiKit.dp(this, 112)));

        TextView durak = label("DURAK", 42, true, Gravity.CENTER, UiKit.TEXT);
        durak.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        durak.setLetterSpacing(0.08f);
        center.addView(durak, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58)));

        TextView arena = label("ARENA", 23, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        arena.setLetterSpacing(0.28f);
        center.addView(arena, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 42)));

        TextView motto = label("КАРТИ. ХАРАКТЕР. РЕЙТИНГ.", 10, false, Gravity.CENTER, UiKit.MUTED);
        motto.setLetterSpacing(0.10f);
        center.addView(motto, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 38)));

        View spacer2 = new View(this);
        center.addView(spacer2, new LinearLayout.LayoutParams(1, 0, 1f));

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(0);
        progress.setProgressTintList(ColorStateList.valueOf(UiKit.GOLD));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(19, 35, 29)));
        center.addView(progress, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 6)));

        TextView status = label("Завантаження  0%", 11, true, Gravity.CENTER, UiKit.MUTED);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 42));
        sp.topMargin = UiKit.dp(this, 8);
        center.addView(status, sp);

        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1, -1);
        cp.leftMargin = UiKit.dp(this, 18);
        cp.rightMargin = UiKit.dp(this, 18);
        root.addView(center, cp);
        setContentView(root);

        final int[] value = {0};
        Runnable tick = new Runnable() {
            @Override public void run() {
                value[0] = Math.min(100, value[0] + 4);
                progress.setProgress(value[0]);
                status.setText("Завантаження  " + value[0] + "%");
                if (value[0] < 100) {
                    handler.postDelayed(this, 34);
                } else {
                    handler.postDelayed(() -> {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) showLogin();
                        else {
                            saveProfile(user);
                            showMain();
                        }
                    }, 220);
                }
            }
        };
        handler.postDelayed(tick, 120);
    }

    private void showLogin() {
        FrameLayout root = scene(ArenaBackdropDrawable.Scene.LOGIN);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(UiKit.dp(this, 26), UiKit.dp(this, 58), UiKit.dp(this, 26), UiKit.dp(this, 42));

        TextView studio = label("NEXORA  INTERACTIVE", 11, true, Gravity.CENTER, UiKit.GOLD);
        studio.setLetterSpacing(0.18f);
        page.addView(studio, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 42)));

        ImageView mark = UiKit.icon(this, "nexora_cards", "Durak Arena");
        page.addView(mark, new LinearLayout.LayoutParams(UiKit.dp(this, 74), UiKit.dp(this, 74)));

        TextView title = label("DURAK ARENA", 30, true, Gravity.CENTER, UiKit.TEXT);
        title.setLetterSpacing(0.08f);
        page.addView(title, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 66)));

        TextView subtitle = label("Увійди на арену та продовж свій шлях", 12, false, Gravity.CENTER, UiKit.MUTED);
        page.addView(subtitle, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 46)));

        View spacer = new View(this);
        page.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1f));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16));
        card.setBackground(UiKit.gradient(this,
                Color.argb(245, 9, 30, 21), Color.argb(245, 3, 14, 10), UiKit.GOLD, 18));
        card.setElevation(UiKit.dp(this, 10));

        TextView login = button("УВІЙТИ ЧЕРЕЗ GOOGLE", true);
        login.setOnClickListener(v -> {
            registrationMode = false;
            startGoogleSignIn();
        });
        card.addView(login, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 62)));

        TextView divider = label("АБО", 10, true, Gravity.CENTER, UiKit.MUTED);
        card.addView(divider, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 38)));

        TextView register = button("СТВОРИТИ ПРОФІЛЬ", false);
        register.setOnClickListener(v -> {
            registrationMode = true;
            startGoogleSignIn();
        });
        card.addView(register, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58)));

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 190));
        cardLp.bottomMargin = UiKit.dp(this, 18);
        page.addView(card, cardLp);

        TextView note = label("Google використовується лише для входу та збереження профілю", 10, false, Gravity.CENTER, UiKit.MUTED);
        page.addView(note, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 40)));

        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void startGoogleSignIn() {
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_SIGN_IN) return;

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, auth -> {
                if (auth.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                    saveProfile(firebaseAuth.getCurrentUser());
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
        Random random = new Random();
        StringBuilder id = new StringBuilder("#");
        for (int i = 0; i < 8; i++) id.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return id.toString();
    }

    private String playerName() {
        String name = profile.getString("player_name", "Фокс");
        return name == null || name.trim().isEmpty() ? "Фокс" : name.trim();
    }

    private String playerId() {
        String id = profile.getString("player_id", "#QR12657R");
        return id == null || id.trim().isEmpty() ? "#QR12657R" : id.trim();
    }

    private int cups() {
        return Math.max(0, profile.getInt("player_cups", 0));
    }

    private int level() {
        return Math.max(1, profile.getInt("player_level", 1));
    }

    private LinearLayout topBar() {
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(UiKit.dp(this, 10), UiKit.dp(this, 7), UiKit.dp(this, 10), UiKit.dp(this, 7));
        top.setBackground(UiKit.panel(this, Color.argb(238, 3, 15, 10), Color.rgb(93, 76, 48), 1, 15));
        top.setElevation(UiKit.dp(this, 5));

        ImageView avatar = UiKit.icon(this, "nexora_profile", "Профіль");
        top.addView(avatar, new LinearLayout.LayoutParams(UiKit.dp(this, 45), UiKit.dp(this, 45)));

        LinearLayout identity = new LinearLayout(this);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setPadding(UiKit.dp(this, 8), 0, UiKit.dp(this, 4), 0);
        identity.addView(label(playerName(), 13, true, Gravity.LEFT, UiKit.TEXT), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 25)));
        identity.addView(label(playerId() + "  ·  LV." + level(), 9, false, Gravity.LEFT, UiKit.MUTED), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 20)));
        top.addView(identity, new LinearLayout.LayoutParams(0, -1, 1f));

        LinearLayout wallet = new LinearLayout(this);
        wallet.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

        ImageView coin = UiKit.icon(this, "nexora_coin", "Монети");
        wallet.addView(coin, new LinearLayout.LayoutParams(UiKit.dp(this, 23), UiKit.dp(this, 23)));
        TextView coins = label(String.valueOf(GloryRewards.balance(this, "coins")), 10, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        wallet.addView(coins, new LinearLayout.LayoutParams(UiKit.dp(this, 48), UiKit.dp(this, 28)));

        ImageView crystal = UiKit.icon(this, "nexora_crystal", "Кристали");
        wallet.addView(crystal, new LinearLayout.LayoutParams(UiKit.dp(this, 23), UiKit.dp(this, 23)));
        TextView crystals = label(String.valueOf(GloryRewards.balance(this, "crystals")), 10, true, Gravity.CENTER, UiKit.TEXT);
        wallet.addView(crystals, new LinearLayout.LayoutParams(UiKit.dp(this, 38), UiKit.dp(this, 28)));

        top.addView(wallet, new LinearLayout.LayoutParams(UiKit.dp(this, 134), -1));
        top.setOnClickListener(v -> showProfile());
        return top;
    }

    private void showMain() {
        portrait();
        FrameLayout root = scene(ArenaBackdropDrawable.Scene.LOBBY);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 15));

        page.addView(topBar(), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 66)));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 10));

        TextView studio = label("NEXORA  INTERACTIVE", 10, true, Gravity.CENTER, UiKit.GOLD);
        studio.setLetterSpacing(0.17f);
        hero.addView(studio, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 28)));

        ImageView mark = UiKit.icon(this, "nexora_cards", "Durak Arena");
        hero.addView(mark, new LinearLayout.LayoutParams(UiKit.dp(this, 76), UiKit.dp(this, 76)));

        TextView title = label("DURAK ARENA", 28, true, Gravity.CENTER, UiKit.TEXT);
        title.setLetterSpacing(0.08f);
        hero.addView(title, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 48)));

        TextView league = label(UiKit.arenaTitleForCups(cups()) + "  ·  " + cups() + " РЕЙТИНГ", 10, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        hero.addView(league, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 30)));

        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, 0, 1f);
        hp.topMargin = UiKit.dp(this, 10);
        page.addView(hero, hp);

        TextView play = button("ГРАТИ", true);
        play.setOnClickListener(v -> showModes());
        LinearLayout.LayoutParams playLp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 66));
        playLp.leftMargin = UiKit.dp(this, 18);
        playLp.rightMargin = UiKit.dp(this, 18);
        playLp.bottomMargin = UiKit.dp(this, 18);
        page.addView(play, playLp);

        LinearLayout quick = new LinearLayout(this);
        quick.setGravity(Gravity.CENTER);
        String[] icons = {"nexora_trophy", "nexora_cards", "nexora_achievement", "nexora_shop"};
        String[] titles = {"СЛАВА", "РЕЖИМИ", "МЕДАЛІ", "МАГАЗИН"};
        for (int i = 0; i < titles.length; i++) {
            LinearLayout tile = iconTile(icons[i], titles[i]);
            final int index = i;
            tile.setOnClickListener(v -> {
                if (index == 0) showGlory();
                else if (index == 1) showModes();
                else if (index == 2) showSimple("МЕДАЛІ", "Досягнення та сезонні нагороди");
                else showSimple("МАГАЗИН", "Скіни, столи, аватари та емоції");
            });
            LinearLayout.LayoutParams tileLp = new LinearLayout.LayoutParams(0, UiKit.dp(this, 82), 1f);
            if (i > 0) tileLp.leftMargin = UiKit.dp(this, 7);
            quick.addView(tile, tileLp);
        }
        LinearLayout.LayoutParams quickLp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 82));
        quickLp.bottomMargin = UiKit.dp(this, 13);
        page.addView(quick, quickLp);

        page.addView(bottomNav(0), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 66)));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private LinearLayout bottomNav(int active) {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(UiKit.dp(this, 4), UiKit.dp(this, 4), UiKit.dp(this, 4), UiKit.dp(this, 4));
        nav.setBackground(UiKit.panel(this, Color.argb(240, 3, 15, 10), Color.rgb(79, 66, 45), 1, 15));

        String[] icons = {"nexora_arena", "nexora_trophy", "nexora_cards", "nexora_profile"};
        String[] names = {"ГОЛОВНА", "СЛАВА", "РЕЖИМИ", "ПРОФІЛЬ"};
        for (int i = 0; i < names.length; i++) {
            LinearLayout item = UiKit.navButton(this, icons[i], names[i], i == active);
            final int index = i;
            item.setOnClickListener(v -> {
                if (index == 0) showMain();
                else if (index == 1) showGlory();
                else if (index == 2) showModes();
                else showProfile();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            if (i > 0) lp.leftMargin = UiKit.dp(this, 4);
            nav.addView(item, lp);
        }
        return nav;
    }

    private LinearLayout header(String title, Runnable back) {
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(UiKit.dp(this, 8), UiKit.dp(this, 5), UiKit.dp(this, 8), UiKit.dp(this, 5));
        bar.setBackground(UiKit.panel(this, Color.argb(238, 3, 15, 10), Color.rgb(90, 73, 46), 1, 14));

        ImageView backIcon = UiKit.icon(this, "nexora_back", "Назад");
        backIcon.setOnClickListener(v -> back.run());
        bar.addView(backIcon, new LinearLayout.LayoutParams(UiKit.dp(this, 43), UiKit.dp(this, 43)));

        TextView text = label(title, 17, true, Gravity.CENTER, UiKit.TEXT);
        text.setLetterSpacing(0.05f);
        bar.addView(text, new LinearLayout.LayoutParams(0, -1, 1f));

        ImageView mark = UiKit.icon(this, "nexora_cards", "Durak Arena");
        bar.addView(mark, new LinearLayout.LayoutParams(UiKit.dp(this, 43), UiKit.dp(this, 43)));
        return bar;
    }

    private void showGlory() {
        FrameLayout root = scene(ArenaBackdropDrawable.Scene.INNER);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 15));
        page.addView(header("ШЛЯХ СЛАВИ", this::showMain), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58)));

        TextView summary = label("Рейтинг  " + cups() + "   ·   " + UiKit.arenaTitleForCups(cups()), 11, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        LinearLayout.LayoutParams sumLp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 48));
        sumLp.topMargin = UiKit.dp(this, 8);
        page.addView(summary, sumLp);

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout path = new LinearLayout(this);
        path.setOrientation(LinearLayout.VERTICAL);
        path.setPadding(UiKit.dp(this, 8), UiKit.dp(this, 2), UiKit.dp(this, 8), UiKit.dp(this, 12));

        String[] rankIcons = {"rank_novice_1", "rank_novice_3", "rank_pro_2", "rank_master_2", "rank_elite_3", "rank_legend_3"};
        String[] rankNames = {"НОВАЧОК", "ГРАВЕЦЬ", "ПРОФІ", "МАЙСТЕР", "ЕЛІТА", "ЛЕГЕНДА"};
        int[] thresholds = {0, 300, 900, 1800, 3000, 4500};
        for (int i = 0; i < rankNames.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 8), UiKit.dp(this, 12), UiKit.dp(this, 8));
            boolean unlocked = cups() >= thresholds[i];
            row.setBackground(UiKit.panel(this,
                    unlocked ? Color.argb(235, 8, 31, 22) : Color.argb(225, 4, 14, 10),
                    unlocked ? UiKit.GOLD : Color.rgb(67, 61, 51), 1, 15));

            ImageView badge = UiKit.icon(this, rankIcons[i], rankNames[i]);
            row.addView(badge, new LinearLayout.LayoutParams(UiKit.dp(this, 58), UiKit.dp(this, 58)));

            LinearLayout words = new LinearLayout(this);
            words.setOrientation(LinearLayout.VERTICAL);
            words.setPadding(UiKit.dp(this, 12), 0, 0, 0);
            words.addView(label(rankNames[i], 15, true, Gravity.LEFT, unlocked ? UiKit.GOLD_LIGHT : UiKit.TEXT), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 28)));
            words.addView(label(thresholds[i] + "+ рейтингу", 10, false, Gravity.LEFT, UiKit.MUTED), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 22)));
            row.addView(words, new LinearLayout.LayoutParams(0, -1, 1f));

            TextView state = label(unlocked ? "ВІДКРИТО" : "ЗАКРИТО", 9, true, Gravity.CENTER, unlocked ? UiKit.GOLD_LIGHT : UiKit.MUTED);
            row.addView(state, new LinearLayout.LayoutParams(UiKit.dp(this, 72), UiKit.dp(this, 36)));

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 78));
            rowLp.bottomMargin = UiKit.dp(this, 9);
            path.addView(row, rowLp);
        }
        scroll.addView(path, new ScrollView.LayoutParams(-1, -2));
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        page.addView(bottomNav(1), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 66)));

        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void showModes() {
        FrameLayout root = scene(ArenaBackdropDrawable.Scene.INNER);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 15));
        page.addView(header("РЕЖИМИ ГРИ", this::showMain), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58)));

        TextView intro = label("ОБЕРИ СТІЛ", 11, true, Gravity.CENTER, UiKit.GOLD);
        intro.setLetterSpacing(0.16f);
        LinearLayout.LayoutParams introLp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 55));
        introLp.topMargin = UiKit.dp(this, 10);
        page.addView(intro, introLp);

        page.addView(modeCard("nexora_cards", "КЛАСИКА", "2–5 гравців  ·  стандартні правила", true, this::startGame), modeParams());
        page.addView(modeCard("nexora_events", "ШВИДКА ГРА", "Короткий стіл  ·  швидкий матч", false,
                () -> Toast.makeText(this, "Швидка гра готується", Toast.LENGTH_SHORT).show()), modeParams());
        page.addView(modeCard("nexora_trophy", "РЕЙТИНГОВА АРЕНА", "Матч за рейтинг  ·  сезонні нагороди", false,
                () -> Toast.makeText(this, "Рейтингова арена готується", Toast.LENGTH_SHORT).show()), modeParams());

        View spacer = new View(this);
        page.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1f));
        page.addView(bottomNav(2), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 66)));

        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private LinearLayout.LayoutParams modeParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 104));
        lp.bottomMargin = UiKit.dp(this, 12);
        return lp;
    }

    private LinearLayout modeCard(String iconName, String title, String subtitle, boolean primary, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(UiKit.dp(this, 14), UiKit.dp(this, 11), UiKit.dp(this, 14), UiKit.dp(this, 11));
        card.setBackground(UiKit.gradient(this,
                primary ? Color.rgb(15, 55, 38) : Color.rgb(7, 27, 19),
                Color.rgb(3, 14, 10),
                primary ? UiKit.GOLD_LIGHT : Color.rgb(95, 76, 47), 17));
        card.setElevation(UiKit.dp(this, primary ? 7 : 3));
        card.setClickable(true);
        card.setOnClickListener(v -> action.run());

        ImageView icon = UiKit.icon(this, iconName, title);
        card.addView(icon, new LinearLayout.LayoutParams(UiKit.dp(this, 62), UiKit.dp(this, 62)));

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(UiKit.dp(this, 14), 0, 0, 0);
        words.addView(label(title, 16, true, Gravity.LEFT, primary ? UiKit.GOLD_LIGHT : UiKit.TEXT), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 31)));
        words.addView(label(subtitle, 10, false, Gravity.LEFT, UiKit.MUTED), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 28)));
        card.addView(words, new LinearLayout.LayoutParams(0, -1, 1f));

        ImageView forward = UiKit.icon(this, "nexora_forward", "Відкрити");
        card.addView(forward, new LinearLayout.LayoutParams(UiKit.dp(this, 30), UiKit.dp(this, 30)));
        return card;
    }

    private void showProfile() {
        FrameLayout root = scene(ArenaBackdropDrawable.Scene.INNER);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 15));
        page.addView(header("ПРОФІЛЬ", this::showMain), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58)));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 22), UiKit.dp(this, 18), UiKit.dp(this, 18));
        card.setBackground(UiKit.gradient(this, Color.rgb(10, 37, 26), Color.rgb(3, 15, 10), UiKit.GOLD, 18));

        ImageView avatar = UiKit.icon(this, "nexora_profile", "Профіль");
        card.addView(avatar, new LinearLayout.LayoutParams(UiKit.dp(this, 92), UiKit.dp(this, 92)));
        card.addView(label(playerName(), 22, true, Gravity.CENTER, UiKit.TEXT), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 42)));
        card.addView(label(playerId(), 11, true, Gravity.CENTER, UiKit.GOLD_LIGHT), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 30)));
        card.addView(label("LV." + level() + "   ·   " + cups() + " РЕЙТИНГ", 11, false, Gravity.CENTER, UiKit.MUTED), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 32)));

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 240));
        cardLp.topMargin = UiKit.dp(this, 18);
        page.addView(card, cardLp);

        String[] icons = {"nexora_history", "nexora_leaders", "nexora_settings", "nexora_support"};
        String[] titles = {"ІСТОРІЯ МАТЧІВ", "СПИСОК ЛІДЕРІВ", "НАЛАШТУВАННЯ", "ПІДТРИМКА"};
        for (int i = 0; i < titles.length; i++) {
            final String title = titles[i];
            LinearLayout row = modeCard(icons[i], title, "", false,
                    () -> showSimple(title, "Розділ підготовлено для підключення логіки"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 70));
            lp.topMargin = UiKit.dp(this, 8);
            page.addView(row, lp);
        }

        View spacer = new View(this);
        page.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1f));
        page.addView(bottomNav(3), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 66)));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void showSimple(String title, String subtitle) {
        FrameLayout root = scene(ArenaBackdropDrawable.Scene.INNER);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 15));
        page.addView(header(title, this::showMain), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58)));

        View spacer1 = new View(this);
        page.addView(spacer1, new LinearLayout.LayoutParams(1, 0, 1f));
        ImageView icon = UiKit.icon(this, "nexora_cards", title);
        page.addView(icon, new LinearLayout.LayoutParams(UiKit.dp(this, 92), UiKit.dp(this, 92)));
        page.addView(label(title, 24, true, Gravity.CENTER, UiKit.TEXT), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 52)));
        page.addView(label(subtitle, 12, false, Gravity.CENTER, UiKit.MUTED), new LinearLayout.LayoutParams(-1, UiKit.dp(this, 50)));
        View spacer2 = new View(this);
        page.addView(spacer2, new LinearLayout.LayoutParams(1, 0, 1f));

        TextView back = button("НАЗАД ДО ГОЛОВНОЇ", false);
        back.setOnClickListener(v -> showMain());
        page.addView(back, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58)));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void startGame() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        showMain();
    }
}
