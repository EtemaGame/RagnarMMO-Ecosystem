package com.etema.ragnarmmo.common.api.mobs.profile;

import com.etema.ragnarmmo.common.api.mobs.MobRank;
import com.etema.ragnarmmo.common.api.stats.RoBaseStats;

public record MobProfile(
        int level,
        MobRank rank,
        RoBaseStats baseStats,
        int maxHp,
        int atkMin,
        int atkMax,
        int matkMin,
        int matkMax,
        int def,
        int mdef,
        int hit,
        int flee,
        int crit,
        int aspd,
        double moveSpeed,
        int baseExp,
        int jobExp,
        String race,
        String element,
        int elementLevel,
        String size) {
    public MobProfile {
        if (level < 1) throw new IllegalArgumentException("level must be >= 1");
        if (rank == null) throw new IllegalArgumentException("rank must not be null");
        if (baseStats == null) baseStats = RoBaseStats.novice();
        if (maxHp <= 0) throw new IllegalArgumentException("maxHp must be > 0");
        if (atkMin < 0) throw new IllegalArgumentException("atkMin must be >= 0");
        if (atkMax < atkMin) throw new IllegalArgumentException("atkMax must be >= atkMin");
        if (matkMin < 0) throw new IllegalArgumentException("matkMin must be >= 0");
        if (matkMax < matkMin) throw new IllegalArgumentException("matkMax must be >= matkMin");
        if (def < 0 || mdef < 0 || hit < 0 || flee < 0 || crit < 0) {
            throw new IllegalArgumentException("def, mdef, hit, flee, and crit must be >= 0");
        }
        if (aspd <= 0) throw new IllegalArgumentException("aspd must be > 0");
        if (moveSpeed <= 0.0D) throw new IllegalArgumentException("moveSpeed must be > 0");
        if (baseExp < 0 || jobExp < 0) throw new IllegalArgumentException("baseExp and jobExp must be >= 0");
        race = token(race, "unknown");
        element = token(element, "neutral");
        elementLevel = Math.max(1, Math.min(4, elementLevel));
        size = token(size, "medium");
    }

    private static String token(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
