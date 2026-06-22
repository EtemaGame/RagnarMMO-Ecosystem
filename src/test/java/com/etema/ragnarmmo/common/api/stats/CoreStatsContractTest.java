package com.etema.ragnarmmo.common.api.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class CoreStatsContractTest {
    @Test
    void stats6AddsAllPrimaryStats() {
        Stats6 total = new Stats6(1, 2, 3, 4, 5, 6)
                .add(new Stats6(6, 5, 4, 3, 2, 1));

        assertEquals(new Stats6(7, 7, 7, 7, 7, 7), total);
    }

    @Test
    void roBaseStatsClampToPositiveValues() {
        RoBaseStats stats = new RoBaseStats(0, -1, 2, 3, 4, 5);

        assertEquals(1, stats.str());
        assertEquals(1, stats.agi());
        assertEquals(2, stats.vit());
    }

    @Test
    void statKeysUseStableLowercaseIdsAndNeverRollLevelRandomly() {
        assertEquals("str", StatKeys.STR.id());
        assertEquals(StatKeys.DEX, StatKeys.fromId("dex").orElseThrow());

        Random rng = new Random(1234L);
        for (int i = 0; i < 100; i++) {
            assertNotEquals(StatKeys.LEVEL, StatKeys.random(rng));
        }
    }
}
