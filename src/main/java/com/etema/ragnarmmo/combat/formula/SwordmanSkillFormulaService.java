package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public final class SwordmanSkillFormulaService {
    private static final ResourceLocation SWORD_MASTERY =
            ResourceLocation.fromNamespaceAndPath("ragnarmmo", "sword_mastery");
    private static final ResourceLocation TWO_HAND_MASTERY =
            ResourceLocation.fromNamespaceAndPath("ragnarmmo", "two_hand_mastery");

    private SwordmanSkillFormulaService() {
    }

    public static double weaponMasteryBonus(ServerPlayer player, ItemStack weapon) {
        if (player == null || weapon == null || weapon.isEmpty()) {
            return 0.0D;
        }
        return PlayerJobSkillsProvider.get(player)
                .map(skills -> {
                    if (isTwoHandedSword(weapon)) {
                        return 4.0D * skills.getSkillLevel(TWO_HAND_MASTERY);
                    }
                    if (isOneHandedSword(weapon) || isDagger(weapon)) {
                        return 4.0D * skills.getSkillLevel(SWORD_MASTERY);
                    }
                    return 0.0D;
                })
                .orElse(0.0D);
    }

    public static double increaseHpRecoveryPerSecond(int skillLevel, double maxHp) {
        int level = Math.max(0, skillLevel);
        if (level <= 0) {
            return 0.0D;
        }
        return (5.0D * level + Math.max(0.0D, maxHp) * 0.002D * level) / 10.0D;
    }

    public static double healingItemMultiplier(int skillLevel, int vit) {
        return 1.0D + Math.max(0, vit) * 0.02D + Math.max(0, skillLevel) * 0.10D;
    }

    private static boolean isDagger(ItemStack weapon) {
        return weapon.getTags().anyMatch(tag -> tag.location().getPath().contains("daggers"));
    }

    private static boolean isTwoHandedSword(ItemStack weapon) {
        return weapon.getTags().anyMatch(tag -> tag.location().getPath().contains("two_handed")
                || tag.location().getPath().contains("two_hand"));
    }

    private static boolean isOneHandedSword(ItemStack weapon) {
        return weapon.getItem() instanceof SwordItem && !isTwoHandedSword(weapon);
    }
}
