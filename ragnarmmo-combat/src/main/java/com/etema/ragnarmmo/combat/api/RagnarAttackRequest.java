package com.etema.ragnarmmo.combat.api;

import java.util.List;

public record RagnarAttackRequest(
        int sequenceId,
        int comboIndex,
        boolean offHand,
        int selectedSlot,
        List<RagnarTargetCandidate> candidates) {

    public RagnarAttackRequest {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }

    public static RagnarAttackRequest singleTarget(int sequenceId, int entityId) {
        return singleTarget(sequenceId, entityId, false);
    }

    public static RagnarAttackRequest singleTarget(int sequenceId, int entityId, boolean offHand) {
        return new RagnarAttackRequest(sequenceId, 0, offHand, 0,
                List.of(new RagnarTargetCandidate(entityId, 0.0D, false)));
    }

    public static RagnarAttackRequest empty(int sequenceId) {
        return new RagnarAttackRequest(sequenceId, 0, false, 0, List.of());
    }
}
