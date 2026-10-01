package com.nexora.durakarena;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MatchmakingView {
    private MatchmakingView() {}

    public interface Listener {
        void onCancel();
        void onMatchFound();
    }

    public static View create(Context context, String playerName, int cups, Listener listener) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);
        root.setBackgroundColor(Color.rgb(8, 18, 18));

        TextView title = new TextView(context);
        title.setText("ПОШУК СУПЕРНИКА");
        title.setTextColor(Color.rgb(235, 190, 78));
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView info = new TextView(context);
        info.setText(playerName + "  •  " + cups + " 🏆");
        info.setTextColor(Color.WHITE);
        info.setTextSize(18);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 28, 0, 28);
        root.addView(info);

        Button play = new Button(context);
        play.setText("МАТЧ ЗНАЙДЕНО — ГРАТИ");
        play.setOnClickListener(v -> listener.onMatchFound());
        root.addView(play);

        Button cancel = new Button(context);
        cancel.setText("СКАСУВАТИ");
        cancel.setOnClickListener(v -> listener.onCancel());
        root.addView(cancel);
        return root;
    }
}
