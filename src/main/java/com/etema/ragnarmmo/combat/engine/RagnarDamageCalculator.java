package com.etema.ragnarmmo.combat.engine;

import com.etema.ragnarmmo.combat.contract.CombatModifiers;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.combat.formula.DamageFormulaService;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import net.minecraft.world.item.ItemStack;

public class RagnarDamageCalculator {
    public double computePhysicalDamage(double baseAttack, int dex, int luk, java.util.Random rng) {
        return CombatMath.computeDamageVariance(baseAttack, dex, luk, rng);
    }

    public double applyModifiers(double damage, ItemStack weapon, CombatModifiers defenderModifiers,
            ElementType attackerElement, boolean magical) {
        double modified = Math.max(0.0D, damage);
        modified *= DamageFormulaService.elementMultiplier(
                attackerElement,
                defenderModifiers.element());
        return Math.max(0.0D, modified);
    }

    public double applyElement(double damage, CombatModifiers defenderModifiers, ElementType attackerElement) {
        return Math.max(0.0D, damage) * DamageFormulaService.elementMultiplier(
                attackerElement,
                defenderModifiers.element());
    }

    public double applyPhysicalDefense(double damage, int vit, int agi, int level, double hardDef) {
        double softDef = CombatMath.computeSoftDEF(vit, agi, level);
        double dr = CombatMath.computePhysDR(hardDef);
        return CombatMath.applyPhysicalDefense(damage, softDef, hardDef, dr);
    }

    public double applyMagicDefense(double damage, int intel, int vit, int agi, int level, double hardMdef) {
        double soft = CombatMath.computeSoftMDEF(intel, vit);
        return CombatMath.applyMagicDefense(damage, soft, hardMdef);
    }
}
