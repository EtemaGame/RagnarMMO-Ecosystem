package com.etema.ragnarmmo.combat.timing;

import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.formula.AspdFormulaService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;

public final class AttackCadenceCalculator {
    private static final int DEFAULT_MELEE_ASPD = 156;
    private static final int DEFAULT_RANGED_ASPD = 145;
    private static final double OFFHAND_ASPD_PENALTY = -8.0D;

    private AttackCadenceCalculator() {
    }

    public static int computeIntervalTicks(ServerPlayer player) {
        return computeIntervalTicks(player, player != null ? player.getMainHandItem() : ItemStack.EMPTY, false);
    }

    public static int computeIntervalTicks(ServerPlayer player, ItemStack weapon, boolean offHand) {
        int agi = stat(player, StatKeys.AGI);
        int dex = stat(player, StatKeys.DEX);
        int aspd = AspdFormulaService.aspdRo(baseWeaponAspd(weapon), hasShield(player, offHand), agi, dex,
                offHand ? OFFHAND_ASPD_PENALTY : 0.0D);
        double aps = AspdFormulaService.attacksPerSecond(aspd);
        return Math.max(1, (int) Math.round(20.0D / Math.max(0.25D, aps)));
    }

    private static int stat(ServerPlayer player, StatKeys key) {
        if (player == null) {
            return 1;
        }
        double value = StatAttributes.getTotal(player, key);
        return value > 0.0D ? (int) Math.round(value) : 1;
    }

    private static int baseWeaponAspd(ItemStack weapon) {
        return weapon != null && weapon.getItem() instanceof ProjectileWeaponItem
                ? DEFAULT_RANGED_ASPD
                : DEFAULT_MELEE_ASPD;
    }

    private static boolean hasShield(ServerPlayer player, boolean offHandAttack) {
        if (player == null) {
            return false;
        }
        ItemStack oppositeHand = offHandAttack ? player.getMainHandItem() : player.getOffhandItem();
        return !oppositeHand.isEmpty() && oppositeHand.getItem() instanceof ShieldItem;
    }
}
