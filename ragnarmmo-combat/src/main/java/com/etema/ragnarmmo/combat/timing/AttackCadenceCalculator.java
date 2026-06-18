package com.etema.ragnarmmo.combat.timing;

import com.etema.ragnarmmo.combat.hand.AttackHandResolver;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.formula.AspdFormulaService;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;

public final class AttackCadenceCalculator {
    private AttackCadenceCalculator() {
    }

    public static double computeAPS(Player player, boolean offHand) {
        if (player == null) {
            return 1.0D;
        }

        boolean effectiveOffHand = offHand && AttackHandResolver.isValidAttackHand(player, true);
        int agi = stat(player, StatKeys.AGI);
        int dex = stat(player, StatKeys.DEX);
        int aspd = AspdFormulaService.aspdRo(baseWeaponAspd(effectiveOffHand ? player.getOffhandItem() : player.getMainHandItem()),
                hasShield(player, effectiveOffHand), agi, dex, effectiveOffHand ? -8.0D : 0.0D);
        double aps = AspdFormulaService.attacksPerSecond(aspd);
        return Double.isFinite(aps) && aps > 0.0D ? aps : 1.0D;
    }

    public static int computeIntervalTicks(Player player, boolean offHand) {
        double aps = computeAPS(player, offHand);
        return Math.max(2, (int) (20.0D / aps));
    }

    private static int stat(Player player, StatKeys key) {
        if (player == null) {
            return 1;
        }
        double value = StatAttributes.getTotal(player, key);
        return value > 0.0D ? (int) Math.round(value) : 1;
    }

    private static int baseWeaponAspd(ItemStack weapon) {
        return weapon != null && weapon.getItem() instanceof ProjectileWeaponItem
                ? 145
                : 156;
    }

    private static boolean hasShield(Player player, boolean offHandAttack) {
        if (player == null) {
            return false;
        }
        ItemStack oppositeHand = offHandAttack ? player.getMainHandItem() : player.getOffhandItem();
        return !oppositeHand.isEmpty() && oppositeHand.getItem() instanceof ShieldItem;
    }
}
