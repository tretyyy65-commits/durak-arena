package com.nexora.durakarena;

import android.content.Context;
import android.content.SharedPreferences;

/** Local league rewards. Keep claim marker and balances in a single committed transaction.
 * Account-qualified keys prevent reward balances leaking between Google accounts.
 * A server-authoritative economy can replace this store when online matches are implemented.
 */
public final class GloryRewards {
    private GloryRewards() {}
    private static final int[] COINS={500,1000,2000,3000,4000,6000,10000,15000,18000,20000,22000,25000,30000,35000,40000};
    private static final int[] CRYSTALS={50,100,150,200,300,400,600,800,950,1100,1250,1500,1800,2100,2500};
    private static final int[] CARDS={1,1,1,2,2,3,3,4,4,4,5,5,6,7,8};
    public static int coins(int i){return COINS[i];}
    public static int crystals(int i){return CRYSTALS[i];}
    public static int cards(int i){return CARDS[i];}
    public static boolean eligible(int cups,int index){return index>=0&&index<LeagueSystem.STARTS.length&&cups>=LeagueSystem.STARTS[index];}
    private static SharedPreferences prefs(Context c){return c.getSharedPreferences("durak_arena_profile",Context.MODE_PRIVATE);}
    private static String prefix(SharedPreferences p){return "glory_v1_"+p.getString("firebase_uid","guest")+"_";}
    public static synchronized boolean isClaimed(Context c,int i){SharedPreferences p=prefs(c);return p.getBoolean(prefix(p)+"claimed_"+i,false);}
    public static synchronized long balance(Context c,String currency){SharedPreferences p=prefs(c);return Math.max(0,p.getLong(prefix(p)+currency,0));}
    public static synchronized boolean claim(Context c,int i){
        if(i<0||i>=LeagueSystem.STARTS.length)return false;
        SharedPreferences p=prefs(c); String key=prefix(p);
        if(!eligible(p.getInt("player_cups",0),i)||p.getBoolean(key+"claimed_"+i,false))return false;
        return p.edit().putBoolean(key+"claimed_"+i,true)
                .putLong(key+"coins",balance(c,"coins")+coins(i))
                .putLong(key+"crystals",balance(c,"crystals")+crystals(i))
                .putLong(key+"cards",balance(c,"cards")+cards(i)).commit();
    }
}
