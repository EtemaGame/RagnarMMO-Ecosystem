package com.etema.ragnarmmo.combat.profile;

import net.minecraft.world.item.ItemStack;

public record HandAttackProfile(
        boolean offHand,
        double physicalAttack,
        double accuracy,
        double critChance,
        double critDamageMultiplier,
        int aspdRo,
        ItemStack weapon,
        double statusAttack,
        double weaponAttack,
        double arrowAttack,
        int weaponLevel,
        boolean ranged) {
    public HandAttackProfile(boolean offHand, double physicalAttack, double accuracy, double critChance,
            double critDamageMultiplier, int aspdRo, ItemStack weapon) {
        this(offHand, physicalAttack, accuracy, critChance, critDamageMultiplier, aspdRo, weapon,
                0.0D, 0.0D, 0.0D, 1, false);
    }

    public HandAttackProfile {
        weapon = weapon == null ? ItemStack.EMPTY : weapon.copy();
        statusAttack = Math.max(0.0D, statusAttack);
        weaponAttack = Math.max(0.0D, weaponAttack);
        arrowAttack = Math.max(0.0D, arrowAttack);
        weaponLevel = Math.max(1, Math.min(4, weaponLevel));
    }
}
