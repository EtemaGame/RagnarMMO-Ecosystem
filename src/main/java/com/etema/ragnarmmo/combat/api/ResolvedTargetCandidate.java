package com.etema.ragnarmmo.combat.api;

public record ResolvedTargetCandidate(
        int entityId,
        boolean accepted,
        TargetRejectReason rejectReason,
        double distance) {

    public static ResolvedTargetCandidate accepted(int entityId, double distance) {
        return new ResolvedTargetCandidate(entityId, true, TargetRejectReason.NONE, distance);
    }

    public static ResolvedTargetCandidate rejected(int entityId, TargetRejectReason reason, double distance) {
        return new ResolvedTargetCandidate(entityId, false,
                reason == null ? TargetRejectReason.TARGET_NOT_FOUND : reason, distance);
    }
}
