package com.etema.ragnarmmo.common.api.mobs.profile;

import com.etema.ragnarmmo.common.api.mobs.MobRank;

public final class MobRewardFormula {
    private MobRewardFormula() {
    }

    public static int expectedHits(MobRank rank) {
        return switch (rank == null ? MobRank.NORMAL : rank) {
            case NORMAL -> 12;
            case ELITE -> 28;
            case BOSS -> 90;
        };
    }

    public static int baseExp(int level, MobRank rank) {
        int safeLevel = Math.max(1, level);
        MobRank safeRank = rank == null ? MobRank.NORMAL : rank;
        double rankMultiplier = switch (safeRank) {
            case NORMAL -> 1.0D;
            case ELITE -> 2.4D;
            case BOSS -> 8.0D;
        };
        return Math.max(1, (int) Math.round((safeLevel * 4.0D + 8.0D) * expectedHits(safeRank) * rankMultiplier / 10.0D));
    }

    public static int jobExp(int level, MobRank rank) {
        return Math.max(1, (int) Math.round(baseExp(level, rank) * 0.65D));
    }
}
