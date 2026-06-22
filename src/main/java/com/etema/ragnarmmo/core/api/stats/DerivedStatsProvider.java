package com.etema.ragnarmmo.core.api.stats;

import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface DerivedStatsProvider {
    DerivedStats compute(ServerPlayer player, IPlayerStats stats);
}
