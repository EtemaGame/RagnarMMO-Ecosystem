package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.mobs.runtime.MobProfileBootstrap;
import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.RoBaseStats;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.element.CombatPropertyResolver;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.ArcherSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.FleeMobbingPenaltyService;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
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
        double critShield = Math.floor(luk / 5.0D);
        PhysicalAttackProfile physical = handProfile != null
                ? new PhysicalAttackProfile(handProfile.physicalAttack(), handProfile.physicalAttack(),
                handProfile.accuracy() * RoCombatStatusService.hitMultiplier(player),
                handProfile.critChance(), handProfile.critDamageMultiplier(), handProfile.aspdRo(), handProfile.weapon(),
                handProfile.statusAttack(), handProfile.weaponAttack(), handProfile.arrowAttack(),
                handProfile.weaponLevel(), handProfile.ranged(), true)
                : new PhysicalAttackProfile(Math.max(0.0D, derived.physicalAttackMin), Math.max(0.0D, derived.physicalAttackMax),
                derived.accuracy, derived.criticalChance, derived.criticalDamageMultiplier,
                (int) Math.round(derived.attackSpeed), ItemStack.EMPTY);
        return Optional.of(new CombatantProfile(
                player,
                CombatantKind.PLAYER,
                new CombatStats(str, agi, vit, intel, dex, luk, level),
                physical,
                new MagicAttackProfile(derived.magicAttackMin, derived.magicAttackMax),
                new DefenseProfile(FleeMobbingPenaltyService.applyMonsterMobbingPenalty(player, derived.flee),
                        derived.perfectDodge, critShield, vit, agi, intel, luk, level,
                        derived.hardDefense, derived.hardMagicDefense),
                new CombatModifiers("demihuman", CombatPropertyResolver.getDefensiveElement(player),
                        CombatPropertyResolver.getEntitySize(player),
                        CombatPropertyResolver.getDefensiveElementLevel(player)),
                false));
    }

    public static Optional<CombatantProfile> resolveMob(Mob mob, CombatStrictMode strictMode) {
        if (mob == null) {
            return Optional.empty();
        }
        var profileOpt = MobProfileBootstrap.ensureInitialized(mob);
        int level = profileOpt.map(profile -> profile.level())
                .orElseGet(() -> Math.max(1, (int) Math.round(mob.getMaxHealth() / 10.0D)));
        RoBaseStats baseStats = profileOpt.map(profile -> profile.baseStats()).orElseGet(RoBaseStats::novice);
        int vit = baseStats.vit();
        int agi = Math.max(1, baseStats.agi() - RoCombatStatusService.agiPenalty(mob));
        int intel = baseStats.intel();
        int dex = baseStats.dex();
        int luk = baseStats.luk();
        double hit = profileOpt.map(profile -> (double) profile.hit()).orElse(1.0D);
        double flee = profileOpt.map(profile -> (double) profile.flee()).orElseGet(() ->
                mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) * 100.0D);
        double hardDef = profileOpt.map(profile -> (double) profile.def()).orElse((double) mob.getArmorValue());
        double hardMdef = profileOpt.map(profile -> (double) profile.mdef()).orElse(0.0D);
        double minAtk = profileOpt.map(profile -> (double) profile.atkMin()).orElse(1.0D);
        double maxAtk = profileOpt.map(profile -> (double) profile.atkMax()).orElse(1.0D);
        double minMatk = profileOpt.map(profile -> (double) profile.matkMin()).orElse(1.0D);
        double maxMatk = profileOpt.map(profile -> (double) profile.matkMax()).orElse(1.0D);
        double critChance = profileOpt.map(profile -> profile.crit() / 100.0D).orElse(0.0D);
        int aspd = profileOpt.map(profile -> profile.aspd()).orElse(156);
        return Optional.of(new CombatantProfile(
                mob,
                CombatantKind.MOB,
                new CombatStats(baseStats.str(), agi, vit, intel, dex, luk, level),
                new PhysicalAttackProfile(minAtk, maxAtk, hit * RoCombatStatusService.hitMultiplier(mob),
                        critChance, 1.4D, aspd, ItemStack.EMPTY),
                new MagicAttackProfile(minMatk, maxMatk),
                new DefenseProfile(Math.max(0.0D, flee - RoCombatStatusService.agiPenalty(mob)) * RoCombatStatusService.fleeMultiplier(mob),
                        0.0D, Math.floor(luk / 5.0D), vit, agi, intel, luk, level, hardDef, hardMdef),
                new CombatModifiers(CombatPropertyResolver.getRace(mob), CombatPropertyResolver.getDefensiveElement(mob),
                        CombatPropertyResolver.getEntitySize(mob),
                        CombatPropertyResolver.getDefensiveElementLevel(mob)),
                false));
    }

    private static int total(ServerPlayer player, StatKeys key) {
        return Math.max(1, (int) Math.round(StatAttributes.getTotal(player, key))
                + AcolyteSkillFormulaService.statusStatModifier(player, key)
                + ArcherSkillFormulaService.statusStatModifier(player, key));
    }
}
