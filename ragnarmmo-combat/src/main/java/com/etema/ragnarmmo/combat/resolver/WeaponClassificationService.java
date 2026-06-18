package com.etema.ragnarmmo.combat.resolver;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public final class WeaponClassificationService {
    private WeaponClassificationService() {
    }

    public static boolean isRangedWeapon(ItemStack weapon) {
        return weapon != null && !weapon.isEmpty() && (weapon.getItem() instanceof ProjectileWeaponItem
                || weapon.getItem() instanceof BowItem
                || weapon.getItem() instanceof CrossbowItem);
    }

    public static int baseAspd(ItemStack weapon) {
        return isRangedWeapon(weapon) ? 145 : 156;
    }
}
