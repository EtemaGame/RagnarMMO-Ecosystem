package com.etema.ragnarmmo.combat.profile;

import net.minecraft.world.item.ItemStack;

public record HandAttackProfile(
        boolean offHand,
        double physicalAttack,
        double accuracy,
        double critChance,
        double critDamageMultiplier,
        int aspdRo,
        ItemStack weapon) {
    public HandAttackProfile {
        weapon = weapon == null ? ItemStack.EMPTY : weapon.copy();
    }
}
