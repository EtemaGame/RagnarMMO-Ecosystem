package com.etema.ragnarmmo.common.api;

import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class RagnarCoreAPI {
    @FunctionalInterface
    public interface StatsAccessor extends Function<Player, Optional<IPlayerStats>> {
    }

    private static final StatsAccessor DEFAULT_ACCESSOR = player -> Optional.empty();
    private static final AtomicReference<StatsAccessor> ACCESSOR = new AtomicReference<>(DEFAULT_ACCESSOR);

    private RagnarCoreAPI() {
    }

    public static void registerAccessor(StatsAccessor statsAccessor) {
        ACCESSOR.set(statsAccessor != null ? statsAccessor : DEFAULT_ACCESSOR);
    }

    public static Optional<IPlayerStats> get(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return ACCESSOR.get().apply(player);
    }

    public static boolean hasStatsAccessor() {
        return ACCESSOR.get() != DEFAULT_ACCESSOR;
    }
}
