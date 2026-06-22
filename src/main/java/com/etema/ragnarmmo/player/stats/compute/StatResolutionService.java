package com.etema.ragnarmmo.player.stats.compute;

import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import com.etema.ragnarmmo.player.stats.progression.JobBonusService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class StatResolutionService {
    private StatResolutionService() {
    }

    public static void resolve(Player player, IPlayerStats stats) {
        if (player == null || stats == null) {
            return;
        }

        JobBonusService.recomputeStats(player, stats);

        if (player instanceof ServerPlayer serverPlayer) {
            DerivedStats derived = DerivedStatsService.compute(serverPlayer, stats)
                    .orElseGet(() -> CoreDerivedStatsCalculator.compute(serverPlayer, stats));
            applyServerDerivedStats(serverPlayer, stats, derived);
            PlayerStatsSyncService.sync(serverPlayer, stats);
        }
    }

    private static void applyServerDerivedStats(ServerPlayer player, IPlayerStats stats, DerivedStats derived) {
        var maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && derived.maxHealth > 0.0D) {
            maxHealth.setBaseValue(derived.maxHealth);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        if (derived.maxMana > 0.0D) {
            stats.setManaMaxClient(derived.maxMana);
        }
        if (derived.maxSP > 0.0D) {
            stats.setSPMaxClient(derived.maxSP);
        }
    }
}
