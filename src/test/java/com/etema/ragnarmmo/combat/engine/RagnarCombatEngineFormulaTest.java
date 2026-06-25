package com.etema.ragnarmmo.combat.engine;

import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import com.etema.ragnarmmo.combat.contract.CombatModifiers;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.combat.formula.BasicPhysicalAttackFormulaService;
import com.etema.ragnarmmo.combat.formula.DefenseFormulaService;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RagnarCombatEngineFormulaTest {
    @Test
    void criticalCanLandEvenWhenNormalHitWouldMiss() {
        RagnarHitCalculator calculator = new RagnarHitCalculator();

        CombatHitResultType result = calculator.rollHitWithCrit(
                1.0D,
                999.0D,
                1.0D,
                0.0D,
                RandomSource.create(1L));

        assertEquals(CombatHitResultType.CRIT, result);
    }

    @Test
    void criticalShieldReducesEffectiveCriticalChance() {
        RagnarHitCalculator calculator = new RagnarHitCalculator();

        CombatHitResultType result = calculator.rollHitWithCrit(
                999.0D,
                1.0D,
                1.0D,
                100.0D,
                RandomSource.create(1L));

        assertEquals(CombatHitResultType.HIT, result);
    }

    @Test
    void commonDamageCalculatorAppliesElement() {
        RagnarDamageCalculator calculator = new RagnarDamageCalculator();
        CombatModifiers largeWaterTarget = new CombatModifiers("unknown", ElementType.WATER, CombatMath.MobSize.LARGE);

        double damage = calculator.applyModifiers(
                100.0D,
                null,
                largeWaterTarget,
                ElementType.FIRE,
                false);

        assertEquals(50.0D, damage, 0.0001D);
    }

    @Test
    void magicalDamageAlsoUsesElement() {
        RagnarDamageCalculator calculator = new RagnarDamageCalculator();
        CombatModifiers largeWaterTarget = new CombatModifiers("unknown", ElementType.WATER, CombatMath.MobSize.LARGE);

        double damage = calculator.applyModifiers(
                100.0D,
                null,
                largeWaterTarget,
                ElementType.FIRE,
                true);

        assertEquals(50.0D, damage, 0.0001D);
    }

    @Test
    void commonDamageCalculatorUsesDefensiveElementLevel() {
        RagnarDamageCalculator calculator = new RagnarDamageCalculator();
        CombatModifiers waterFourTarget = new CombatModifiers("unknown", ElementType.WATER, CombatMath.MobSize.LARGE, 4);

        double damage = calculator.applyModifiers(
                100.0D,
                null,
                waterFourTarget,
                ElementType.FIRE,
                false);

        assertEquals(0.0D, damage, 0.0001D);
    }

    @Test
    void commonPhysicalDefenseUsesPreRenewalPlayerSoftDefRoll() {
        RagnarDamageCalculator calculator = new RagnarDamageCalculator();

        double damage = calculator.applyPhysicalDefense(100.0D, 30, 10.0D, false, new java.util.Random(7L), false);
        double expectedSoft = DefenseFormulaService.playerSoftDefRoll(30, new java.util.Random(7L));
        double expected = DefenseFormulaService.applyPhysicalDefense(100.0D, expectedSoft, 10.0D, false);

        assertEquals(expected, damage, 0.0001D);
    }

    @Test
    void commonPhysicalDefenseCanUseMobSoftDefRoll() {
        RagnarDamageCalculator calculator = new RagnarDamageCalculator();

        double damage = calculator.applyPhysicalDefense(100.0D, 40, 10.0D, false, new java.util.Random(3L), true);
        double expectedSoft = DefenseFormulaService.mobSoftDefRoll(40, new java.util.Random(3L));
        double expected = DefenseFormulaService.applyPhysicalDefense(100.0D, expectedSoft, 10.0D, false);

        assertEquals(expected, damage, 0.0001D);
    }

    @Test
    void commonPhysicalDefenseCriticalIgnoresDefense() {
        RagnarDamageCalculator calculator = new RagnarDamageCalculator();

        double damage = calculator.applyPhysicalDefense(100.0D, 999, 99.0D, true, new java.util.Random(1L), false);

        assertEquals(100.0D, damage, 0.0001D);
    }

    @Test
    void separatedBasicAttackSnapshotKeepsStatusAtkOutsideSizePenalty() {
        double damage = BasicPhysicalAttackFormulaService.combineStatusAndWeaponAttack(
                40.0D,
                60.0D,
                0.75D);

        assertEquals(85.0D, damage, 0.0001D);
    }
}
