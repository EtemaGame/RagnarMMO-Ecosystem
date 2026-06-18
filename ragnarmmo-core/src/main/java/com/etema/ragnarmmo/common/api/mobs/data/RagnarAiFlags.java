package com.etema.ragnarmmo.common.api.mobs.data;

public record RagnarAiFlags(
        RagnarAggroType aggroType,
        boolean looter,
        boolean canMove,
        boolean canAttack,
        boolean immobile,
        boolean retaliates,
        boolean detector,
        boolean changeTargetOnAttack,
        boolean changeTargetOnChase,
        boolean castSensorIdle,
        boolean castSensorChase) {

    public RagnarAiFlags {
        if (aggroType == null) {
            throw new IllegalArgumentException("aggroType must not be null");
        }
    }
}
