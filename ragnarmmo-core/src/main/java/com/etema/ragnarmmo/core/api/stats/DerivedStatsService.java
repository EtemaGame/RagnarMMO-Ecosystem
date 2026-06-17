package com.etema.ragnarmmo.core.api.stats;

import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public final class DerivedStatsService {
    private static final AtomicReference<DerivedStatsProvider> PROVIDER = new AtomicReference<>();
    private static final List<DerivedStatsContributor> CONTRIBUTORS = new CopyOnWriteArrayList<>();

    private DerivedStatsService() {
    }

    public static void register(DerivedStatsProvider provider) {
        PROVIDER.set(provider);
    }

    public static void registerContributor(DerivedStatsContributor contributor) {
        if (contributor != null && !CONTRIBUTORS.contains(contributor)) {
            CONTRIBUTORS.add(contributor);
        }
    }

    public static Optional<DerivedStats> compute(ServerPlayer player, IPlayerStats stats) {
        DerivedStatsProvider provider = PROVIDER.get();
        if (provider == null || player == null || stats == null) {
            return Optional.empty();
        }
        DerivedStats derived = provider.compute(player, stats);
        if (derived == null) {
            return Optional.empty();
        }
        for (DerivedStatsContributor contributor : CONTRIBUTORS) {
            contributor.contribute(player, stats, derived);
        }
        return Optional.of(derived);
    }
}
