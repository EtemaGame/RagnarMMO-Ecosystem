package com.etema.ragnarmmo.combat.engine;

import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import com.etema.ragnarmmo.combat.contract.CombatModifiers;
import com.etema.ragnarmmo.combat.element.ElementType;
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
}
