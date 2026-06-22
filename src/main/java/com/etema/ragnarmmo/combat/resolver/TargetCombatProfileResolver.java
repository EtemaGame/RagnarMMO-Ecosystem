package com.etema.ragnarmmo.combat.resolver;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.combat.contract.CombatStats;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class TargetCombatProfileResolver {
    private TargetCombatProfileResolver() {
    }

    public static CombatStats getTargetStats(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            int level = RagnarCoreAPI.get(player).map(stats -> Math.max(1, stats.getLevel())).orElse(1);
            return new CombatStats(
                    total(player, StatKeys.STR),
                    total(player, StatKeys.AGI),
                    total(player, StatKeys.VIT),
                    total(player, StatKeys.INT),
                    total(player, StatKeys.DEX),
                    total(player, StatKeys.LUK),
                    level);
        }
        return new CombatStats(1, 1, 1, 1, 1, 1, 1);
    }

    public static java.util.OptionalInt tryGetTargetLevel(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return RagnarCoreAPI.get(player)
                    .map(stats -> java.util.OptionalInt.of(Math.max(1, stats.getLevel())))
                    .orElseGet(java.util.OptionalInt::empty);
        }
        return MobCombatProfileResolver.tryGetResolvedMobLevel(entity);
    }

    private static int total(ServerPlayer player, StatKeys key) {
        return Math.max(1, (int) Math.round(StatAttributes.getTotal(player, key)));
    }
}
