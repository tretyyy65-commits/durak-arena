package com.nexora.durakarena;

import android.graphics.Color;

/** Single source of truth for league progression across Glory Path, Profile and future screens. */
public final class LeagueSystem {
    private LeagueSystem() {}

    public static final String[] NAMES = {
            "НОВАЧОК I","НОВАЧОК II","НОВАЧОК III",
            "ПРОФІ I","ПРОФІ II","ПРОФІ III",
            "МАЙСТЕР I","МАЙСТЕР II","МАЙСТЕР III",
            "ЕЛІТА I","ЕЛІТА II","ЕЛІТА III",
            "ЛЕГЕНДА I","ЛЕГЕНДА II","ЛЕГЕНДА III"
    };
    public static final int[] STARTS = {0,100,250,500,800,1200,1700,2200,2800,3500,4200,5000,6000,7000,8500};

    public static int indexForCups(int cups) {
        int c=Math.max(0,cups), idx=0;
        for(int i=0;i<STARTS.length;i++) if(c>=STARTS[i]) idx=i; else break;
        return idx;
    }
    public static String nameForCups(int cups){ return NAMES[indexForCups(cups)]; }
    public static int startForCups(int cups){ return STARTS[indexForCups(cups)]; }
    public static int nextStartForCups(int cups){ int i=indexForCups(cups); return i>=STARTS.length-1?STARTS[i]:STARTS[i+1]; }
    public static String nextNameForCups(int cups){ int i=indexForCups(cups); return i>=NAMES.length-1?NAMES[i]:NAMES[i+1]; }
    public static boolean isMaxLeague(int cups){ return indexForCups(cups)==NAMES.length-1; }

    /** Cups earned inside the current league tier. */
    public static int progressForCups(int cups) {
        int c = Math.max(0, cups);
        if (isMaxLeague(c)) return 0;
        return Math.max(0, c - startForCups(c));
    }

    /** Number of cups required to fill the current league progress bar. */
    public static int progressMaxForCups(int cups) {
        int c = Math.max(0, cups);
        if (isMaxLeague(c)) return 1;
        return Math.max(1, nextStartForCups(c) - startForCups(c));
    }

    public static int familyColorForIndex(int i){
        if(i<3) return Color.rgb(196,145,69);
        if(i<6) return Color.rgb(72,174,150);
        if(i<9) return Color.rgb(65,151,211);
        if(i<12) return Color.rgb(157,88,211);
        return Color.rgb(242,191,72);
    }
    public static int colorForCups(int cups){ return familyColorForIndex(indexForCups(cups)); }

    public static String assetForIndex(int i){
        int tier=(i%3)+1;
        String family=i<3?"novice":i<6?"pro":i<9?"master":i<12?"elite":"legend";
        return "rank_"+family+"_"+tier;
    }
}
