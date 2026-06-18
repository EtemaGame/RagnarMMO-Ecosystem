package com.etema.ragnarmmo.core.client;

import com.etema.ragnarmmo.common.api.compute.DerivedStats;

public final class DerivedStatsClientCache {
    private static DerivedStats current = new DerivedStats();

    private DerivedStatsClientCache() {
    }

    public static DerivedStats get() {
        return current;
    }

    public static void update(DerivedStats stats) {
        current = stats == null ? new DerivedStats() : stats;
    }
}
