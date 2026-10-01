package com.nexora.durakarena;

import org.junit.Test;
import static org.junit.Assert.*;

public class GloryProgressionTest {
    @Test public void thresholdsUnlockExactlyAtTheirCupValue() {
        for(int i=0;i<LeagueSystem.STARTS.length;i++) {
            int threshold=LeagueSystem.STARTS[i];
            assertEquals(i,LeagueSystem.indexForCups(threshold));
            assertTrue(GloryRewards.eligible(threshold,i));
            if(i>0) {
                assertEquals(i-1,LeagueSystem.indexForCups(threshold-1));
                assertFalse(GloryRewards.eligible(threshold-1,i));
            }
            assertTrue(GloryRewards.coins(i)>0);
            assertTrue(GloryRewards.crystals(i)>0);
            assertTrue(GloryRewards.cards(i)>0);
        }
    }
    @Test public void progressResetsAtPromotionAndFillsAtFinalLeague() {
        assertEquals(0,LeagueSystem.indexForCups(-10));
        for(int i=0;i<LeagueSystem.STARTS.length-1;i++) {
            int start=LeagueSystem.STARTS[i], next=LeagueSystem.STARTS[i+1];
            assertEquals(0,LeagueSystem.progressForCups(start));
            assertEquals(next-start,LeagueSystem.progressMaxForCups(start));
            assertEquals(next-start-1,LeagueSystem.progressForCups(next-1));
        }
        assertTrue(LeagueSystem.isMaxLeague(8500));
        assertTrue(LeagueSystem.isMaxLeague(Integer.MAX_VALUE));
        assertEquals(LeagueSystem.progressMaxForCups(9000),LeagueSystem.progressForCups(9000));
    }
    @Test public void invalidRewardsRemainUnavailable() {
        assertFalse(GloryRewards.eligible(99999,-1));
        assertFalse(GloryRewards.eligible(99999,15));
        assertFalse(GloryRewards.eligible(-1,0));
    }
}
