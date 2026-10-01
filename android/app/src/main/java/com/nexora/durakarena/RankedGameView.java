package com.nexora.durakarena;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class RankedGameView {
    private RankedGameView() {}

    public interface Listener { void onExit(); }

    public static View create(Context context, String playerName, int cups, Listener listener) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(36, 36, 36, 36);
        root.setBackgroundColor(Color.rgb(16, 64, 46));

        TextView title = new TextView(context);
        title.setText("РЕЙТИНГОВИЙ МАТЧ");
        title.setTextColor(Color.rgb(240, 196, 82));
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView player = new TextView(context);
        player.setText(playerName + "  •  " + LeagueSystem.nameForCups(cups) + "  •  " + cups + " 🏆");
        player.setTextColor(Color.WHITE);
        player.setTextSize(17);
        player.setGravity(Gravity.CENTER);
        player.setPadding(0, 24, 0, 24);
        root.addView(player);

        TextView table = new TextView(context);
        table.setText("♠     ♦     ♣     ♥\n\nСТІЛ DURAK ARENA");
        table.setTextColor(Color.WHITE);
        table.setTextSize(24);
        table.setGravity(Gravity.CENTER);
        table.setPadding(0, 70, 0, 70);
        root.addView(table);

        Button exit = new Button(context);
        exit.setText("ВИЙТИ");
        exit.setOnClickListener(v -> listener.onExit());
        root.addView(exit);
        return root;
    }
}
