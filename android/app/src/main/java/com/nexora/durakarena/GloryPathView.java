package com.nexora.durakarena;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

/** Lightweight 2D Glory Path. Uses dedicated transparent PNG for every league tier. */
public class GloryPathView extends FrameLayout {
    private final Context context;
    private final int playerCups;
    private final Runnable onBack;
    private final int BG=Color.rgb(2,8,7), PANEL=Color.rgb(4,18,14), CURRENT=Color.rgb(7,29,22);
    private final int GOLD=Color.rgb(211,163,76), GOLD_LIGHT=Color.rgb(246,211,139);
    private final int TEXT=Color.rgb(239,226,193), MUTED=Color.rgb(155,149,132), LOCKED=Color.rgb(105,103,95);
    private final int LINE_DARK=Color.rgb(69,60,43);

    public GloryPathView(Context context,int playerCups,Runnable onBack){
        super(context); this.context=context; this.playerCups=Math.max(0,playerCups); this.onBack=onBack; build();
    }

    private void build(){
        setBackgroundColor(BG);
        LinearLayout header=new LinearLayout(context); header.setOrientation(LinearLayout.HORIZONTAL); header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8),0,dp(12),0); header.setBackground(panel(BG,GOLD,1,0));
        TextView back=text("‹",40,true,Gravity.CENTER,GOLD_LIGHT); back.setOnClickListener(v->{if(onBack!=null)onBack.run();});
        header.addView(back,new LinearLayout.LayoutParams(dp(54),LayoutParams.MATCH_PARENT));
        header.addView(text("ШЛЯХ СЛАВИ",20,true,Gravity.CENTER,GOLD_LIGHT),new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        header.addView(text("🏆 "+playerCups,12,true,Gravity.CENTER,GOLD_LIGHT),new LinearLayout.LayoutParams(dp(65),LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(68)); hp.gravity=Gravity.TOP; addView(header,hp);

        ScrollView scroll=new ScrollView(context); scroll.setFillViewport(true); scroll.setVerticalScrollBarEnabled(false); scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout content=new LinearLayout(context); content.setOrientation(LinearLayout.VERTICAL); content.setGravity(Gravity.CENTER_HORIZONTAL); content.setPadding(dp(12),dp(18),dp(12),dp(55));
        content.addView(text("ВІД НОВАЧКА ДО ЛЕГЕНДИ",13,true,Gravity.CENTER,GOLD),new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(28)));
        LinearLayout.LayoutParams intro=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(35)); intro.bottomMargin=dp(12);
        content.addView(text("Перемагай у рейтингових матчах та піднімайся вище",11,false,Gravity.CENTER,MUTED),intro);

        int currentIndex=LeagueSystem.indexForCups(playerCups); String currentName=LeagueSystem.nameForCups(playerCups);
        LinearLayout hero=new LinearLayout(context); hero.setOrientation(LinearLayout.VERTICAL); hero.setGravity(Gravity.CENTER); hero.setPadding(dp(16),dp(12),dp(16),dp(14));
        hero.setBackground(panel(CURRENT,GOLD_LIGHT,2,14)); hero.addView(text("ПОТОЧНА ЛІГА",10,true,Gravity.CENTER,GOLD));
        LinearLayout.LayoutParams hb=new LinearLayout.LayoutParams(dp(112),dp(112)); hb.topMargin=dp(2); hb.bottomMargin=dp(2); hero.addView(makeLeagueBadge(currentName,false,true),hb);
        hero.addView(text(currentName,22,true,Gravity.CENTER,GOLD_LIGHT),new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(36)));
        if(!LeagueSystem.isMaxLeague(playerCups)){
            int start=LeagueSystem.startForCups(playerCups), next=LeagueSystem.nextStartForCups(playerCups), total=Math.max(1,next-start);
            hero.addView(text("🏆 "+playerCups+" / "+next,14,true,Gravity.CENTER,TEXT));
            ProgressBar p=new ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal); p.setMax(total); p.setProgress(Math.max(0,Math.min(playerCups-start,total)));
            p.setProgressTintList(ColorStateList.valueOf(GOLD)); p.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(22,35,30)));
            LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(8)); pp.topMargin=dp(9); pp.bottomMargin=dp(7); hero.addView(p,pp);
            hero.addView(text("До "+LeagueSystem.nextNameForCups(playerCups)+" залишилось "+Math.max(0,next-playerCups)+" кубків",11,false,Gravity.CENTER,MUTED));
        }else hero.addView(text("🏆 "+playerCups+"  •  МАКСИМАЛЬНА ЛІГА",13,true,Gravity.CENTER,GOLD_LIGHT));
        LinearLayout.LayoutParams heroLp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT); heroLp.bottomMargin=dp(24); content.addView(hero,heroLp);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(35)); titleLp.bottomMargin=dp(4); content.addView(text("ЛІГИ DURAK ARENA",12,true,Gravity.CENTER,GOLD),titleLp);

        for(int i=0;i<LeagueSystem.NAMES.length;i++){
            boolean current=i==currentIndex, completed=i<currentIndex, locked=i>currentIndex; String name=LeagueSystem.NAMES[i];
            LinearLayout card=new LinearLayout(context); card.setOrientation(LinearLayout.HORIZONTAL); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(8),dp(8),dp(10),dp(8));
            card.setBackground(panel(current?CURRENT:PANEL,current?GOLD_LIGHT:GOLD,current?2:1,13));
            card.addView(makeLeagueBadge(name,locked,current),new LinearLayout.LayoutParams(dp(96),dp(96)));
            LinearLayout info=new LinearLayout(context); info.setOrientation(LinearLayout.VERTICAL); info.setGravity(Gravity.CENTER_VERTICAL); info.setPadding(dp(10),0,dp(4),0);
            info.addView(text(name,current?17:15,true,Gravity.LEFT,locked?MUTED:GOLD_LIGHT));
            info.addView(text(i==0?"ПОЧАТОК ШЛЯХУ":"🏆 "+LeagueSystem.STARTS[i]+" КУБКІВ",11,false,Gravity.LEFT,locked?LOCKED:TEXT));
            String status=current?"◆  ТИ ТУТ":completed?"✓  ПРОЙДЕНО":"🔒  ЩЕ "+Math.max(0,LeagueSystem.STARTS[i]-playerCups)+" КУБКІВ";
            TextView st=text(status,10,current,Gravity.LEFT,current?GOLD_LIGHT:completed?GOLD:LOCKED); LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(25)); slp.topMargin=dp(3); info.addView(st,slp);
            card.addView(info,new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
            card.addView(text(current?"ТИ\nТУТ":completed?"✓":"🔒",current?11:17,true,Gravity.CENTER,current?GOLD_LIGHT:completed?GOLD:LOCKED),new LinearLayout.LayoutParams(dp(45),LayoutParams.MATCH_PARENT));
            if(locked)card.setAlpha(.88f); content.addView(card,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(116)));
            if(i<LeagueSystem.NAMES.length-1){ LinearLayout connector=new LinearLayout(context); connector.setGravity(Gravity.CENTER); View line=new View(context); GradientDrawable l=new GradientDrawable(); l.setColor(i<currentIndex?GOLD:LINE_DARK); l.setCornerRadius(dp(2)); line.setBackground(l); connector.addView(line,new LinearLayout.LayoutParams(dp(3),dp(26))); content.addView(connector,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(26))); }
        }
        LinearLayout.LayoutParams end=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dp(80)); end.topMargin=dp(18); content.addView(text("♛  ВЕРШИНА ШЛЯХУ  ♛\nЛЕГЕНДА III",15,true,Gravity.CENTER,GOLD_LIGHT),end);
        scroll.addView(content); FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT); sp.topMargin=dp(68); addView(scroll,sp);
    }

    /** Maps every displayed league directly to one of the 15 dedicated PNG files. */
    private String assetForLeague(String leagueName){
        String n=leagueName==null?"":leagueName.trim().toUpperCase(); int tier=stage(n); String family;
        if(n.contains("ЛЕГЕН")) family="legend";
        else if(n.contains("ЕЛІТ")) family="elite";
        else if(n.contains("МАЙСТЕР")) family="master";
        else if(n.contains("ПРОФ")) family="pro";
        else family="novice";
        return "rank_"+family+"_"+tier;
    }
    private int stage(String n){ if(n.endsWith(" III"))return 3; if(n.endsWith(" II"))return 2; return 1; }

    /** PNG already contains its own complete frame/crest, so no photo-like overlay or crop is added here. */
    private FrameLayout makeLeagueBadge(String leagueName,boolean locked,boolean current){
        FrameLayout holder=new FrameLayout(context); holder.setClipChildren(false); holder.setClipToPadding(false);
        int id=context.getResources().getIdentifier(assetForLeague(leagueName),"drawable",context.getPackageName());
        if(id!=0){
            ImageView icon=new ImageView(context); icon.setScaleType(ImageView.ScaleType.FIT_CENTER); icon.setAdjustViewBounds(true); icon.setImageResource(id); icon.setAlpha(locked?.58f:1f);
            FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT); ip.gravity=Gravity.CENTER; holder.addView(icon,ip);
        }else{
            GradientDrawable fallback=new GradientDrawable(); fallback.setShape(GradientDrawable.OVAL); fallback.setColor(Color.rgb(10,20,17)); fallback.setStroke(dp(2),GOLD); holder.setBackground(fallback);
            holder.addView(text("♠",34,true,Gravity.CENTER,locked?LOCKED:GOLD_LIGHT),new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        }
        return holder;
    }

    private TextView text(String s,int size,boolean bold,int gravity,int color){ TextView v=new TextView(context); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setGravity(gravity); if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v; }
    private GradientDrawable panel(int fill,int stroke,int width,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(fill); d.setCornerRadius(dp(radius)); if(width>0)d.setStroke(dp(width),stroke); return d; }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
