package com.nexora.durakarena;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

public class RedesignActivity extends Activity {

    private static final int BG = Color.rgb(4, 13, 11);
    private static final int PANEL = Color.rgb(8, 28, 23);
    private static final int PANEL_2 = Color.rgb(12, 38, 31);
    private static final int GOLD = Color.rgb(207, 164, 73);
    private static final int GOLD_LIGHT = Color.rgb(241, 216, 155);
    private static final int TEXT = Color.rgb(239, 242, 235);
    private static final int MUTED = Color.rgb(150, 166, 157);
    private static final int GREEN = Color.rgb(27, 105, 73);

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        showLoading();
    }

    private GradientDrawable rounded(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    private TextView centered(String value, int size, int color, boolean bold) {
        TextView t = text(value, size, color, bold);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private Button primaryButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.rgb(255, 243, 212));
        b.setTextSize(18);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackground(rounded(Color.rgb(31, 103, 70), GOLD, 18));
        b.setPadding(dp(18), dp(12), dp(18), dp(12));
        return b;
    }

    private Button secondaryButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(GOLD_LIGHT);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackground(rounded(PANEL_2, Color.rgb(91, 111, 96), 16));
        b.setPadding(dp(16), dp(10), dp(16), dp(10));
        return b;
    }

    private LinearLayout card(String eyebrow, String title, String desc) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(16), dp(18), dp(16));
        box.setBackground(rounded(PANEL, Color.rgb(64, 88, 75), 18));

        TextView e = text(eyebrow.toUpperCase(), 11, GOLD, true);
        e.setLetterSpacing(0.12f);
        box.addView(e);

        TextView h = text(title, 21, TEXT, true);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hp.topMargin = dp(6);
        box.addView(h, hp);

        TextView d = text(desc, 14, MUTED, false);
        d.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams dpv = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dpv.topMargin = dp(5);
        box.addView(d, dpv);
        return box;
    }

    private FrameLayout baseRoot() {
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new ArenaBackground());
        return root;
    }

    private LinearLayout contentColumn(FrameLayout root, boolean scroll) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20), dp(32), dp(20), dp(24));

        if (scroll) {
            ScrollView sv = new ScrollView(this);
            sv.setFillViewport(true);
            sv.addView(col, new ScrollView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            root.addView(sv, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            root.addView(col, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return col;
    }

    private void addLogo(LinearLayout col, boolean compact) {
        TextView studio = centered("NEXORA  INTERACTIVE", compact ? 11 : 12, GOLD, true);
        studio.setLetterSpacing(0.22f);
        col.addView(studio);

        TextView logo = centered("DURAK", compact ? 31 : 42, TEXT, true);
        logo.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(compact ? 4 : 8);
        col.addView(logo, lp);

        TextView arena = centered("ARENA", compact ? 19 : 24, GOLD_LIGHT, true);
        arena.setLetterSpacing(0.25f);
        col.addView(arena);
    }

    private void showLoading() {
        FrameLayout root = baseRoot();
        LinearLayout col = contentColumn(root, false);
        col.setGravity(Gravity.CENTER);

        Space top = new Space(this);
        col.addView(top, new LinearLayout.LayoutParams(1, 0, 1f));

        addLogo(col, false);

        TextView symbol = centered("♠", 62, GOLD_LIGHT, false);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.topMargin = dp(26);
        col.addView(symbol, sp);

        TextView status = centered("ПІДГОТОВКА АРЕНИ", 12, MUTED, true);
        status.setLetterSpacing(0.16f);
        LinearLayout.LayoutParams st = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        st.topMargin = dp(34);
        col.addView(status, st);

        View progress = new View(this);
        GradientDrawable pd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(63, 124, 86), GOLD});
        pd.setCornerRadius(dp(3));
        progress.setBackground(pd);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(dp(220), dp(4));
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        pp.topMargin = dp(12);
        col.addView(progress, pp);

        Space bottom = new Space(this);
        col.addView(bottom, new LinearLayout.LayoutParams(1, 0, 1f));

        TextView version = centered("NEXORA BUILD • UI PREVIEW", 10, Color.rgb(100, 119, 108), false);
        col.addView(version);

        setContentView(root);
        new Handler(Looper.getMainLooper()).postDelayed(this::showLogin, 1500);
    }

    private void showLogin() {
        FrameLayout root = baseRoot();
        LinearLayout col = contentColumn(root, false);
        col.setGravity(Gravity.CENTER_HORIZONTAL);

        Space top = new Space(this);
        col.addView(top, new LinearLayout.LayoutParams(1, 0, 1f));
        addLogo(col, false);

        TextView tag = centered("Класична гра. Нова арена.", 15, MUTED, false);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(18);
        col.addView(tag, tp);

        LinearLayout loginCard = card("ПРОФІЛЬ ГРАВЦЯ", "Увійти в Durak Arena",
                "Авторизація відкриває профіль, рейтинг, медалі та сезонний прогрес.");
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.topMargin = dp(38);
        col.addView(loginCard, cp);

        Button google = primaryButton("Увійти через Google");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        bp.topMargin = dp(14);
        loginCard.addView(google, bp);
        google.setOnClickListener(v -> showHome());

        TextView note = centered("Тестова APK: кнопка одразу відкриває нове меню", 11, Color.rgb(112, 132, 119), false);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        np.topMargin = dp(10);
        loginCard.addView(note, np);

        Space bottom = new Space(this);
        col.addView(bottom, new LinearLayout.LayoutParams(1, 0, 1f));
        setContentView(root);
    }

    private void showHome() {
        FrameLayout root = baseRoot();
        LinearLayout col = contentColumn(root, true);

        LinearLayout profile = new LinearLayout(this);
        profile.setGravity(Gravity.CENTER_VERTICAL);
        profile.setPadding(dp(14), dp(12), dp(14), dp(12));
        profile.setBackground(rounded(PANEL, Color.rgb(68, 91, 78), 18));

        TextView avatar = centered("F", 18, BG, true);
        avatar.setBackground(rounded(GOLD, GOLD_LIGHT, 14));
        profile.addView(avatar, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout who = new LinearLayout(this);
        who.setOrientation(LinearLayout.VERTICAL);
        who.setPadding(dp(12), 0, 0, 0);
        who.addView(text("Фокс", 17, TEXT, true));
        who.addView(text("#QR12657R  •  ПРОФІ", 11, GOLD, true));
        profile.addView(who, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView money = text("$ 417 666", 14, GOLD_LIGHT, true);
        profile.addView(money);
        col.addView(profile);

        LinearLayout.LayoutParams logoP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        logoP.topMargin = dp(34);
        LinearLayout logoWrap = new LinearLayout(this);
        logoWrap.setOrientation(LinearLayout.VERTICAL);
        addLogo(logoWrap, false);
        col.addView(logoWrap, logoP);

        TextView season = centered("СЕЗОН 01  •  1250 РЕЙТИНГ", 12, MUTED, true);
        LinearLayout.LayoutParams se = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        se.topMargin = dp(12);
        col.addView(season, se);

        Button play = primaryButton("ГРАТИ");
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(68));
        pp.topMargin = dp(28);
        col.addView(play, pp);
        play.setOnClickListener(v -> showModes());

        LinearLayout glory = card("ШЛЯХ СЛАВИ", "Профі • 1250 / 2000", "Підіймай рейтинг, відкривай ліги та сезонні нагороди.");
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gp.topMargin = dp(16);
        col.addView(glory, gp);
        glory.setOnClickListener(v -> showGlory());

        LinearLayout modes = card("РЕЖИМИ", "Обери свій стіл", "Класика, швидка гра та рейтингова арена — без зайвого шуму на екрані.");
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mp.topMargin = dp(12);
        col.addView(modes, mp);
        modes.setOnClickListener(v -> showModes());

        addBottomNav(col);
        setContentView(root);
    }

    private void addBottomNav(LinearLayout col) {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(8), dp(8), dp(8));
        nav.setBackground(rounded(Color.rgb(5, 20, 16), Color.rgb(56, 79, 66), 18));

        Button home = secondaryButton("Головна");
        Button glory = secondaryButton("Слава");
        Button rating = secondaryButton("Рейтинг");
        Button profile = secondaryButton("Профіль");

        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, dp(48), 1f);
        np.setMargins(dp(3), 0, dp(3), 0);
        nav.addView(home, np);
        nav.addView(glory, new LinearLayout.LayoutParams(np));
        nav.addView(rating, new LinearLayout.LayoutParams(np));
        nav.addView(profile, new LinearLayout.LayoutParams(np));

        home.setOnClickListener(v -> showHome());
        glory.setOnClickListener(v -> showGlory());
        rating.setOnClickListener(v -> showSimple("РЕЙТИНГ", "Ліга Профі", "1250 очок • сезонна позиція №4"));
        profile.setOnClickListener(v -> showSimple("ПРОФІЛЬ", "Фокс", "ID #QR12657R • Профі • серія перемог 4"));

        LinearLayout.LayoutParams navp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        navp.topMargin = dp(18);
        col.addView(nav, navp);
    }

    private void showGlory() {
        FrameLayout root = baseRoot();
        LinearLayout col = contentColumn(root, true);
        header(col, "ШЛЯХ СЛАВИ", "Сезонний прогрес");

        String[][] ranks = {
                {"НОВАЧОК", "0", "Стартова арена"},
                {"ПРОФІ", "1250", "Поточна ліга"},
                {"МАЙСТЕР", "2000", "Преміальна нагорода"},
                {"ЛЕГЕНДА", "3200", "Вершина сезону"}
        };

        for (int i = 0; i < ranks.length; i++) {
            LinearLayout c = card("ЕТАП " + (i + 1), ranks[i][0] + "  •  " + ranks[i][1], ranks[i][2]);
            if (i == 1) c.setBackground(rounded(Color.rgb(13, 50, 38), GOLD, 18));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.topMargin = dp(12);
            col.addView(c, p);
        }

        Button modes = primaryButton("ДО РЕЖИМІВ ГРИ");
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        mp.topMargin = dp(20);
        col.addView(modes, mp);
        modes.setOnClickListener(v -> showModes());
        addBottomNav(col);
        setContentView(root);
    }

    private void showModes() {
        FrameLayout root = baseRoot();
        LinearLayout col = contentColumn(root, true);
        header(col, "РЕЖИМИ ГРИ", "Обери формат столу");

        LinearLayout classic = card("КЛАСИКА", "Дурак • 2–5 гравців", "Звичайні правила, повна колода та спокійний темп.");
        LinearLayout quick = card("ШВИДКА ГРА", "Швидкий матч", "Коротка сесія для гри з телефону за кілька хвилин.");
        LinearLayout ranked = card("РЕЙТИНГОВА", "Арена Профі", "Матч впливає на рейтинг, серію перемог і шлях слави.");
        LinearLayout privateRoom = card("ПРИВАТНА", "Гра з друзями", "Створи кімнату або приєднайся за кодом.");

        LinearLayout[] cards = {classic, quick, ranked, privateRoom};
        for (LinearLayout c : cards) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.topMargin = dp(12);
            col.addView(c, p);
            c.setOnClickListener(v -> showSimple("ПОШУК СТОЛУ", "Підбір суперників", "Це тест дизайну. Ігрову логіку підв’яжемо після затвердження інтерфейсу."));
        }

        addBottomNav(col);
        setContentView(root);
    }

    private void showSimple(String title, String heading, String description) {
        FrameLayout root = baseRoot();
        LinearLayout col = contentColumn(root, false);
        header(col, title, "Durak Arena");
        Space s = new Space(this);
        col.addView(s, new LinearLayout.LayoutParams(1, 0, 1f));
        LinearLayout c = card(title, heading, description);
        col.addView(c);
        Button back = secondaryButton("Назад до меню");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        bp.topMargin = dp(16);
        col.addView(back, bp);
        back.setOnClickListener(v -> showHome());
        Space b = new Space(this);
        col.addView(b, new LinearLayout.LayoutParams(1, 0, 1f));
        setContentView(root);
    }

    private void header(LinearLayout col, String title, String subtitle) {
        TextView back = text("‹  НАЗАД", 13, GOLD, true);
        back.setPadding(0, dp(4), 0, dp(12));
        back.setOnClickListener(v -> showHome());
        col.addView(back);

        TextView h = text(title, 29, TEXT, true);
        h.setLetterSpacing(0.05f);
        col.addView(h);

        TextView s = text(subtitle, 13, MUTED, false);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.topMargin = dp(4);
        col.addView(s, sp);
    }

    private class ArenaBackground extends GradientDrawable {
        ArenaBackground() {
            super(GradientDrawable.Orientation.TL_BR,
                    new int[]{Color.rgb(2, 9, 8), Color.rgb(6, 28, 22), Color.rgb(3, 14, 12)});
        }
    }
}
