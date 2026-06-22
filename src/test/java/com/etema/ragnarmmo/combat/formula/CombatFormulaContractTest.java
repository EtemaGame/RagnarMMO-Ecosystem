package com.etema.ragnarmmo.combat.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class CombatFormulaContractTest {
    @Test
    void damageFormulaKeepsRoStyleStatAttackSplit() {
        double melee = DamageFormulaService.statusAtk(40, 20, 10, false);
        double ranged = DamageFormulaService.statusAtk(40, 20, 10, true);

        assertTrue(melee > ranged);
        assertEquals(0.0, DamageFormulaService.weaponAtk(-10));
    }

    @Test
    void defenseFormulaNeverDropsDamageBelowOne() {
        double reduced = DefenseFormulaService.applyPhysicalDefense(5, 999, 0.99);

        assertEquals(1.0, reduced);
    }

    @Test
    void criticalPhysicalDamageIgnoresHardAndSoftDefense() {
        double reduced = DefenseFormulaService.applyPhysicalDefense(100.0D, 999.0D, 99.0D, true);

        assertEquals(100.0D, reduced);
    }

    @Test
    void varianceFloorStaysInsideExpectedRange() {
        double floor = DamageFormulaService.damageVarianceFloor(100, 150, 0);
        double rolled = DamageFormulaService.damageVariance(100, 150, 0, new Random(1L));

        assertTrue(floor >= 80.0);
        assertTrue(rolled >= floor);
        assertTrue(rolled <= 100.0);
    }
}
