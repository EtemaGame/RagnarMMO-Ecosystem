package com.etema.ragnarmmo.combat.contract;

import net.minecraft.world.item.ItemStack;

public record PhysicalAttackProfile(
        double minAttack,
        double maxAttack,
        double hit,
        double critChance,
        double critDamageMultiplier,
        int aspdRo,
        ItemStack weapon,
        double statusAttack,
        double weaponAttack,
        double arrowAttack,
        int weaponLevel,
        boolean ranged,
        boolean separatedComponents) {
    public PhysicalAttackProfile(
            double minAttack,
            double maxAttack,
            double hit,
            double critChance,
            double critDamageMultiplier,
            int aspdRo,
            ItemStack weapon) {
        this(minAttack, maxAttack, hit, critChance, critDamageMultiplier, aspdRo, weapon,
                0.0D, 0.0D, 0.0D, 1, false, false);
    }

    public PhysicalAttackProfile {
        weapon = weapon == null ? ItemStack.EMPTY : weapon.copy();
        statusAttack = Math.max(0.0D, statusAttack);
        weaponAttack = Math.max(0.0D, weaponAttack);
        arrowAttack = Math.max(0.0D, arrowAttack);
        weaponLevel = Math.max(1, Math.min(4, weaponLevel));
    }

    public double averageAttack() {
        if (separatedComponents) {
            return statusAttack + weaponAttack + (ranged ? arrowAttack : 0.0D);
        }
        return Math.max(0.0D, (minAttack + maxAttack) * 0.5D);
    }
}
