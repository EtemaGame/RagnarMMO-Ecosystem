package com.etema.ragnarmmo.combat.api;

public record CombatResolution(
        int targetEntityId,
        CombatHitResultType resultType,
        double rawDamage,
        double finalDamage,
        boolean critical,
        double hitRate) {

    public boolean dealsDamage() {
        return resultType == CombatHitResultType.HIT || resultType == CombatHitResultType.CRIT;
    }
}
