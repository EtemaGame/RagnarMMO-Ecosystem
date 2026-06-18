package com.etema.ragnarmmo.combat.profile;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.contract.CombatStats;
import com.etema.ragnarmmo.combat.hand.AttackHandResolver;
import com.etema.ragnarmmo.combat.timing.AttackCadenceCalculator;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class HandAttackProfileResolver {
    private HandAttackProfileResolver() {
    }

    public static Optional<HandAttackProfile> resolve(ServerPlayer player, boolean offHand) {
        if (player == null) {
            return Optional.empty();
        }
        ItemStack weapon = offHand ? player.getOffhandItem() : player.getMainHandItem();
        int str = (int) Math.round(StatAttributes.getTotal(player, StatKeys.STR));
        int dex = (int) Math.round(StatAttributes.getTotal(player, StatKeys.DEX));
        int luk = (int) Math.round(StatAttributes.getTotal(player, StatKeys.LUK));
        int agi = (int) Math.round(StatAttributes.getTotal(player, StatKeys.AGI));
        int aspdRo = (int) Math.round(CombatMath.computeASPD_RO(CombatMath.getWeaponBaseASPD(weapon), !offHand && player.getOffhandItem().getItem() instanceof net.minecraft.world.item.ShieldItem, agi, dex, offHand ? -8.0D : 0.0D));
        double hit = CombatMath.computeHIT(dex, luk, player.experienceLevel, 0.0D);
        double crit = CombatMath.computeCritChance(luk, dex, 0.0D);
        double critMult = CombatMath.computeCritDamageMultiplier(luk, str);
        double attack = Math.max(1.0D, weapon.isEmpty() ? player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) : player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));
        return Optional.of(new HandAttackProfile(offHand, attack, hit, crit, critMult, aspdRo, weapon));
    }
}
