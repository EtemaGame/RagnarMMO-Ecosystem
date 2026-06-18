package com.etema.ragnarmmo.combat.status;

import net.minecraft.nbt.CompoundTag;
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
        long until = target.level().getGameTime() + Math.max(1, durationTicks);
        CompoundTag data = target.getPersistentData();
        data.putLong(DECREASE_AGI_UNTIL_TAG, until);
        data.putInt(DECREASE_AGI_AMOUNT_TAG, Math.max(0, agiReduction));
    }

    public static void clearDecreaseAgi(LivingEntity target) {
        if (target == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        data.remove(DECREASE_AGI_UNTIL_TAG);
        data.remove(DECREASE_AGI_AMOUNT_TAG);
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

    public static double physicalAttackMultiplier(LivingEntity entity) {
        if (isActive(entity, OVER_THRUST_UNTIL_TAG)) {
            return 1.0D + (entity.getPersistentData().getInt(OVER_THRUST_LEVEL_TAG) * 0.05D);
        }
        if (!isActive(entity, PROVOKE_UNTIL_TAG)) {
            return 1.0D;
        }
        return 1.0D + (entity.getPersistentData().getDouble(PROVOKE_ATK_BONUS_TAG) / 100.0D);
    }

    public static double physicalDefenseMultiplier(LivingEntity entity) {
        if (!isActive(entity, PROVOKE_UNTIL_TAG)) {
            return 1.0D;
        }
        double reduction = entity.getPersistentData().getDouble(PROVOKE_DEF_REDUCTION_TAG) / 100.0D;
        return Math.max(0.0D, 1.0D - reduction);
    }

    public static int agiPenalty(LivingEntity entity) {
        if (!isActive(entity, DECREASE_AGI_UNTIL_TAG)) {
            return 0;
        }
        return Math.max(0, entity.getPersistentData().getInt(DECREASE_AGI_AMOUNT_TAG));
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
        if (isExpired(entity, OVER_THRUST_UNTIL_TAG)) {
            clearOverThrust(entity);
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
