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
import android.widget.SeekBar;
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
 * Isolated V2 interface. The legacy MainActivity remains untouched on the backup/main branches.
 * This activity only reads/writes the same SharedPreferences and uses the same rating/Firebase systems.
 */
public class RedesignActivity extends Activity {
    private static final int RC_SIGN_IN = 2402;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private SharedPreferences settings;
    private SharedPreferences profile;
    private SharedPreferences gamePrefs;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean pendingRegistration = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        settings = getSharedPreferences("durak_arena_settings", MODE_PRIVATE);
        profile = getSharedPreferences("durak_arena_profile", MODE_PRIVATE);
        gamePrefs = getSharedPreferences("durak_arena_game", MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        immersive();
        showLoading();
    }

    private void immersive() {
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

    private void portrait() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        immersive();
    }

    private void landscape() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        immersive();
    }

    private int cups() {
        return Math.max(0, profile.getInt("player_cups", 0));
    }

    private String playerName() {
        String name = profile.getString("player_name", "FOX");
        return name == null || name.trim().isEmpty() ? "FOX" : name.trim();
    }

    private int playerLevel() {
        return Math.max(1, profile.getInt("player_level", 1));
    }

    private void showLoading() {
        portrait();
        FrameLayout root = UiKit.imageRoot(this, R.drawable.loading_background, cups(), 110);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);
        center.setPadding(UiKit.dp(this, 24), 0, UiKit.dp(this, 24), 0);

        TextView brand = UiKit.text(this, "NEXORA\nINTERACTIVE", 16, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        brand.setLetterSpacing(0.12f);
        center.addView(brand, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 64)));

        TextView logo = UiKit.text(this, "♠\nDURAK\nARENA", 34, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        logo.setTypeface(Typeface.create("serif", Typeface.BOLD));
        logo.setShadowLayer(8f, 0, 2, Color.BLACK);
        center.addView(logo, new LinearLayout.LayoutParams(-1, 0, 1f));

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(0);
        bar.setProgressTintList(ColorStateList.valueOf(UiKit.GOLD));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(33, 31, 28)));
        center.addView(bar, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 8)));

        TextView loading = UiKit.text(this, "Завантаження... 0%", 12, false, Gravity.CENTER, UiKit.TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 46));
        lp.topMargin = UiKit.dp(this, 8);
        center.addView(loading, lp);

        TextView motto = UiKit.text(this, "Тут грають легенди", 12, false, Gravity.CENTER, UiKit.MUTED);
        center.addView(motto, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 36)));

        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1, -1);
        cp.leftMargin = UiKit.dp(this, 22);
        cp.rightMargin = UiKit.dp(this, 22);
        cp.topMargin = UiKit.dp(this, 42);
        cp.bottomMargin = UiKit.dp(this, 40);
        root.addView(center, cp);
        setContentView(root);

        final int[] value = {0};
        Runnable progress = new Runnable() {
            @Override public void run() {
                value[0] = Math.min(100, value[0] + 5);
                bar.setProgress(value[0]);
                loading.setText("Завантаження... " + value[0] + "%");
                if (value[0] < 100) {
                    handler.postDelayed(this, 48);
                } else {
                    handler.postDelayed(() -> {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveFirebaseProfile(user);
                            showMain();
                        } else {
                            showLogin();
                        }
                    }, 260);
                }
            }
        };
        handler.postDelayed(progress, 120);
    }

    private void showLogin() {
        portrait();
        FrameLayout root = UiKit.imageRoot(this, R.drawable.login_background_new, 0, 105);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(UiKit.dp(this, 20), UiKit.dp(this, 48), UiKit.dp(this, 20), UiKit.dp(this, 28));

        TextView title = UiKit.text(this, "DURAK\nARENA", 34, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        title.setTypeface(Typeface.create("serif", Typeface.BOLD));
        title.setShadowLayer(9f, 0, 2, Color.BLACK);
        page.addView(title, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 140)));

        TextView subtitle = UiKit.text(this,
                "Темна арена карт. Піднімай лігу. Відкривай нові столи.",
                12, false, Gravity.CENTER, UiKit.TEXT);
        page.addView(subtitle, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 62)));

        View spacer = new View(this);
        page.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1f));

        LinearLayout authCard = new LinearLayout(this);
        authCard.setOrientation(LinearLayout.VERTICAL);
        authCard.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 14));
        authCard.setBackground(UiKit.panel(this, Color.argb(225, 5, 10, 14), UiKit.GOLD, 1, 15));

        TextView signIn = UiKit.button(this, "G   Увійти через Google", false);
        signIn.setOnClickListener(v -> {
            pendingRegistration = false;
            signIn();
        });
        authCard.addView(signIn, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58)));

        TextView divider = UiKit.text(this, "АБО", 10, true, Gravity.CENTER, UiKit.MUTED);
        authCard.addView(divider, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 34)));

        TextView register = UiKit.button(this, "РЕЄСТРАЦІЯ ЧЕРЕЗ GOOGLE", true);
        register.setOnClickListener(v -> {
            pendingRegistration = true;
            signIn();
        });
        authCard.addView(register, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 62)));

        TextView note = UiKit.text(this,
                "Продовжуючи, ви погоджуєтесь з правилами Durak Arena",
                10, false, Gravity.CENTER, UiKit.MUTED);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 42));
        np.topMargin = UiKit.dp(this, 6);
        authCard.addView(note, np);

        page.addView(authCard, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 218)));

        TextView settingsButton = UiKit.button(this, "⚙  НАЛАШТУВАННЯ", false);
        settingsButton.setOnClickListener(v -> showSettings());
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 52));
        slp.topMargin = UiKit.dp(this, 12);
        page.addView(settingsButton, slp);

        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void signIn() {
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
                    saveFirebaseProfile(firebaseAuth.getCurrentUser());
                    if (pendingRegistration && !profile.contains("registered_at")) {
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

    private void saveFirebaseProfile(FirebaseUser user) {
        SharedPreferences.Editor e = profile.edit();

        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            e.putString("player_name", user.getDisplayName().trim());
        }
        e.putString("firebase_uid", user.getUid());
        if (user.getPhotoUrl() != null) e.putString("player_photo_url", user.getPhotoUrl().toString());

        String accountKey = "player_id_" + user.getUid();
        String id = profile.getString(accountKey, "");
        if (id == null || id.trim().isEmpty()) {
            id = profile.getString("player_id", "");
            if (id == null || id.trim().isEmpty()) id = generatePlayerId();
            e.putString(accountKey, id);
        }
        e.putString("player_id", id);
        e.apply();
    }

    private String generatePlayerId() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder id = new StringBuilder("#");
        for (int i = 0; i < 8; i++) id.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return id.toString();
    }

    private void showMain() {
        portrait();
        int cups = cups();
        FrameLayout root = UiKit.imageRoot(this, R.drawable.main_menu_background, cups, 92);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(UiKit.dp(this, 7), UiKit.dp(this, 5), UiKit.dp(this, 7), UiKit.dp(this, 5));
        top.setBackground(UiKit.panel(this, Color.argb(228, 5, 12, 15), UiKit.GOLD, 1, 8));

        ImageView avatar = UiKit.icon(this, "nexora_profile", "Профіль");
        top.addView(avatar, new LinearLayout.LayoutParams(UiKit.dp(this, 45), UiKit.dp(this, 45)));

        LinearLayout identity = new LinearLayout(this);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setPadding(UiKit.dp(this, 6), 0, UiKit.dp(this, 5), 0);
        identity.addView(UiKit.text(this, playerName(), 13, true, Gravity.LEFT, UiKit.GOLD_LIGHT));
        identity.addView(UiKit.text(this,
                "Lv. " + playerLevel() + "   🏆 " + cups,
                10, false, Gravity.LEFT, UiKit.TEXT));
        top.addView(identity, new LinearLayout.LayoutParams(0, -2, 1f));

        LinearLayout wallet = new LinearLayout(this);
        wallet.setGravity(Gravity.CENTER);
        wallet.addView(UiKit.icon(this, "nexora_coin", "Монети"),
                new LinearLayout.LayoutParams(UiKit.dp(this, 22), UiKit.dp(this, 22)));
        wallet.addView(UiKit.text(this, String.valueOf(GloryRewards.balance(this, "coins")),
                10, true, Gravity.CENTER, UiKit.GOLD_LIGHT));
        wallet.addView(UiKit.icon(this, "nexora_crystal", "Кристали"),
                new LinearLayout.LayoutParams(UiKit.dp(this, 22), UiKit.dp(this, 22)));
        wallet.addView(UiKit.text(this, String.valueOf(GloryRewards.balance(this, "crystals")),
                10, true, Gravity.CENTER, UiKit.TEXT));
        top.addView(wallet, new LinearLayout.LayoutParams(UiKit.dp(this, 126), -1));

        FrameLayout.LayoutParams topLp = new FrameLayout.LayoutParams(-1, UiKit.dp(this, 62));
        topLp.leftMargin = UiKit.dp(this, 8);
        topLp.rightMargin = UiKit.dp(this, 8);
        topLp.topMargin = UiKit.dp(this, 8);
        root.addView(top, topLp);
        top.setOnClickListener(v -> showProfile());

        LinearLayout side = new LinearLayout(this);
        side.setOrientation(LinearLayout.VERTICAL);
        side.setGravity(Gravity.CENTER);
        String[] icons = {"nexora_events", "nexora_friends", "nexora_achievement", "nexora_clan"};
        String[] names = {"ПОДІЇ", "ДРУЗІ", "ЗАВДАННЯ", "КЛАН"};
        for (int i = 0; i < names.length; i++) {
            LinearLayout item = UiKit.navButton(this, icons[i], names[i], false);
            final int index = i;
            item.setOnClickListener(v -> {
                if (index == 3) showClan();
                else showSimple(names[index], "Розділ готується до онлайн-підключення.");
            });
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(UiKit.dp(this, 62), UiKit.dp(this, 58));
            ip.bottomMargin = UiKit.dp(this, 6);
            side.addView(item, ip);
        }
        FrameLayout.LayoutParams sideLp = new FrameLayout.LayoutParams(UiKit.dp(this, 66), -2);
        sideLp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        sideLp.leftMargin = UiKit.dp(this, 7);
        sideLp.topMargin = UiKit.dp(this, 30);
        root.addView(side, sideLp);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView arenaTag = UiKit.text(this,
                UiKit.arenaTitleForCups(cups) + "\n" + LeagueSystem.nameForCups(cups),
                11, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        arenaTag.setBackground(UiKit.panel(this, Color.argb(195, 5, 9, 12), LeagueSystem.colorForCups(cups), 1, 10));
        center.addView(arenaTag, new LinearLayout.LayoutParams(UiKit.dp(this, 220), UiKit.dp(this, 52)));

        TextView logo = UiKit.text(this, "DURAK\nARENA", 31, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        logo.setTypeface(Typeface.create("serif", Typeface.BOLD));
        logo.setShadowLayer(8f, 0, 2f, Color.BLACK);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 132));
        llp.topMargin = UiKit.dp(this, 8);
        center.addView(logo, llp);

        TextView play = UiKit.button(this, "♠   ГРАТИ   ♠", true);
        play.setTextSize(25);
        play.setOnClickListener(v -> showModes());
        center.addView(play, new LinearLayout.LayoutParams(UiKit.dp(this, 270), UiKit.dp(this, 72)));

        TextView mode = UiKit.button(this,
                "РЕЖИМИ ГРИ  •  " + gamePrefs.getString("selected_mode_name", "Рейтинговий"),
                false);
        mode.setOnClickListener(v -> showModes());
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(UiKit.dp(this, 245), UiKit.dp(this, 52));
        mlp.topMargin = UiKit.dp(this, 8);
        center.addView(mode, mlp);

        TextView league = UiKit.button(this,
                "🏆 " + cups + "   •   " + LeagueSystem.nameForCups(cups) + "   ›",
                false);
        league.setOnClickListener(v -> showGlory());
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(UiKit.dp(this, 285), UiKit.dp(this, 54));
        glp.topMargin = UiKit.dp(this, 8);
        center.addView(league, glp);

        FrameLayout.LayoutParams centerLp = new FrameLayout.LayoutParams(-1, -2);
        centerLp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        centerLp.bottomMargin = UiKit.dp(this, 76);
        centerLp.leftMargin = UiKit.dp(this, 64);
        centerLp.rightMargin = UiKit.dp(this, 12);
        root.addView(center, centerLp);

        TextView settingsBtn = UiKit.button(this, "⚙", false);
        settingsBtn.setTextSize(24);
        settingsBtn.setOnClickListener(v -> showSettings());
        FrameLayout.LayoutParams sbp = new FrameLayout.LayoutParams(UiKit.dp(this, 48), UiKit.dp(this, 48));
        sbp.gravity = Gravity.RIGHT | Gravity.TOP;
        sbp.rightMargin = UiKit.dp(this, 10);
        sbp.topMargin = UiKit.dp(this, 72);
        root.addView(settingsBtn, sbp);

        addBottomNav(root, 0);
        setContentView(root);
    }

    private void showProfile() {
        portrait();
        int cups = cups();
        FrameLayout root = UiKit.imageRoot(this, R.drawable.main_menu_background, cups, 170);
        ScrollView scroll = new ScrollView(this);

        LinearLayout page = pageColumn();
        page.addView(header("ПРОФІЛЬ", this::showMain));

        LinearLayout identity = new LinearLayout(this);
        identity.setGravity(Gravity.CENTER_VERTICAL);
        identity.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 10), UiKit.dp(this, 12), UiKit.dp(this, 10));
        identity.setBackground(UiKit.panel(this, Color.argb(235, 6, 13, 17), UiKit.GOLD, 1, 14));

        ImageView rank = UiKit.icon(this, LeagueSystem.assetForIndex(LeagueSystem.indexForCups(cups)), "Ранг");
        identity.addView(rank, new LinearLayout.LayoutParams(UiKit.dp(this, 92), UiKit.dp(this, 92)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(UiKit.dp(this, 10), 0, 0, 0);
        info.addView(UiKit.text(this, playerName(), 22, true, Gravity.LEFT, UiKit.GOLD_LIGHT));
        info.addView(UiKit.text(this, profile.getString("player_id", "#--------"), 11, false, Gravity.LEFT, UiKit.MUTED));
        info.addView(UiKit.text(this, "Клан: " + profile.getString("clan_name", "NEXORA"), 12, true, Gravity.LEFT, UiKit.TEXT));
        info.addView(UiKit.text(this, "Lv. " + playerLevel(), 12, true, Gravity.LEFT, UiKit.TEXT));
        identity.addView(info, new LinearLayout.LayoutParams(0, -2, 1f));
        page.addView(identity, fullCard());

        LinearLayout stats = new LinearLayout(this);
        stats.setGravity(Gravity.CENTER);
        stats.addView(stat("🏆", String.valueOf(cups), "КУБКИ"));
        stats.addView(stat("🔥", String.valueOf(profile.getInt("win_streak", 0)), "СЕРІЯ"));
        stats.addView(stat("✓", String.valueOf(profile.getInt("wins", 0)), "ПЕРЕМОГИ"));
        page.addView(stats, fullCard());

        LinearLayout league = new LinearLayout(this);
        league.setGravity(Gravity.CENTER_VERTICAL);
        league.setPadding(UiKit.dp(this, 10), UiKit.dp(this, 8), UiKit.dp(this, 10), UiKit.dp(this, 8));
        league.setBackground(UiKit.panel(this, Color.argb(235, 7, 14, 18), LeagueSystem.colorForCups(cups), 1, 12));
        ImageView badge = UiKit.icon(this, LeagueSystem.assetForIndex(LeagueSystem.indexForCups(cups)), "Ліга");
        league.addView(badge, new LinearLayout.LayoutParams(UiKit.dp(this, 76), UiKit.dp(this, 76)));
        LinearLayout leagueText = new LinearLayout(this);
        leagueText.setOrientation(LinearLayout.VERTICAL);
        leagueText.setPadding(UiKit.dp(this, 8), 0, 0, 0);
        leagueText.addView(UiKit.text(this, "ПОТОЧНА ЛІГА", 9, true, Gravity.LEFT, UiKit.MUTED));
        leagueText.addView(UiKit.text(this, LeagueSystem.nameForCups(cups), 18, true, Gravity.LEFT, UiKit.GOLD_LIGHT));
        leagueText.addView(UiKit.text(this, "🏆 " + cups + " / " + LeagueSystem.displayProgressTarget(cups), 11, false, Gravity.LEFT, UiKit.TEXT));
        league.addView(leagueText, new LinearLayout.LayoutParams(0, -2, 1f));
        page.addView(league, fullCard());

        page.addView(sectionTitle("МОЇ РОЗДІЛИ"));
        page.addView(actionRow("🏅  МЕДАЛІ", "🏆  РЕЙТИНГ", this::showMedals, this::showRanking));
        page.addView(actionRow("☷  ІСТОРІЯ МАТЧІВ", "⚙  НАЛАШТУВАННЯ",
                () -> showSimple("ІСТОРІЯ МАТЧІВ", "Перемоги: " + profile.getInt("wins", 0) + "\nПоразки: " + profile.getInt("losses", 0)),
                this::showSettings));
        page.addView(actionRow("♡  ПІДТРИМКА", "⌖  РЕГІОН",
                () -> showSimple("ПІДТРИМКА", "Підтримка Durak Arena буде підключена окремим сервісом."),
                this::showSettings));

        scroll.addView(page);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1, -1);
        sp.bottomMargin = UiKit.dp(this, 62);
        root.addView(scroll, sp);
        addBottomNav(root, 1);
        setContentView(root);
    }

    private void showGlory() {
        portrait();
        setContentView(new GloryPathV2View(this, cups(), this::showMain));
    }

    private void showShop() {
        portrait();
        FrameLayout root = darkRoot();
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(header("МАГАЗИН", this::showMain));
        page.addView(walletRow());
        page.addView(sectionTitle("ПОПУЛЯРНЕ"));

        page.addView(shopOffer("nexora_coin", "10 000 МОНЕТ", "Пакет валюти"));
        page.addView(shopOffer("nexora_crystal", "500 КРИСТАЛІВ", "Преміальна валюта"));
        page.addView(shopOffer("nexora_season", "СЕЗОННИЙ ПРОПУСК", "Ексклюзивні нагороди"));
        page.addView(shopOffer("nexora_profile", "НАБІР НОВАЧКА", "Аватар • рамка • монети"));
        page.addView(shopOffer("nexora_cards", "СКІНИ КАРТ", "Колекційні сорочки карт"));

        scroll.addView(page);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1, -1);
        sp.bottomMargin = UiKit.dp(this, 62);
        root.addView(scroll, sp);
        addBottomNav(root, 3);
        setContentView(root);
    }

    private void showInventory() {
        portrait();
        FrameLayout root = darkRoot();
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(header("ІНВЕНТАР", this::showMain));

        page.addView(sectionTitle("АВАТАРИ ТА РАМКИ"));
        page.addView(inventoryGrid(new String[]{
                "nexora_profile", "nexora_clan", "nexora_trophy",
                "nexora_achievement", "nexora_arena", "nexora_cards"
        }, new String[]{"FOX", "ВОВК", "ЛЕГЕНДА", "МЕДАЛІ", "АРЕНА", "КАРТИ"}));

        page.addView(sectionTitle("ЕМОЦІЇ"));
        page.addView(inventoryGrid(new String[]{
                "nexora_events", "nexora_friends", "nexora_help",
                "nexora_battle", "nexora_info", "nexora_play"
        }, new String[]{"ПОДІЯ", "ДРУЖБА", "ПИТАННЯ", "БІЙ", "INFO", "ГРАТИ"}));

        page.addView(sectionTitle("СТОЛИ"));
        for (int family = 0; family < 5; family++) {
            int threshold = LeagueSystem.STARTS[family * 3];
            boolean unlocked = cups() >= threshold;
            TextView table = UiKit.button(this,
                    (unlocked ? "✓ " : "🔒 ") + UiKit.arenaTitleForCups(threshold) + "  •  " + threshold + " 🏆",
                    false);
            table.setAlpha(unlocked ? 1f : 0.55f);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 56));
            tlp.bottomMargin = UiKit.dp(this, 7);
            page.addView(table, tlp);
        }

        scroll.addView(page);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1, -1);
        sp.bottomMargin = UiKit.dp(this, 62);
        root.addView(scroll, sp);
        addBottomNav(root, 4);
        setContentView(root);
    }

    private void showClan() {
        portrait();
        FrameLayout root = darkRoot();
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(header("КЛАН", this::showMain));

        LinearLayout clan = new LinearLayout(this);
        clan.setGravity(Gravity.CENTER_VERTICAL);
        clan.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 10), UiKit.dp(this, 12), UiKit.dp(this, 10));
        clan.setBackground(UiKit.panel(this, UiKit.PANEL, UiKit.GOLD, 1, 14));
        ImageView shield = UiKit.icon(this, "nexora_clan", "Клан");
        clan.addView(shield, new LinearLayout.LayoutParams(UiKit.dp(this, 88), UiKit.dp(this, 88)));
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(UiKit.text(this, profile.getString("clan_name", "NEXORA"), 22, true, Gravity.LEFT, UiKit.GOLD_LIGHT));
        info.addView(UiKit.text(this, "[NXR]", 12, true, Gravity.LEFT, UiKit.MUTED));
        info.addView(UiKit.text(this, "🏆 " + profile.getInt("clan_cups", 0), 13, true, Gravity.LEFT, UiKit.TEXT));
        clan.addView(info, new LinearLayout.LayoutParams(0, -2, 1f));
        page.addView(clan, fullCard());

        page.addView(sectionTitle("УЧАСНИКИ"));
        page.addView(memberRow("1", playerName(), "Лідер", cups()));
        page.addView(memberRow("2", "Вільне місце", "Запросити", 0));
        page.addView(memberRow("3", "Вільне місце", "Запросити", 0));
        page.addView(memberRow("4", "Вільне місце", "Запросити", 0));

        TextView chat = UiKit.button(this, "ЧАТ КЛАНУ", false);
        chat.setOnClickListener(v -> Toast.makeText(this, "Онлайн-чат буде підключено після серверної частини.", Toast.LENGTH_SHORT).show());
        page.addView(chat, fullCardHeight(54));

        scroll.addView(page);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void showSettings() {
        portrait();
        FrameLayout root = darkRoot();
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(header("НАЛАШТУВАННЯ", () -> {
            if (firebaseAuth.getCurrentUser() == null) showLogin(); else showMain();
        }));

        page.addView(settingChoice("🌐  МОВА", new String[]{"Українська", "English", "Deutsch", "Español"},
                new String[]{"uk", "en", "de", "es"}, "language", "uk"));
        page.addView(settingChoice("⌖  РЕГІОН", new String[]{"Україна", "Європа", "Америка"},
                new String[]{"UA", "EU", "US"}, "region", "UA"));
        page.addView(seekSetting("🔊  ЗВУК", "sound", 75));
        page.addView(seekSetting("♫  МУЗИКА", "music", 65));
        page.addView(seekSetting("✦  ЕФЕКТИ", "effects", 70));

        TextView graphics = UiKit.button(this, "▣  ГРАФІКА     ВИСОКА  ›", false);
        page.addView(graphics, fullCardHeight(56));
        TextView notifications = UiKit.button(this, "●  СПОВІЩЕННЯ     УВІМКНЕНО", false);
        page.addView(notifications, fullCardHeight(56));
        TextView support = UiKit.button(this, "?  ПІДТРИМКА  ›", false);
        support.setOnClickListener(v -> showSimple("ПІДТРИМКА", "Опишіть проблему в майбутньому чаті підтримки."));
        page.addView(support, fullCardHeight(56));
        TextView authors = UiKit.button(this, "N  АВТОР ГРИ     FOX / NEXORA INTERACTIVE", false);
        page.addView(authors, fullCardHeight(56));

        scroll.addView(page);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void showModes() {
        portrait();
        FrameLayout root = darkRoot();
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(header("РЕЖИМИ ГРИ", this::showMain));

        addMode(page, "РЕЙТИНГОВИЙ", "Гра на кубки • зміна ліги", "ranked", "🏆");
        addMode(page, "КЛАСИЧНИЙ", "Гра до останнього", "classic", "♠");
        addMode(page, "НА ВИЛІТ", "Турнірний формат", "elimination", "♛");
        addMode(page, "ПРИВАТНИЙ", "Гра з друзями", "private", "🔒");
        addMode(page, "ТУРНІРИ", "Сезонні змагання", "tournament", "★");

        scroll.addView(page);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void addMode(LinearLayout page, String name, String subtitle, String id, String symbol) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 10), UiKit.dp(this, 12), UiKit.dp(this, 10));
        card.setBackground(UiKit.gradient(this,
                Color.rgb(30, 19, 17), Color.rgb(7, 13, 18), UiKit.GOLD, 13));

        TextView icon = UiKit.text(this, symbol, 34, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        card.addView(icon, new LinearLayout.LayoutParams(UiKit.dp(this, 70), UiKit.dp(this, 70)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(UiKit.text(this, name, 18, true, Gravity.LEFT, UiKit.GOLD_LIGHT));
        text.addView(UiKit.text(this, subtitle, 11, false, Gravity.LEFT, UiKit.TEXT));
        card.addView(text, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView go = UiKit.text(this, "›", 34, true, Gravity.CENTER, UiKit.GOLD);
        card.addView(go, new LinearLayout.LayoutParams(UiKit.dp(this, 40), -1));

        card.setClickable(true);
        card.setOnClickListener(v -> {
            gamePrefs.edit()
                    .putString("selected_mode_id", id)
                    .putString("selected_mode_name", name)
                    .putString("selected_mode_icon", symbol)
                    .apply();

            if ("ranked".equals(id)) {
                startMatchmaking();
            } else {
                Toast.makeText(this, name + ": режим обрано", Toast.LENGTH_SHORT).show();
                showMain();
            }
        });

        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 92));
        cp.bottomMargin = UiKit.dp(this, 9);
        page.addView(card, cp);
    }

    private void startMatchmaking() {
        portrait();
        int currentCups = cups();
        setContentView(MatchmakingView.create(
                this,
                playerName(),
                currentCups,
                new MatchmakingView.Listener() {
                    @Override public void onCancel() {
                        showModes();
                    }

                    @Override public void onMatchFound() {
                        landscape();
                        handler.postDelayed(() -> setContentView(
                                RankedGameView.create(
                                        RedesignActivity.this,
                                        playerName(),
                                        currentCups,
                                        () -> {
                                            portrait();
                                            handler.postDelayed(RedesignActivity.this::showMain, 180);
                                        }
                                )
                        ), 180);
                    }
                }
        ));
    }

    private void showMedals() {
        portrait();
        FrameLayout root = darkRoot();
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = pageColumn();
        page.addView(header("МЕДАЛІ", this::showProfile));

        String[] icons = {"nexora_achievement", "nexora_battle", "nexora_cards",
                "nexora_trophy", "nexora_season", "nexora_arena"};
        String[] names = {"ПЕРШІ КРОКИ", "МАЙСТЕР АТАК", "КОЛЕКЦІОНЕР",
                "СЕРІЯ ПЕРЕМОГ", "СЕЗОННИЙ ГЕРОЙ", "КОРОЛЬ СТОЛУ"};
        page.addView(inventoryGrid(icons, names));

        scroll.addView(page);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void showRanking() {
        portrait();
        FrameLayout root = darkRoot();
        LinearLayout page = pageColumn();
        page.addView(header("РЕЙТИНГ", this::showProfile));
        page.addView(UiKit.text(this, "ГЛОБАЛЬНИЙ РЕЙТИНГ", 11, true, Gravity.CENTER, UiKit.MUTED),
                new LinearLayout.LayoutParams(-1, UiKit.dp(this, 38)));

        String[] names = {"Shadow", "Raven", "Night", playerName(), "Ghost", "Storm", "Alex"};
        int[] scores = {8540, 7230, 6980, cups(), 6120, 5980, 5430};
        for (int i = 0; i < names.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(UiKit.dp(this, 10), 0, UiKit.dp(this, 10), 0);
            row.setBackground(UiKit.panel(this,
                    names[i].equals(playerName()) ? Color.rgb(23, 34, 40) : UiKit.PANEL,
                    names[i].equals(playerName()) ? UiKit.GOLD_LIGHT : Color.rgb(70, 61, 48),
                    1, 8));
            row.addView(UiKit.text(this, String.valueOf(i + 1), 15, true, Gravity.CENTER, UiKit.GOLD_LIGHT),
                    new LinearLayout.LayoutParams(UiKit.dp(this, 40), -1));
            row.addView(UiKit.text(this, names[i], 14, true, Gravity.LEFT, UiKit.TEXT),
                    new LinearLayout.LayoutParams(0, -1, 1f));
            row.addView(UiKit.text(this, "🏆 " + scores[i], 13, true, Gravity.RIGHT | Gravity.CENTER_VERTICAL, UiKit.GOLD_LIGHT),
                    new LinearLayout.LayoutParams(UiKit.dp(this, 100), -1));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 52));
            rp.bottomMargin = UiKit.dp(this, 6);
            page.addView(row, rp);
        }

        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void showSimple(String title, String body) {
        portrait();
        FrameLayout root = darkRoot();
        LinearLayout page = pageColumn();
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.addView(header(title, this::showMain));
        TextView message = UiKit.text(this, body, 15, false, Gravity.CENTER, UiKit.TEXT);
        message.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 18), UiKit.dp(this, 18), UiKit.dp(this, 18));
        message.setBackground(UiKit.panel(this, UiKit.PANEL, UiKit.GOLD, 1, 12));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 180));
        mp.topMargin = UiKit.dp(this, 30);
        page.addView(message, mp);
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private FrameLayout darkRoot() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(UiKit.BG);
        View topGlow = new View(this);
        topGlow.setBackground(UiKit.gradient(this,
                Color.rgb(19, 13, 11), UiKit.BG, Color.TRANSPARENT, 0));
        FrameLayout.LayoutParams gp = new FrameLayout.LayoutParams(-1, UiKit.dp(this, 220));
        root.addView(topGlow, gp);
        return root;
    }

    private LinearLayout pageColumn() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 12),
                UiKit.dp(this, 12), UiKit.dp(this, 24));
        return page;
    }

    private LinearLayout header(String title, Runnable backAction) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = UiKit.button(this, "‹", false);
        back.setTextSize(30);
        back.setOnClickListener(v -> backAction.run());
        header.addView(back, new LinearLayout.LayoutParams(UiKit.dp(this, 48), UiKit.dp(this, 48)));

        TextView t = UiKit.text(this, title, 20, true, Gravity.CENTER, UiKit.GOLD_LIGHT);
        header.addView(t, new LinearLayout.LayoutParams(0, UiKit.dp(this, 48), 1f));

        View spacer = new View(this);
        header.addView(spacer, new LinearLayout.LayoutParams(UiKit.dp(this, 48), UiKit.dp(this, 48)));

        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58));
        header.setLayoutParams(hp);
        return header;
    }

    private LinearLayout.LayoutParams fullCard() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = UiKit.dp(this, 10);
        return p;
    }

    private LinearLayout.LayoutParams fullCardHeight(int h) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, UiKit.dp(this, h));
        p.bottomMargin = UiKit.dp(this, 8);
        return p;
    }

    private TextView sectionTitle(String title) {
        TextView t = UiKit.text(this, title, 11, true, Gravity.LEFT | Gravity.CENTER_VERTICAL, UiKit.GOLD);
        t.setPadding(UiKit.dp(this, 5), 0, 0, 0);
        return t;
    }

    private LinearLayout stat(String symbol, String value, String label) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackground(UiKit.panel(this, UiKit.PANEL, Color.rgb(82, 67, 45), 1, 9));
        box.addView(UiKit.text(this, symbol + " " + value, 16, true, Gravity.CENTER, UiKit.GOLD_LIGHT));
        box.addView(UiKit.text(this, label, 8, true, Gravity.CENTER, UiKit.MUTED));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, UiKit.dp(this, 72), 1f);
        lp.setMargins(UiKit.dp(this, 2), 0, UiKit.dp(this, 2), 0);
        box.setLayoutParams(lp);
        return box;
    }

    private LinearLayout actionRow(String leftText, String rightText, Runnable leftAction, Runnable rightAction) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        TextView left = UiKit.button(this, leftText, false);
        TextView right = UiKit.button(this, rightText, false);
        left.setTextSize(11);
        right.setTextSize(11);
        left.setOnClickListener(v -> leftAction.run());
        right.setOnClickListener(v -> rightAction.run());
        LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, UiKit.dp(this, 54), 1f);
        a.rightMargin = UiKit.dp(this, 4);
        LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(0, UiKit.dp(this, 54), 1f);
        b.leftMargin = UiKit.dp(this, 4);
        row.addView(left, a);
        row.addView(right, b);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 62));
        rp.bottomMargin = UiKit.dp(this, 4);
        row.setLayoutParams(rp);
        return row;
    }

    private LinearLayout walletRow() {
        LinearLayout wallet = new LinearLayout(this);
        wallet.setGravity(Gravity.CENTER);
        wallet.setBackground(UiKit.panel(this, UiKit.PANEL, UiKit.GOLD, 1, 10));

        ImageView coin = UiKit.icon(this, "nexora_coin", "Монети");
        wallet.addView(coin, new LinearLayout.LayoutParams(UiKit.dp(this, 30), UiKit.dp(this, 30)));
        wallet.addView(UiKit.text(this, String.valueOf(GloryRewards.balance(this, "coins")),
                14, true, Gravity.CENTER, UiKit.GOLD_LIGHT),
                new LinearLayout.LayoutParams(0, UiKit.dp(this, 48), 1f));

        ImageView crystal = UiKit.icon(this, "nexora_crystal", "Кристали");
        wallet.addView(crystal, new LinearLayout.LayoutParams(UiKit.dp(this, 30), UiKit.dp(this, 30)));
        wallet.addView(UiKit.text(this, String.valueOf(GloryRewards.balance(this, "crystals")),
                14, true, Gravity.CENTER, UiKit.TEXT),
                new LinearLayout.LayoutParams(0, UiKit.dp(this, 48), 1f));

        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 52));
        wp.bottomMargin = UiKit.dp(this, 12);
        wallet.setLayoutParams(wp);
        return wallet;
    }

    private LinearLayout shopOffer(String iconName, String title, String subtitle) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(UiKit.dp(this, 10), UiKit.dp(this, 8), UiKit.dp(this, 10), UiKit.dp(this, 8));
        card.setBackground(UiKit.panel(this, UiKit.PANEL, UiKit.GOLD, 1, 12));

        ImageView icon = UiKit.icon(this, iconName, title);
        card.addView(icon, new LinearLayout.LayoutParams(UiKit.dp(this, 68), UiKit.dp(this, 68)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(UiKit.dp(this, 8), 0, 0, 0);
        text.addView(UiKit.text(this, title, 15, true, Gravity.LEFT, UiKit.GOLD_LIGHT));
        text.addView(UiKit.text(this, subtitle, 10, false, Gravity.LEFT, UiKit.MUTED));
        card.addView(text, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView buy = UiKit.button(this, "ПЕРЕГЛЯНУТИ", true);
        buy.setTextSize(10);
        buy.setOnClickListener(v -> Toast.makeText(this,
                "Платежі ще не підключені. Це безпечний попередній перегляд магазину.",
                Toast.LENGTH_SHORT).show());
        card.addView(buy, new LinearLayout.LayoutParams(UiKit.dp(this, 102), UiKit.dp(this, 44)));

        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 88));
        cp.bottomMargin = UiKit.dp(this, 8);
        card.setLayoutParams(cp);
        return card;
    }

    private LinearLayout inventoryGrid(String[] icons, String[] names) {
        LinearLayout all = new LinearLayout(this);
        all.setOrientation(LinearLayout.VERTICAL);

        for (int rowIndex = 0; rowIndex < (names.length + 2) / 3; rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            for (int col = 0; col < 3; col++) {
                int index = rowIndex * 3 + col;
                if (index >= names.length) {
                    View empty = new View(this);
                    row.addView(empty, new LinearLayout.LayoutParams(0, UiKit.dp(this, 102), 1f));
                    continue;
                }

                LinearLayout item = new LinearLayout(this);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setGravity(Gravity.CENTER);
                item.setBackground(UiKit.panel(this, UiKit.PANEL, Color.rgb(98, 74, 42), 1, 10));

                ImageView icon = UiKit.icon(this, icons[index], names[index]);
                item.addView(icon, new LinearLayout.LayoutParams(UiKit.dp(this, 58), UiKit.dp(this, 58)));
                item.addView(UiKit.text(this, names[index], 9, true, Gravity.CENTER, UiKit.TEXT),
                        new LinearLayout.LayoutParams(-1, UiKit.dp(this, 26)));

                LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, UiKit.dp(this, 98), 1f);
                ip.setMargins(UiKit.dp(this, 3), UiKit.dp(this, 3), UiKit.dp(this, 3), UiKit.dp(this, 3));
                row.addView(item, ip);
            }
            all.addView(row, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 104)));
        }

        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, -2);
        ap.bottomMargin = UiKit.dp(this, 12);
        all.setLayoutParams(ap);
        return all;
    }

    private LinearLayout memberRow(String number, String name, String role, int score) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(UiKit.dp(this, 8), 0, UiKit.dp(this, 8), 0);
        row.setBackground(UiKit.panel(this, UiKit.PANEL, Color.rgb(78, 65, 48), 1, 8));
        row.addView(UiKit.text(this, number, 14, true, Gravity.CENTER, UiKit.GOLD_LIGHT),
                new LinearLayout.LayoutParams(UiKit.dp(this, 36), -1));
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(UiKit.text(this, name, 13, true, Gravity.LEFT, UiKit.TEXT));
        text.addView(UiKit.text(this, role, 9, false, Gravity.LEFT, UiKit.MUTED));
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(UiKit.text(this, score > 0 ? "🏆 " + score : "＋",
                11, true, Gravity.CENTER, UiKit.GOLD_LIGHT),
                new LinearLayout.LayoutParams(UiKit.dp(this, 84), -1));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 58));
        rp.bottomMargin = UiKit.dp(this, 6);
        row.setLayoutParams(rp);
        return row;
    }

    private LinearLayout settingChoice(String title, String[] labels, String[] values, String key, String fallback) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(UiKit.dp(this, 10), UiKit.dp(this, 8), UiKit.dp(this, 10), UiKit.dp(this, 8));
        box.setBackground(UiKit.panel(this, UiKit.PANEL, Color.rgb(88, 70, 44), 1, 10));
        box.addView(UiKit.text(this, title, 14, true, Gravity.LEFT, UiKit.GOLD_LIGHT));

        LinearLayout row = new LinearLayout(this);
        String active = settings.getString(key, fallback);
        for (int i = 0; i < labels.length; i++) {
            final String value = values[i];
            TextView b = UiKit.button(this, labels[i], value.equals(active));
            b.setTextSize(10);
            b.setOnClickListener(v -> {
                settings.edit().putString(key, value).apply();
                showSettings();
            });
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, UiKit.dp(this, 42), 1f);
            bp.setMargins(UiKit.dp(this, 2), UiKit.dp(this, 5), UiKit.dp(this, 2), 0);
            row.addView(b, bp);
        }
        box.addView(row);
        LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 92));
        op.bottomMargin = UiKit.dp(this, 8);
        box.setLayoutParams(op);
        return box;
    }

    private LinearLayout seekSetting(String title, String key, int fallback) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(UiKit.dp(this, 10), UiKit.dp(this, 6), UiKit.dp(this, 10), UiKit.dp(this, 6));
        box.setBackground(UiKit.panel(this, UiKit.PANEL, Color.rgb(88, 70, 44), 1, 10));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = UiKit.text(this, title, 14, true, Gravity.LEFT, UiKit.TEXT);
        TextView value = UiKit.text(this, "", 11, true, Gravity.RIGHT | Gravity.CENTER_VERTICAL, UiKit.GOLD_LIGHT);
        top.addView(label, new LinearLayout.LayoutParams(0, UiKit.dp(this, 28), 1f));
        top.addView(value, new LinearLayout.LayoutParams(UiKit.dp(this, 60), UiKit.dp(this, 28)));
        box.addView(top);

        SeekBar bar = new SeekBar(this);
        bar.setMax(100);
        int current = settings.getInt(key, fallback);
        bar.setProgress(current);
        value.setText(current + "%");
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value.setText(progress + "%");
                settings.edit().putInt(key, progress).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        box.addView(bar, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 42)));

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, UiKit.dp(this, 78));
        bp.bottomMargin = UiKit.dp(this, 8);
        box.setLayoutParams(bp);
        return box;
    }

    private void addBottomNav(FrameLayout root, int active) {
        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER);
        String[] icons = {"nexora_menu", "nexora_profile", "nexora_trophy", "nexora_shop", "nexora_inventory"};
        String[] labels = {"ГОЛОВНА", "ПРОФІЛЬ", "СЛАВА", "МАГАЗИН", "ІНВЕНТАР"};

        for (int i = 0; i < labels.length; i++) {
            LinearLayout item = UiKit.navButton(this, icons[i], labels[i], i == active);
            final int index = i;
            item.setOnClickListener(v -> {
                if (index == 0) showMain();
                else if (index == 1) showProfile();
                else if (index == 2) showGlory();
                else if (index == 3) showShop();
                else showInventory();
            });
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, -1, 1f);
            ip.setMargins(UiKit.dp(this, 1), 0, UiKit.dp(this, 1), 0);
            bottom.addView(item, ip);
        }

        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-1, UiKit.dp(this, 60));
        bp.gravity = Gravity.BOTTOM;
        bp.leftMargin = UiKit.dp(this, 4);
        bp.rightMargin = UiKit.dp(this, 4);
        bp.bottomMargin = UiKit.dp(this, 3);
        root.addView(bottom, bp);
    }
}
