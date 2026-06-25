package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.combat.element.ElementType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobType;

public final class PassiveCombatModifierService {
    private static final ResourceLocation DEMON_BANE = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "demon_bane");
    private static final ResourceLocation DIVINE_PROTECTION = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "divine_protection");

    private PassiveCombatModifierService() {
    }

    public static double applyOutgoingPhysicalDamage(CombatantProfile attacker, CombatantProfile defender, double damage) {
        if (!(attacker.entity() instanceof ServerPlayer player)) {
            return damage;
        }
        if (isDemonOrUndead(defender)) {
            int level = skillLevel(player, DEMON_BANE);
            damage += level * 3.0D;
        }
        return damage;
    }

    public static double applyIncomingPhysicalDamage(CombatantProfile attacker, CombatantProfile defender, double damage) {
        if (!(defender.entity() instanceof ServerPlayer player) || !isDemonOrUndead(attacker)) {
            return damage;
        }
        int level = skillLevel(player, DIVINE_PROTECTION);
        return Math.max(0.0D, damage - (level * 3.0D));
    }

    private static boolean isDemonOrUndead(CombatantProfile profile) {
        if (profile == null || profile.entity() == null) {
            return false;
        }
        String race = profile.modifiers().race();
        ElementType element = profile.modifiers().element();
        return "demon".equalsIgnoreCase(race)
                || "undead".equalsIgnoreCase(race)
                || element == ElementType.UNDEAD
                || profile.entity().getMobType() == MobType.UNDEAD;
    }

    private static int skillLevel(ServerPlayer player, ResourceLocation skillId) {
        return RagnarSkillsAPI.get(player).map(skills -> skills.getSkillLevel(skillId)).orElse(0);
    }
}
