package com.etema.ragnarmmo.combat.status;

import net.minecraft.nbt.CompoundTag;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class RoCombatStatusService {
    public static final String PROVOKE_UNTIL_TAG = "ragnar_provoke_until";
    public static final String PROVOKE_DEF_REDUCTION_TAG = "ragnar_provoke_def_reduction_percent";
    public static final String PROVOKE_ATK_BONUS_TAG = "ragnar_provoke_atk_bonus_percent";
    public static final String DECREASE_AGI_UNTIL_TAG = "ragnar_decrease_agi_until";
    public static final String DECREASE_AGI_AMOUNT_TAG = "ragnar_decrease_agi_amount";
    public static final String OVER_THRUST_UNTIL_TAG = "ragnar_over_thrust_until";
    public static final String OVER_THRUST_LEVEL_TAG = "ragnar_over_thrust_level";
    public static final String MAGNUM_BREAK_UNTIL_TAG = "ragnar_magnum_break_until";
    public static final String MAGNUM_BREAK_FIRE_BONUS_TAG = "ragnar_magnum_break_fire_bonus_ratio";
    public static final String ENDURE_UNTIL_TAG = "ragnar_endure_until";
    public static final String ENDURE_MDEF_BONUS_TAG = "ragnar_endure_mdef_bonus";
    public static final String ENDURE_REMAINING_MONSTER_HITS_TAG = "ragnar_endure_remaining_monster_hits";
    public static final String INCREASE_AGI_UNTIL_TAG = "ragnar_increase_agi_until";
    public static final String INCREASE_AGI_AMOUNT_TAG = "ragnar_increase_agi_amount";
    public static final String BLESSING_UNTIL_TAG = "ragnar_blessing_until";
    public static final String BLESSING_AMOUNT_TAG = "ragnar_blessing_amount";
    public static final String OFFENSIVE_BLESSING_UNTIL_TAG = "ragnar_offensive_blessing_until";
    public static final String OFFENSIVE_BLESSING_STAT_MULTIPLIER_TAG = "ragnar_offensive_blessing_stat_multiplier";
    public static final String ANGELUS_UNTIL_TAG = "ragnar_angelus_until";
    public static final String ANGELUS_SOFT_DEF_MULTIPLIER_TAG = "ragnar_angelus_soft_def_multiplier";
    public static final String SIGNUM_UNTIL_TAG = "ragnar_signum_until";
    public static final String SIGNUM_HARD_DEF_MULTIPLIER_TAG = "ragnar_signum_hard_def_multiplier";
    public static final String IMPROVE_CONCENTRATION_UNTIL_TAG = "ragnar_improve_concentration_until";
    public static final String IMPROVE_CONCENTRATION_LEVEL_TAG = "ragnar_improve_concentration_level";
    public static final String POISON_UNTIL_TAG = "ragnar_poison_until";
    public static final String POISON_NEXT_TICK_TAG = "ragnar_poison_next_tick";
    public static final String FROZEN_UNTIL_TAG = "ragnar_frozen_until";
    public static final String STONE_CURSE_UNTIL_TAG = "ragnar_stone_curse_until";
    public static final String SILENCE_UNTIL_TAG = "ragnar_silence_until";
    public static final String BLIND_UNTIL_TAG = "ragnar_blind_until";
    public static final String CHAOS_UNTIL_TAG = "ragnar_chaos_until";
    public static final String SIGHT_UNTIL_TAG = "ragnar_sight_until";
    public static final String HIDING_UNTIL_TAG = "ragnar_hiding_until";

    private RoCombatStatusService() {
    }

    public static void applyProvoke(Mob mob, int durationTicks, double defReductionPercent, double atkBonusPercent) {
        if (mob == null) {
            return;
        }
        long until = mob.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = mob.getPersistentData();
        data.putLong(PROVOKE_UNTIL_TAG, until);
        data.putDouble(PROVOKE_DEF_REDUCTION_TAG, Math.max(0.0D, defReductionPercent));
        data.putDouble(PROVOKE_ATK_BONUS_TAG, Math.max(0.0D, atkBonusPercent));
    }

    public static void clearProvoke(Mob mob) {
        if (mob == null) {
            return;
        }
        CompoundTag data = mob.getPersistentData();
        data.remove(PROVOKE_UNTIL_TAG);
        data.remove(PROVOKE_DEF_REDUCTION_TAG);
        data.remove(PROVOKE_ATK_BONUS_TAG);
    }

    public static void applyDecreaseAgi(LivingEntity target, int durationTicks, int agiReduction) {
        if (target == null) {
            return;
        }
        clearIncreaseAgi(target);
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(DECREASE_AGI_UNTIL_TAG, until);
        data.putInt(DECREASE_AGI_AMOUNT_TAG, Math.max(0, agiReduction));
    }

    public static void applyIncreaseAgi(LivingEntity target, int durationTicks, int agiBonus) {
        if (target == null) {
            return;
        }
        clearDecreaseAgi(target);
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(INCREASE_AGI_UNTIL_TAG, until);
        data.putInt(INCREASE_AGI_AMOUNT_TAG, Math.max(0, agiBonus));
    }

    public static void clearIncreaseAgi(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(INCREASE_AGI_UNTIL_TAG);
        data.remove(INCREASE_AGI_AMOUNT_TAG);
    }

    public static void clearDecreaseAgi(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(DECREASE_AGI_UNTIL_TAG);
        data.remove(DECREASE_AGI_AMOUNT_TAG);
    }

    public static void applyBlessing(LivingEntity target, int durationTicks, int statBonus) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(BLESSING_UNTIL_TAG, until);
        data.putInt(BLESSING_AMOUNT_TAG, Math.max(0, statBonus));
    }

    public static void applyOffensiveBlessing(LivingEntity target, int durationTicks, double statMultiplier) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(OFFENSIVE_BLESSING_UNTIL_TAG, until);
        data.putDouble(OFFENSIVE_BLESSING_STAT_MULTIPLIER_TAG, Math.max(0.0D, Math.min(1.0D, statMultiplier)));
    }

    public static void clearOffensiveBlessing(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(OFFENSIVE_BLESSING_UNTIL_TAG);
        data.remove(OFFENSIVE_BLESSING_STAT_MULTIPLIER_TAG);
    }

    public static void clearBlessing(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(BLESSING_UNTIL_TAG);
        data.remove(BLESSING_AMOUNT_TAG);
    }

    public static void applyAngelus(LivingEntity target, int durationTicks, double softDefenseMultiplier) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(ANGELUS_UNTIL_TAG, until);
        data.putDouble(ANGELUS_SOFT_DEF_MULTIPLIER_TAG, Math.max(1.0D, softDefenseMultiplier));
    }

    public static void clearAngelus(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(ANGELUS_UNTIL_TAG);
        data.remove(ANGELUS_SOFT_DEF_MULTIPLIER_TAG);
    }

    public static void applySignumCrucis(LivingEntity target, int durationTicks, double hardDefenseMultiplier) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(SIGNUM_UNTIL_TAG, until);
        data.putDouble(SIGNUM_HARD_DEF_MULTIPLIER_TAG, Math.max(0.0D, hardDefenseMultiplier));
    }

    public static void applyImproveConcentration(LivingEntity target, int durationTicks, int level) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(IMPROVE_CONCENTRATION_UNTIL_TAG, until);
        data.putInt(IMPROVE_CONCENTRATION_LEVEL_TAG, Math.max(1, level));
    }

    public static void applyPoison(LivingEntity target, int durationTicks) {
        if (target == null) {
            return;
        }
        long now = target.level().getGameTime();
        CompoundTag data = target.getPersistentData();
        data.putLong(POISON_UNTIL_TAG, now + Math.max(1, durationTicks));
        data.putLong(POISON_NEXT_TICK_TAG, now + 60L);
    }

    public static void clearPoison(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(POISON_UNTIL_TAG);
        data.remove(POISON_NEXT_TICK_TAG);
    }

    public static void applyFrozen(LivingEntity target, int durationTicks) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        target.getPersistentData().putLong(FROZEN_UNTIL_TAG, until);
    }

    public static void clearFrozen(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.getPersistentData().remove(FROZEN_UNTIL_TAG);
    }

    public static void applyStoneCurse(LivingEntity target, int durationTicks) {
        if (target == null) {
            return;
        }
        clearFrozen(target);
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        target.getPersistentData().putLong(STONE_CURSE_UNTIL_TAG, until);
    }

    public static void clearStoneCurse(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.getPersistentData().remove(STONE_CURSE_UNTIL_TAG);
    }

    public static void applySilence(LivingEntity target, int durationTicks) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        target.getPersistentData().putLong(SILENCE_UNTIL_TAG, until);
    }

    public static void clearSilence(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.getPersistentData().remove(SILENCE_UNTIL_TAG);
    }

    public static void applyBlind(LivingEntity target, int durationTicks) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        target.getPersistentData().putLong(BLIND_UNTIL_TAG, until);
    }

    public static void clearBlind(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.getPersistentData().remove(BLIND_UNTIL_TAG);
    }

    public static void applyChaos(LivingEntity target, int durationTicks) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        target.getPersistentData().putLong(CHAOS_UNTIL_TAG, until);
    }

    public static void clearChaos(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.getPersistentData().remove(CHAOS_UNTIL_TAG);
    }

    public static void applySight(LivingEntity target, int durationTicks) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        target.getPersistentData().putLong(SIGHT_UNTIL_TAG, until);
    }

    public static void applyHiding(LivingEntity target, int durationTicks) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        target.getPersistentData().putLong(HIDING_UNTIL_TAG, until);
    }

    public static void clearHiding(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.getPersistentData().remove(HIDING_UNTIL_TAG);
    }

    public static void clearSight(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.getPersistentData().remove(SIGHT_UNTIL_TAG);
    }

    public static void clearImproveConcentration(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(IMPROVE_CONCENTRATION_UNTIL_TAG);
        data.remove(IMPROVE_CONCENTRATION_LEVEL_TAG);
    }

    public static void clearSignumCrucis(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(SIGNUM_UNTIL_TAG);
        data.remove(SIGNUM_HARD_DEF_MULTIPLIER_TAG);
    }

    public static void applyOverThrust(LivingEntity target, int durationTicks, int level) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(OVER_THRUST_UNTIL_TAG, until);
        data.putInt(OVER_THRUST_LEVEL_TAG, Math.max(0, level));
    }

    public static void clearOverThrust(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(OVER_THRUST_UNTIL_TAG);
        data.remove(OVER_THRUST_LEVEL_TAG);
    }

    public static void applyMagnumBreakFireBonus(LivingEntity target, int durationTicks, double fireBonusRatio) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(MAGNUM_BREAK_UNTIL_TAG, until);
        data.putDouble(MAGNUM_BREAK_FIRE_BONUS_TAG, Math.max(0.0D, fireBonusRatio));
    }

    public static void clearMagnumBreakFireBonus(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(MAGNUM_BREAK_UNTIL_TAG);
        data.remove(MAGNUM_BREAK_FIRE_BONUS_TAG);
    }

    public static void applyEndure(LivingEntity target, int durationTicks, int mdefBonus, int monsterHitLimit) {
        if (target == null) {
            return;
        }
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(ENDURE_UNTIL_TAG, until);
        data.putInt(ENDURE_MDEF_BONUS_TAG, Math.max(0, mdefBonus));
        data.putInt(ENDURE_REMAINING_MONSTER_HITS_TAG, Math.max(0, monsterHitLimit));
    }

    public static void clearEndure(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(ENDURE_UNTIL_TAG);
        data.remove(ENDURE_MDEF_BONUS_TAG);
        data.remove(ENDURE_REMAINING_MONSTER_HITS_TAG);
    }

    public static double physicalAttackMultiplier(LivingEntity entity) {
        if (isActive(entity, OVER_THRUST_UNTIL_TAG)) {
            return 1.0D + (entity.getPersistentData().getInt(OVER_THRUST_LEVEL_TAG) * 0.05D);
        }
        if (!isActive(entity, PROVOKE_UNTIL_TAG)) {
            return 1.0D;
        }
        return 1.0D + (entity.getPersistentData().getDouble(PROVOKE_ATK_BONUS_TAG) / 100.0D);
    }

    public static double magnumBreakFireBonusRatio(LivingEntity entity) {
        if (!isActive(entity, MAGNUM_BREAK_UNTIL_TAG)) {
            return 0.0D;
        }
        return Math.max(0.0D, entity.getPersistentData().getDouble(MAGNUM_BREAK_FIRE_BONUS_TAG));
    }

    public static int endureMdefBonus(LivingEntity entity) {
        if (!isActive(entity, ENDURE_UNTIL_TAG)) {
            return 0;
        }
        return Math.max(0, entity.getPersistentData().getInt(ENDURE_MDEF_BONUS_TAG));
    }

    public static boolean hasEndure(LivingEntity entity) {
        return isActive(entity, ENDURE_UNTIL_TAG);
    }

    public static void consumeEndureMonsterHit(LivingEntity target) {
        if (!hasEndure(target)) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        int remaining = Math.max(0, data.getInt(ENDURE_REMAINING_MONSTER_HITS_TAG) - 1);
        if (remaining <= 0) {
            clearEndure(target);
            return;
        }
        data.putInt(ENDURE_REMAINING_MONSTER_HITS_TAG, remaining);
    }

    public static double physicalDefenseMultiplier(LivingEntity entity) {
        double multiplier = signumHardDefenseMultiplier(entity);
        if (hasPoison(entity)) {
            multiplier *= 0.75D;
        }
        if (!isActive(entity, PROVOKE_UNTIL_TAG)) {
            return multiplier;
        }
        double reduction = entity.getPersistentData().getDouble(PROVOKE_DEF_REDUCTION_TAG) / 100.0D;
        return multiplier * Math.max(0.0D, 1.0D - reduction);
    }

    public static int agiPenalty(LivingEntity entity) {
        if (!isActive(entity, DECREASE_AGI_UNTIL_TAG)) {
            return 0;
        }
        return Math.max(0, entity.getPersistentData().getInt(DECREASE_AGI_AMOUNT_TAG));
    }

    public static int statModifier(LivingEntity entity, StatKeys key) {
        if (entity == null || key == null) {
            return 0;
        }
        int modifier = 0;
        if (isActive(entity, BLESSING_UNTIL_TAG)
                && (key == StatKeys.STR || key == StatKeys.DEX || key == StatKeys.INT)) {
            modifier += Math.max(0, entity.getPersistentData().getInt(BLESSING_AMOUNT_TAG));
        }
        if (key == StatKeys.AGI) {
            if (isActive(entity, INCREASE_AGI_UNTIL_TAG)) {
                modifier += Math.max(0, entity.getPersistentData().getInt(INCREASE_AGI_AMOUNT_TAG));
            }
            if (isActive(entity, DECREASE_AGI_UNTIL_TAG)) {
                modifier -= Math.max(0, entity.getPersistentData().getInt(DECREASE_AGI_AMOUNT_TAG));
            }
        }
        return modifier;
    }

    public static double angelusSoftDefenseMultiplier(LivingEntity entity) {
        if (!isActive(entity, ANGELUS_UNTIL_TAG)) {
            return 1.0D;
        }
        return Math.max(1.0D, entity.getPersistentData().getDouble(ANGELUS_SOFT_DEF_MULTIPLIER_TAG));
    }

    public static double offensiveBlessingStatMultiplier(LivingEntity entity) {
        if (!isActive(entity, OFFENSIVE_BLESSING_UNTIL_TAG)) {
            return 1.0D;
        }
        return Math.max(0.0D, Math.min(1.0D,
                entity.getPersistentData().getDouble(OFFENSIVE_BLESSING_STAT_MULTIPLIER_TAG)));
    }

    public static int improveConcentrationLevel(LivingEntity entity) {
        if (!isActive(entity, IMPROVE_CONCENTRATION_UNTIL_TAG)) {
            return 0;
        }
        return Math.max(1, entity.getPersistentData().getInt(IMPROVE_CONCENTRATION_LEVEL_TAG));
    }

    public static boolean hasPoison(LivingEntity entity) {
        return isActive(entity, POISON_UNTIL_TAG);
    }

    public static boolean hasFrozen(LivingEntity entity) {
        return isActive(entity, FROZEN_UNTIL_TAG);
    }

    public static boolean hasStoneCurse(LivingEntity entity) {
        return isActive(entity, STONE_CURSE_UNTIL_TAG);
    }

    public static boolean hasSilence(LivingEntity entity) {
        return isActive(entity, SILENCE_UNTIL_TAG);
    }

    public static boolean hasBlind(LivingEntity entity) {
        return isActive(entity, BLIND_UNTIL_TAG);
    }

    public static boolean hasChaos(LivingEntity entity) {
        return isActive(entity, CHAOS_UNTIL_TAG);
    }

    public static boolean hasHiding(LivingEntity entity) {
        return isActive(entity, HIDING_UNTIL_TAG);
    }

    public static boolean blocksCast(LivingEntity entity) {
        return hasSilence(entity);
    }

    public static double hitMultiplier(LivingEntity entity) {
        return hasBlind(entity) ? 0.75D : 1.0D;
    }

    public static double fleeMultiplier(LivingEntity entity) {
        return hasBlind(entity) ? 0.75D : 1.0D;
    }

    public static boolean hasSight(LivingEntity entity) {
        return isActive(entity, SIGHT_UNTIL_TAG);
    }

    public static boolean canDetectHiding(LivingEntity observer) {
        if (observer == null) {
            return false;
        }
        if (hasSight(observer)) {
            return true;
        }
        if (observer instanceof Mob mob) {
            String path = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()) == null
                    ? ""
                    : net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).getPath();
            return mob.getMobType() == net.minecraft.world.entity.MobType.ARTHROPOD
                    || path.contains("spider")
                    || path.contains("enderman")
                    || path.contains("vex")
                    || path.contains("warden")
                    || path.contains("wither")
                    || path.contains("ender_dragon");
        }
        return false;
    }

    public static boolean revealHiding(LivingEntity target) {
        if (!hasHiding(target)) {
            return false;
        }
        clearHiding(target);
        target.removeEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY);
        return true;
    }

    public static void tickHiding(LivingEntity entity) {
        if (!hasHiding(entity)) {
            return;
        }
        entity.setDeltaMovement(0.0D, entity.getDeltaMovement().y, 0.0D);
        entity.hurtMarked = true;
    }

    public static void tickChaos(LivingEntity entity) {
        if (!hasChaos(entity)) {
            return;
        }
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            if (entity.level().getGameTime() % 20L == 0L) {
                double dx = (entity.getRandom().nextDouble() - 0.5D) * 0.35D;
                double dz = (entity.getRandom().nextDouble() - 0.5D) * 0.35D;
                mob.getNavigation().moveTo(entity.getX() + dx * 6.0D, entity.getY(), entity.getZ() + dz * 6.0D, 0.8D);
            }
            return;
        }
        if (entity.level().getGameTime() % 10L == 0L) {
            double dx = (entity.getRandom().nextDouble() - 0.5D) * 0.18D;
            double dz = (entity.getRandom().nextDouble() - 0.5D) * 0.18D;
            entity.push(dx, 0.0D, dz);
            entity.hurtMarked = true;
        }
    }

    public static void tickPoison(LivingEntity entity) {
        if (!hasPoison(entity)) {
            return;
        }
        long now = entity.level().getGameTime();
        CompoundTag data = entity.getPersistentData();
        long nextTick = data.getLong(POISON_NEXT_TICK_TAG);
        if (nextTick > now) {
            return;
        }
        data.putLong(POISON_NEXT_TICK_TAG, now + 60L);
        float floor = entity.getMaxHealth() * 0.25F;
        if (entity.getHealth() <= floor) {
            return;
        }
        float damage = entity.getMaxHealth() * 0.03F;
        float allowed = Math.max(0.0F, entity.getHealth() - floor);
        if (allowed <= 0.0F) {
            return;
        }
        entity.hurt(entity.damageSources().magic(), Math.min(damage, allowed));
    }

    public static double signumHardDefenseMultiplier(LivingEntity entity) {
        if (!isActive(entity, SIGNUM_UNTIL_TAG)) {
            return 1.0D;
        }
        return Math.max(0.0D, entity.getPersistentData().getDouble(SIGNUM_HARD_DEF_MULTIPLIER_TAG));
    }

    public static void clearExpired(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        if (isExpired(entity, PROVOKE_UNTIL_TAG) && entity instanceof Mob mob) {
            clearProvoke(mob);
        }
        if (isExpired(entity, DECREASE_AGI_UNTIL_TAG)) {
            clearDecreaseAgi(entity);
        }
        if (isExpired(entity, INCREASE_AGI_UNTIL_TAG)) {
            clearIncreaseAgi(entity);
        }
        if (isExpired(entity, BLESSING_UNTIL_TAG)) {
            clearBlessing(entity);
        }
        if (isExpired(entity, OFFENSIVE_BLESSING_UNTIL_TAG)) {
            clearOffensiveBlessing(entity);
        }
        if (isExpired(entity, ANGELUS_UNTIL_TAG)) {
            clearAngelus(entity);
        }
        if (isExpired(entity, SIGNUM_UNTIL_TAG)) {
            clearSignumCrucis(entity);
        }
        if (isExpired(entity, IMPROVE_CONCENTRATION_UNTIL_TAG)) {
            clearImproveConcentration(entity);
        }
        if (isExpired(entity, POISON_UNTIL_TAG)) {
            clearPoison(entity);
        }
        if (isExpired(entity, FROZEN_UNTIL_TAG)) {
            clearFrozen(entity);
        }
        if (isExpired(entity, STONE_CURSE_UNTIL_TAG)) {
            clearStoneCurse(entity);
        }
        if (isExpired(entity, SILENCE_UNTIL_TAG)) {
            clearSilence(entity);
        }
        if (isExpired(entity, BLIND_UNTIL_TAG)) {
            clearBlind(entity);
        }
        if (isExpired(entity, CHAOS_UNTIL_TAG)) {
            clearChaos(entity);
        }
        if (isExpired(entity, SIGHT_UNTIL_TAG)) {
            clearSight(entity);
        }
        if (isExpired(entity, HIDING_UNTIL_TAG)) {
            clearHiding(entity);
        }
        if (isExpired(entity, OVER_THRUST_UNTIL_TAG)) {
            clearOverThrust(entity);
        }
        if (isExpired(entity, MAGNUM_BREAK_UNTIL_TAG)) {
            clearMagnumBreakFireBonus(entity);
        }
        if (isExpired(entity, ENDURE_UNTIL_TAG)) {
            clearEndure(entity);
        }
    }

    private static boolean isActive(LivingEntity entity, String untilTag) {
        if (entity == null) {
            return false;
        }
        long until = entity.getPersistentData().getLong(untilTag);
        return until > entity.level().getGameTime();
    }

    private static boolean isExpired(LivingEntity entity, String untilTag) {
        if (entity == null) {
            return false;
        }
        long until = entity.getPersistentData().getLong(untilTag);
        return until > 0 && until <= entity.level().getGameTime();
    }
}
