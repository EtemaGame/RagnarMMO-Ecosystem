package com.etema.ragnarmmo.combat.profile;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.contract.CombatStats;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.ArcherSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.DamageFormulaService;
import com.etema.ragnarmmo.combat.hand.AttackHandResolver;
import com.etema.ragnarmmo.items.runtime.RangedWeaponStatsHelper;
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
        int str = totalStat(player, StatKeys.STR);
        int dex = totalStat(player, StatKeys.DEX);
        int luk = totalStat(player, StatKeys.LUK);
        int agi = totalStat(player, StatKeys.AGI);
        int level = RagnarCoreAPI.get(player).map(stats -> Math.max(1, stats.getLevel())).orElse(1);
        int aspdRo = (int) Math.round(CombatMath.computeASPD_RO(CombatMath.getWeaponBaseASPD(weapon), !offHand && player.getOffhandItem().getItem() instanceof net.minecraft.world.item.ShieldItem, agi, dex, offHand ? -8.0D : 0.0D));
        double hit = CombatMath.computeHIT(dex, luk, level, 0.0D);
        double crit = CombatMath.computeCritChance(luk, dex, 0.0D);
        double critMult = CombatMath.computeCritDamageMultiplier(luk, str);
        double attack = rangedAttackOrFallback(player, weapon, str, dex, luk);
        return Optional.of(new HandAttackProfile(offHand, attack, hit, crit, critMult, aspdRo, weapon));
    }

    private static double rangedAttackOrFallback(ServerPlayer player, ItemStack weapon, int str, int dex, int luk) {
        return RangedWeaponStatsHelper.resolve(weapon)
                .map(stats -> DamageFormulaService.statusAtk(str, dex, luk, true) + stats.weaponAtk() + 25.0D)
                .orElseGet(() -> Math.max(1.0D,
                        player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)));
    }

    private static int totalStat(ServerPlayer player, StatKeys key) {
        return Math.max(1, (int) Math.round(StatAttributes.getTotal(player, key))
                + AcolyteSkillFormulaService.statusStatModifier(player, key)
                + ArcherSkillFormulaService.statusStatModifier(player, key));
    }
}
