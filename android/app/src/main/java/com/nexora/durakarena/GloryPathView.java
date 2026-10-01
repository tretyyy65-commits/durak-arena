package com.nexora.durakarena;

import android.content.Context;
import android.content.SharedPreferences;
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

/** Compact automatic Glory Path. All visible text is rendered by code, never baked into badges. */
public class GloryPathView extends FrameLayout {
    private final Context context;
    private final int playerCups;
    private final Runnable onBack;
    private final SharedPreferences prefs;
    private final int BG=Color.rgb(2,10,8), PANEL=Color.rgb(4,22,17), CURRENT=Color.rgb(6,31,23);
    private final int GOLD=Color.rgb(211,163,76), GOLD_LIGHT=Color.rgb(246,211,139), TEXT=Color.rgb(235,226,201), MUTED=Color.rgb(151,151,139);

    public GloryPathView(Context context,int playerCups,Runnable onBack){
        super(context); this.context=context; this.playerCups=Math.max(0,playerCups); this.onBack=onBack;
        prefs=context.getSharedPreferences("durak_arena_settings",Context.MODE_PRIVATE); build();
    }

    private String tr(String uk,String en,String de,String es){
        String l=prefs.getString("language","uk");
        if("en".equals(l))return en; if("de".equals(l))return de; if("es".equals(l))return es; return uk;
    }
    private String leagueName(int i){
        String[] uk={"НОВАЧОК","ПРОФІ","МАЙСТЕР","ЕЛІТА","ЛЕГЕНДА"};
        String[] en={"NOVICE","PRO","MASTER","ELITE","LEGEND"};
        String[] de={"ANFÄNGER","PROFI","MEISTER","ELITE","LEGENDE"};
        String[] es={"NOVATO","PRO","MAESTRO","ÉLITE","LEYENDA"};
        int f=i/3; String base=tr(uk[f],en[f],de[f],es[f]); return base+" "+new String[]{"I","II","III"}[i%3];
    }

