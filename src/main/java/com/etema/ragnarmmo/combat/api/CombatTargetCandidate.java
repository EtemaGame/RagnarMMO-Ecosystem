package com.etema.ragnarmmo.combat.api;

public record CombatTargetCandidate(
        int entityId,
        String source,
        double distance,
        boolean lineOfSight) {
}
