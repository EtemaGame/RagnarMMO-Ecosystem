package com.etema.ragnarmmo.entity.mob;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public final class RagnarRetaliateMemory {
    private static final String TARGET_UUID_TAG = "ragnar_retaliate_target_uuid";
    private static final String EXPIRES_AT_TAG = "ragnar_retaliate_expires_at";
    private static final long DEFAULT_DURATION_TICKS = 100L;

    private RagnarRetaliateMemory() {
    }

    public static void mark(LivingEntity mob, LivingEntity attacker) {
        if (mob == null || attacker == null) {
            return;
        }
        CompoundTag data = mob.getPersistentData();
        data.putUUID(TARGET_UUID_TAG, attacker.getUUID());
        data.putLong(EXPIRES_AT_TAG, mob.level().getGameTime() + DEFAULT_DURATION_TICKS);
    }

    public static LivingEntity resolve(LivingEntity mob) {
        if (mob == null) {
            return null;
        }

        CompoundTag data = mob.getPersistentData();
        if (!data.hasUUID(TARGET_UUID_TAG)) {
            return null;
        }

        long expiresAt = data.getLong(EXPIRES_AT_TAG);
        if (mob.level().getGameTime() > expiresAt) {
            clear(mob);
            return null;
        }

        UUID uuid = data.getUUID(TARGET_UUID_TAG);
        if (mob.level() instanceof ServerLevel serverLevel) {
            var entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity living && living.isAlive()) {
                return living;
            }
        }

        return null;
    }

    public static void clear(LivingEntity mob) {
        if (mob == null) {
            return;
        }
        CompoundTag data = mob.getPersistentData();
        data.remove(TARGET_UUID_TAG);
        data.remove(EXPIRES_AT_TAG);
    }
}
