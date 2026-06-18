package com.etema.ragnarmmo.combat.api;

public record RagnarTargetCandidate(int entityId, double distance, boolean clientLineOfSight) {
    public static RagnarTargetCandidate from(int entityId, RagnarTargetSource source) {
        return new RagnarTargetCandidate(entityId, 0.0D, source == RagnarTargetSource.CLIENT_AIM);
    }
}
