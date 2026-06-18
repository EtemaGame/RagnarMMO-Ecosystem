package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

import java.util.Random;

public final class CombatProcService {
    private static final ResourceLocation DOUBLE_ATTACK = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "double_attack");
    private CombatProcService() {
    }

    public static double applyBasicAttackDamageMultiplier(CombatantProfile attacker, double damage, Random rng) {
        if (!(attacker.entity() instanceof ServerPlayer player) || rng == null) {
            return damage;
        }
        if (!player.getMainHandItem().is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ragnarmmo", "daggers")))) {
            return damage;
        }
        int level = RagnarSkillsAPI.get(player).map(skills -> skills.getSkillLevel(DOUBLE_ATTACK)).orElse(0);
        if (level <= 0) {
            return damage;
        }
        double chance = Math.min(1.0D, level * 0.05D);
        if (rng.nextDouble() >= chance) {
            return damage;
        }
        return damage * 2.0D;
    }
}
