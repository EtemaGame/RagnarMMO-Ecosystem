package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.element.CombatPropertyResolver;
import com.etema.ragnarmmo.combat.profile.HandAttackProfile;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import com.etema.ragnarmmo.player.stats.compute.StatResolutionService;
import com.etema.ragnarmmo.player.stats.compute.CoreDerivedStatsCalculator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class CombatantProfileResolver {
    private CombatantProfileResolver() {
    }

    public static Optional<CombatantProfile> resolvePlayer(ServerPlayer player, HandAttackProfile handProfile) {
        var statsOpt = RagnarCoreAPI.get(player);
        if (statsOpt.isEmpty()) {
            return Optional.empty();
        }
        IPlayerStats stats = statsOpt.get();
        DerivedStats derived = DerivedStatsService.compute(player, stats).orElseGet(() -> CoreDerivedStatsCalculator.compute(player, stats));
        if (derived == null) {
            return Optional.empty();
        }
        int str = total(player, StatKeys.STR);
        int agi = total(player, StatKeys.AGI);
        int vit = total(player, StatKeys.VIT);
        int intel = total(player, StatKeys.INT);
        int dex = total(player, StatKeys.DEX);
        int luk = total(player, StatKeys.LUK);
        int level = Math.max(1, stats.getLevel());
        double critShield = Math.floor(level / 15.0D) + Math.floor(luk / 5.0D);
        PhysicalAttackProfile physical = handProfile != null
                ? new PhysicalAttackProfile(handProfile.physicalAttack(), handProfile.physicalAttack(), handProfile.accuracy(),
                handProfile.critChance(), handProfile.critDamageMultiplier(), handProfile.aspdRo(), handProfile.weapon())
                : new PhysicalAttackProfile(Math.max(0.0D, derived.physicalAttackMin), Math.max(0.0D, derived.physicalAttackMax),
                derived.accuracy, derived.criticalChance, derived.criticalDamageMultiplier,
                (int) Math.round(derived.attackSpeed), ItemStack.EMPTY);
        return Optional.of(new CombatantProfile(
                player,
                CombatantKind.PLAYER,
                new CombatStats(str, agi, vit, intel, dex, luk, level),
                physical,
                new MagicAttackProfile(derived.magicAttackMin, derived.magicAttackMax),
                new DefenseProfile(derived.flee, derived.perfectDodge, critShield, vit, agi, intel, luk, level,
                        derived.hardDefense, derived.hardMagicDefense),
                new CombatModifiers("demihuman", CombatPropertyResolver.getDefensiveElement(player),
                        CombatPropertyResolver.getEntitySize(player)),
                false));
    }

    public static Optional<CombatantProfile> resolveMob(Mob mob, CombatStrictMode strictMode) {
        if (mob == null) {
            return Optional.empty();
        }
        int level = Math.max(1, (int) Math.round(mob.getMaxHealth() / 10.0D));
        int vit = 1;
        int agi = 1;
        int intel = 1;
        int dex = 1;
        int luk = 1;
        double flee = Math.max(0.0D, mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) * 100.0D);
        return Optional.of(new CombatantProfile(
                mob,
                CombatantKind.MOB,
                new CombatStats(1, agi, vit, intel, dex, luk, level),
                new PhysicalAttackProfile(1.0D, 1.0D, 1.0D, 0.0D, 1.4D, 156, ItemStack.EMPTY),
                new MagicAttackProfile(1.0D, 1.0D),
                new DefenseProfile(flee, 0.0D, 0.0D, vit, agi, intel, luk, level, mob.getArmorValue(), 0.0D),
                new CombatModifiers("unknown", CombatPropertyResolver.getDefensiveElement(mob),
                        CombatPropertyResolver.getEntitySize(mob)),
                false));
    }

    private static int total(ServerPlayer player, StatKeys key) {
        return (int) Math.round(StatAttributes.getTotal(player, key));
    }
}
