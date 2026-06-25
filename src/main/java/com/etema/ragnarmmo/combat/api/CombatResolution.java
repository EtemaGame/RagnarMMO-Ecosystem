package com.etema.ragnarmmo.combat.api;

public record CombatResolution(
        int targetEntityId,
        CombatHitResultType resultType,
        double rawDamage,
        double finalDamage,
        boolean critical,
        double hitRate,
        int hitCount) {

    public CombatResolution(
            int targetEntityId,
            CombatHitResultType resultType,
            double rawDamage,
            double finalDamage,
            boolean critical,
            double hitRate) {
        this(targetEntityId, resultType, rawDamage, finalDamage, critical, hitRate, 1);
    }

    public CombatResolution {
        hitCount = Math.max(1, hitCount);
    }

    public boolean dealsDamage() {
        return resultType == CombatHitResultType.HIT || resultType == CombatHitResultType.CRIT;
    }
}
