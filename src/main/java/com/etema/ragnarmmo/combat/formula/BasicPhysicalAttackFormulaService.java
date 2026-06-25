package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.combat.contract.CombatStats;
import com.etema.ragnarmmo.combat.contract.PhysicalAttackProfile;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;

import java.util.Random;

public final class BasicPhysicalAttackFormulaService {
    private BasicPhysicalAttackFormulaService() {
    }

    public static double damageBeforeDefense(PhysicalAttackProfile attack, CombatStats stats,
            CombatMath.MobSize targetSize, boolean critical, double physicalAttackMultiplier, Random rng) {
        if (attack == null || stats == null) {
            return 0.0D;
        }
        double multiplier = Math.max(0.0D, physicalAttackMultiplier);
        double damage = attack.separatedComponents()
                ? separatedComponentDamage(attack, stats.dex(), targetSize, critical, rng)
                : DamageFormulaService.damageVariance(attack.averageAttack(), stats.dex(), stats.luk(), rng);
        damage *= multiplier;
        if (critical) {
            damage *= Math.max(1.0D, attack.critDamageMultiplier());
        }
        return Math.max(0.0D, damage);
    }

    private static double separatedComponentDamage(PhysicalAttackProfile attack, int dex, CombatMath.MobSize targetSize,
            boolean critical, Random rng) {
        double weaponRoll = attack.ranged()
                ? DamageFormulaService.bowWeaponAtkRoll(attack.weaponAttack(), attack.arrowAttack(),
                Math.max(1, dex), attack.weaponLevel(), critical, rng)
                : DamageFormulaService.meleeWeaponAtkRoll(attack.weaponAttack(),
                Math.max(1, dex), attack.weaponLevel(), critical, rng);
        double sizePenalty = CombatMath.getWeaponSizePenalty(attack.weapon(),
                targetSize == null ? CombatMath.MobSize.MEDIUM : targetSize);
        return combineStatusAndWeaponAttack(attack.statusAttack(), weaponRoll, sizePenalty);
    }

    public static double combineStatusAndWeaponAttack(double statusAttack, double weaponAttack, double sizePenalty) {
        return Math.max(0.0D, statusAttack) + Math.max(0.0D, weaponAttack) * Math.max(0.0D, sizePenalty);
    }
}
