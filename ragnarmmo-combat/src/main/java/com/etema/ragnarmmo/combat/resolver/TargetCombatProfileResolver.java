package com.etema.ragnarmmo.combat.resolver;

import com.etema.ragnarmmo.combat.contract.CombatStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class TargetCombatProfileResolver {
    private TargetCombatProfileResolver() {
    }

    public static CombatStats getTargetStats(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return new CombatStats(
                    1, 1, 1, 1, 1, 1, Math.max(1, player.experienceLevel));
        }
        return new CombatStats(1, 1, 1, 1, 1, 1, 1);
    }

    public static java.util.OptionalInt tryGetTargetLevel(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return java.util.OptionalInt.of(Math.max(1, player.experienceLevel));
        }
        return MobCombatProfileResolver.tryGetResolvedMobLevel(entity);
    }
}
