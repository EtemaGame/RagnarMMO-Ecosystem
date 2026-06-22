package com.etema.ragnarmmo.player.stats.compute;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoPreRenewalFormulaServiceTest {
    @Test
    void hitAndFleeAreRoPointValuesNotProbabilities() {
        assertEquals(50.0D, RoPreRenewalFormulaService.hit(40, 10, 0), 0.0001D);
        assertEquals(40.0D, RoPreRenewalFormulaService.flee(30, 10, 0), 0.0001D);
        assertEquals(0.90D, RoPreRenewalFormulaService.hitRate(50, 40), 0.0001D);
    }

    @Test
    void critAndPerfectDodgeAreNormalizedProbabilities() {
        assertEquals(0.31D, RoPreRenewalFormulaService.criticalChance(100, 0), 0.0001D);
        assertEquals(0.11D, RoPreRenewalFormulaService.perfectDodge(100), 0.0001D);
    }

    @Test
    void aspdUsesRoScaleAndConvertsToAttacksPerSecond() {
        assertEquals(5.0D, RoPreRenewalFormulaService.aspdToAttacksPerSecond(190), 0.0001D);
        assertEquals(3.125D, RoPreRenewalFormulaService.aspdToAttacksPerSecond(184), 0.0001D);
    }

    @Test
    void preRenewalVariableCastUsesDexOver150Only() {
        assertEquals(2.0D, RoPreRenewalFormulaService.variableCastSeconds(2.0D, 0, 0), 0.0001D);
        assertEquals(1.0D, RoPreRenewalFormulaService.variableCastSeconds(2.0D, 75, 0), 0.0001D);
        assertEquals(0.0D, RoPreRenewalFormulaService.variableCastSeconds(2.0D, 150, 0), 0.0001D);
    }
}
