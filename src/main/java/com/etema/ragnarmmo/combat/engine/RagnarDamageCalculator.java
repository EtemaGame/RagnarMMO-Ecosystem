package com.etema.ragnarmmo.combat.engine;

import com.etema.ragnarmmo.combat.contract.CombatModifiers;
import com.etema.ragnarmmo.combat.contract.CombatantKind;
import com.etema.ragnarmmo.combat.contract.CombatantProfile;
import com.etema.ragnarmmo.combat.contract.DefenseProfile;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.combat.formula.DamageFormulaService;
import com.etema.ragnarmmo.combat.formula.DefenseFormulaService;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
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
                defenderModifiers.element(),
                defenderModifiers.elementLevel());
        return Math.max(0.0D, modified);
    }

    public double applyElement(double damage, CombatModifiers defenderModifiers, ElementType attackerElement) {
        return Math.max(0.0D, damage) * DamageFormulaService.elementMultiplier(
                attackerElement,
                defenderModifiers.element(),
                defenderModifiers.elementLevel());
    }

    public double applyPhysicalDefense(double damage, int vit, int agi, int level, double hardDef) {
        return applyPhysicalDefense(damage, vit, hardDef, false, null, false);
    }

    public double applyPhysicalDefense(double damage, CombatantProfile defender, boolean critical, java.util.Random rng) {
        if (defender == null) {
            return Math.max(1.0D, damage);
        }
        DefenseProfile defense = defender.defense();
        boolean mob = defender.kind() == CombatantKind.MOB;
        double multiplier = mob ? RoCombatStatusService.physicalDefenseMultiplier(defender.entity()) : 1.0D;
        double hardDef = defense.hardDef();
        return applyPhysicalDefense(damage, defense.vit(), hardDef, critical, rng, mob, multiplier);
    }

    public double applyPhysicalDefense(double damage, int vit, double hardDef, boolean critical, java.util.Random rng,
            boolean mobFormula) {
        return applyPhysicalDefense(damage, vit, hardDef, critical, rng, mobFormula, 1.0D);
    }

    public double applyPhysicalDefense(double damage, int vit, double hardDef, boolean critical, java.util.Random rng,
            boolean mobFormula, double defenseMultiplier) {
        double softDef = mobFormula
                ? DefenseFormulaService.mobSoftDefRoll(vit, rng)
                : DefenseFormulaService.playerSoftDefRoll(vit, rng);
        double multiplier = Math.max(0.0D, defenseMultiplier);
        return DefenseFormulaService.applyPhysicalDefense(damage, softDef * multiplier, hardDef * multiplier, critical);
    }

    public double applyMagicDefense(double damage, int intel, int vit, int agi, int level, double hardMdef) {
        return DefenseFormulaService.applyMagicDefense(damage, DefenseFormulaService.softMdef(intel, vit), hardMdef);
    }

    public double applyMagicDefense(double damage, CombatantProfile defender) {
        if (defender == null) {
            return Math.max(1.0D, damage);
        }
        DefenseProfile defense = defender.defense();
        return DefenseFormulaService.applyMagicDefense(damage,
                DefenseFormulaService.softMdef(defense.intel(), defense.vit()),
                defense.hardMdef());
    }
}
