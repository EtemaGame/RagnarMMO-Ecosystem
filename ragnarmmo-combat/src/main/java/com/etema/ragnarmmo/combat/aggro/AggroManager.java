package com.etema.ragnarmmo.combat.aggro;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.UUID;

public final class AggroManager {
    public static final String AGGRO_TARGET_TAG = "ragnar_aggro_target";
    public static final String AGGRO_UNTIL_TAG = "ragnar_aggro_until";

    private AggroManager() {
    }

    public static void applyAggro(Mob mob, LivingEntity tauntedBy, int durationTicks) {
        if (mob == null || tauntedBy == null) {
            return;
        }
        CompoundTag data = mob.getPersistentData();
        data.putString(AGGRO_TARGET_TAG, tauntedBy.getUUID().toString());
        data.putLong(AGGRO_UNTIL_TAG, mob.level().getGameTime() + Math.max(1, durationTicks));
    }

    public static UUID getAggroTarget(Mob mob) {
        if (mob == null) {
            return null;
        }
        CompoundTag data = mob.getPersistentData();
        if (!data.contains(AGGRO_TARGET_TAG)) {
            return null;
        }
        long until = data.getLong(AGGRO_UNTIL_TAG);
        if (mob.level().getGameTime() >= until) {
            clearAggro(mob);
            return null;
        }
        try {
            return UUID.fromString(data.getString(AGGRO_TARGET_TAG));
        } catch (IllegalArgumentException ex) {
            clearAggro(mob);
            return null;
        }
    }

    public static void clearAggro(Mob mob) {
        if (mob == null) {
            return;
        }
        CompoundTag data = mob.getPersistentData();
        data.remove(AGGRO_TARGET_TAG);
        data.remove(AGGRO_UNTIL_TAG);
    }

    public static boolean hasAggro(Mob mob) {
        return getAggroTarget(mob) != null;
    }
}