    private void build(){
        setBackgroundColor(BG);
        int ci=LeagueSystem.indexForCups(playerCups), accent=LeagueSystem.familyColorForIndex(ci);

        LinearLayout header=new LinearLayout(context); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(8),0,dp(10),0); header.setBackground(panel(BG,GOLD,1,0));
        TextView back=text("‹",40,true,Gravity.CENTER,GOLD_LIGHT); back.setOnClickListener(v->{if(onBack!=null)onBack.run();}); header.addView(back,new LinearLayout.LayoutParams(dp(52),-1));
        header.addView(text(tr("ШЛЯХ СЛАВИ","GLORY PATH","RUHMESPFAD","CAMINO DE GLORIA"),20,true,Gravity.CENTER,GOLD_LIGHT),new LinearLayout.LayoutParams(0,-1,1));
        header.addView(text("🏆 "+playerCups,12,true,Gravity.CENTER,GOLD_LIGHT),new LinearLayout.LayoutParams(dp(70),-1));
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,dp(64)); addView(header,hp);

        ScrollView scroll=new ScrollView(context); scroll.setVerticalScrollBarEnabled(false); scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout content=new LinearLayout(context); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(12),dp(14),dp(12),dp(36));

        LinearLayout info=new LinearLayout(context); info.setOrientation(LinearLayout.HORIZONTAL); info.setGravity(Gravity.CENTER_VERTICAL); info.setPadding(dp(12),dp(8),dp(12),dp(8)); info.setBackground(panel(Color.rgb(4,18,14),GOLD,1,12));
        TextView bang=text("!",18,true,Gravity.CENTER,GOLD_LIGHT); bang.setBackground(circle(GOLD)); info.addView(bang,new LinearLayout.LayoutParams(dp(34),dp(34)));
        info.addView(text(tr("Підіймай лігу • отримуй призи • ставай №1","Climb leagues • earn rewards • become #1","Steige auf • verdiene Belohnungen • werde #1","Sube de liga • gana premios • sé #1"),11,false,Gravity.CENTER_VERTICAL,TEXT),new LinearLayout.LayoutParams(0,dp(42),1));
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(-1,-2); ilp.bottomMargin=dp(12); content.addView(info,ilp);

        LinearLayout hero=new LinearLayout(context); hero.setOrientation(LinearLayout.VERTICAL); hero.setGravity(Gravity.CENTER); hero.setPadding(dp(14),dp(10),dp(14),dp(12)); hero.setBackground(panel(CURRENT,accent,2,14));
        hero.addView(text(tr("ПОТОЧНА ЛІГА","CURRENT LEAGUE","AKTUELLE LIGA","LIGA ACTUAL"),10,true,Gravity.CENTER,accent));
        hero.addView(makeBadge(ci,false),new LinearLayout.LayoutParams(dp(92),dp(92)));
        hero.addView(text(leagueName(ci),20,true,Gravity.CENTER,accent),new LinearLayout.LayoutParams(-1,dp(31)));
        if(!LeagueSystem.isMaxLeague(playerCups)){
            int start=LeagueSystem.startForCups(playerCups), next=LeagueSystem.nextStartForCups(playerCups), span=Math.max(1,next-start), progress=Math.max(0,Math.min(span,playerCups-start));
            hero.addView(text("🏆 "+playerCups+" / "+next,13,true,Gravity.CENTER,TEXT));
            ProgressBar bar=new ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal); bar.setMax(span); bar.setProgress(progress); bar.setProgressTintList(ColorStateList.valueOf(accent)); bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(20,38,31)));
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(8)); bp.topMargin=dp(7); bp.bottomMargin=dp(5); hero.addView(bar,bp);
            hero.addView(text(tr("До ","To ","Bis ","Hasta ")+leagueName(ci+1)+": "+(next-playerCups)+" "+tr("кубків","cups","Pokale","copas"),10,false,Gravity.CENTER,MUTED));
        } else hero.addView(text(tr("МАКСИМАЛЬНА ЛІГА","MAX LEAGUE","HÖCHSTE LIGA","LIGA MÁXIMA"),11,true,Gravity.CENTER,accent));
        LinearLayout.LayoutParams hlp=new LinearLayout.LayoutParams(-1,-2); hlp.bottomMargin=dp(16); content.addView(hero,hlp);

        for(int i=0;i<LeagueSystem.NAMES.length;i++){
            boolean current=i==ci, done=i<ci, locked=i>ci; int color=LeagueSystem.familyColorForIndex(i);
            LinearLayout card=new LinearLayout(context); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(9),dp(7),dp(10),dp(7)); card.setBackground(panel(current?CURRENT:PANEL,current?color:Color.rgb(120,99,55),current?2:1,12));
            card.addView(makeBadge(i,locked),new LinearLayout.LayoutParams(dp(72),dp(72)));
            LinearLayout words=new LinearLayout(context); words.setOrientation(LinearLayout.VERTICAL); words.setGravity(Gravity.CENTER_VERTICAL); words.setPadding(dp(10),0,dp(4),0);
            TextView name=text(leagueName(i),15,true,Gravity.LEFT,color); name.setSingleLine(true); name.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM); words.addView(name,new LinearLayout.LayoutParams(-1,dp(28)));
            words.addView(text(i==0?tr("ПОЧАТОК ШЛЯХУ","PATH START","START DES PFADES","INICIO DEL CAMINO"):"🏆 "+LeagueSystem.STARTS[i]+" "+tr("КУБКІВ","CUPS","POKALE","COPAS"),10,false,Gravity.LEFT,locked?MUTED:TEXT));
            String status=current?tr("◆ ТИ ТУТ","◆ YOU ARE HERE","◆ DU BIST HIER","◆ ESTÁS AQUÍ"):done?tr("✓ ПРОЙДЕНО","✓ COMPLETED","✓ ERREICHT","✓ COMPLETADO"):"🔒 "+tr("ЩЕ","NEED","NOCH","FALTAN")+" "+Math.max(0,LeagueSystem.STARTS[i]-playerCups)+" "+tr("КУБКІВ","CUPS","POKALE","COPAS");
            words.addView(text(status,9,current,Gravity.LEFT,current?color:done?GOLD:MUTED)); card.addView(words,new LinearLayout.LayoutParams(0,-1,1));
            card.addView(text(current?"●":done?"✓":"🔒",16,true,Gravity.CENTER,current?color:done?GOLD:MUTED),new LinearLayout.LayoutParams(dp(38),-1)); if(locked)card.setAlpha(.76f);
            content.addView(card,new LinearLayout.LayoutParams(-1,dp(94)));
            if(i<LeagueSystem.NAMES.length-1){ View line=new View(context); line.setBackgroundColor(i<ci?LeagueSystem.familyColorForIndex(Math.min(i+1,ci)):Color.rgb(58,52,39)); LinearLayout connector=new LinearLayout(context); connector.setGravity(Gravity.CENTER); connector.addView(line,new LinearLayout.LayoutParams(dp(2),dp(17))); content.addView(connector,new LinearLayout.LayoutParams(-1,dp(17))); }
        }
        scroll.addView(content); FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,-1); sp.topMargin=dp(64); addView(scroll,sp);
    }

    private FrameLayout makeBadge(int index,boolean locked){
        FrameLayout h=new FrameLayout(context); int id=context.getResources().getIdentifier(LeagueSystem.assetForIndex(index),"drawable",context.getPackageName());
        if(id!=0){ ImageView v=new ImageView(context); v.setImageResource(id); v.setScaleType(ImageView.ScaleType.FIT_CENTER); v.setAlpha(locked?.48f:1f); h.addView(v,new FrameLayout.LayoutParams(-1,-1)); }
        else { h.setBackground(circle(LeagueSystem.familyColorForIndex(index))); h.addView(text("♠",28,true,Gravity.CENTER,LeagueSystem.familyColorForIndex(index)),new FrameLayout.LayoutParams(-1,-1)); }
        return h;
    }
    private TextView text(String s,int z,boolean b,int g,int c){ TextView v=new TextView(context); v.setText(s); v.setTextSize(z); v.setTextColor(c); v.setGravity(g); if(b)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setPadding(dp(4),0,dp(4),0); return v; }
    private GradientDrawable panel(int fill,int stroke,int width,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(fill); d.setCornerRadius(dp(radius)); if(width>0)d.setStroke(dp(width),stroke); return d; }
    private GradientDrawable circle(int stroke){ GradientDrawable d=new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(Color.rgb(5,18,14)); d.setStroke(dp(2),stroke); return d; }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
