package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.combat.element.CombatPropertyResolver;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class AcolyteSkillFormulaService {
    private AcolyteSkillFormulaService() {
    }

    public static int divineProtectionReduction(ServerPlayer defender, LivingEntity attacker) {
        int level = skillLevel(defender, "divine_protection");
        if (level <= 0 || defender == null || attacker == null || attacker instanceof ServerPlayer
                || !isUndeadOrDemon(attacker)) {
            return 0;
        }
        return 3 * level + Math.floorDiv(baseLevel(defender) + 1, 25);
    }

    public static int demonBaneBonus(ServerPlayer attacker, LivingEntity target) {
        int level = skillLevel(attacker, "demon_bane");
        if (level <= 0 || attacker == null || target == null || target instanceof ServerPlayer
                || !isUndeadOrDemon(target)) {
            return 0;
        }
        return 3 * level + Math.floorDiv(baseLevel(attacker) + 1, 20);
    }

    public static int healAmount(ServerPlayer caster, int skillLevel) {
        int safeLevel = Math.max(1, skillLevel);
        int baseHeal = 4 + 8 * safeLevel;
        int multiplier = Math.max(1, Math.floorDiv(baseLevel(caster) + totalStat(caster, StatKeys.INT), 8));
        return multiplier * baseHeal;
    }

    public static int offensiveHealDamage(ServerPlayer caster, LivingEntity target, int skillLevel) {
        if (target == null || CombatPropertyResolver.getDefensiveElement(target) != ElementType.UNDEAD) {
            return 0;
        }
        double element = DamageFormulaService.elementMultiplier(
                ElementType.HOLY,
                CombatPropertyResolver.getDefensiveElement(target),
                CombatPropertyResolver.getDefensiveElementLevel(target));
        return (int) Math.floor(healAmount(caster, skillLevel) * element * 0.5D);
    }

    public static int agiBonus(int skillLevel) {
        return Math.max(0, 2 + skillLevel);
    }

    public static int blessingStatBonus(int skillLevel) {
        return Math.max(0, skillLevel);
    }

    public static double angelusSoftDefenseMultiplier(int skillLevel) {
        return 1.0D + Math.max(0, skillLevel) * 0.05D;
    }

    public static double signumHardDefenseMultiplier(int skillLevel) {
        double reduction = Math.max(0, 10 + 4 * skillLevel) / 100.0D;
        return Math.max(0.0D, 1.0D - reduction);
    }

    public static double decreaseAgiSuccessChance(ServerPlayer caster, LivingEntity target, int skillLevel) {
        int targetMdef = targetMdef(target);
        double chance = 40.0D + 2.0D * Math.max(1, skillLevel)
                + Math.floor((baseLevel(caster) + totalStat(caster, StatKeys.INT)) / 5.0D)
                - targetMdef;
        return FormulaUtil.clamp(0.0D, 0.95D, chance / 100.0D);
    }

    public static boolean isUndeadOrDemon(LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (CombatPropertyResolver.getDefensiveElement(target) == ElementType.UNDEAD) {
            return true;
        }
        String race = CombatPropertyResolver.getRace(target);
        return race != null && race.equalsIgnoreCase("demon");
    }

    public static int baseLevel(ServerPlayer player) {
        if (player == null) {
            return 1;
        }
        return RagnarCoreAPI.get(player).map(IPlayerStats::getLevel).map(level -> Math.max(1, level)).orElse(1);
    }

    public static int totalStat(ServerPlayer player, StatKeys key) {
        if (player == null || key == null) {
            return 1;
        }
        double total = StatAttributes.getTotal(player, key);
        int value = total > 0.0D ? (int) Math.round(total) : 1;
        return Math.max(1, value + statusStatModifier(player, key));
    }

    public static int statusStatModifier(LivingEntity entity, StatKeys key) {
        return com.etema.ragnarmmo.combat.status.RoCombatStatusService.statModifier(entity, key);
    }

    private static int skillLevel(ServerPlayer player, String path) {
        if (player == null) {
            return 0;
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("ragnarmmo", path);
        return PlayerJobSkillsProvider.get(player).map(skills -> skills.getSkillLevel(id)).orElse(0);
    }

    private static int targetMdef(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return (int) Math.round(StatAttributes.getTotal(player, StatKeys.INT));
        }
        return com.etema.ragnarmmo.common.api.mobs.runtime.MobProfileBootstrap.ensureInitialized(target)
                .map(profile -> Math.max(0, profile.mdef()))
                .orElse(0);
    }
}
