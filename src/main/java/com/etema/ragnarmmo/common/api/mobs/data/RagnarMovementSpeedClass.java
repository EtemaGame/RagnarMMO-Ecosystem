package com.etema.ragnarmmo.common.api.mobs.data;

public enum RagnarMovementSpeedClass {
    SLOW_400(0.18D),
    MEDIUM_300(0.23D),
    FAST_200(0.30D),
    VERY_FAST_150(0.33D),
    VERY_FAST_155(0.34D),
    IMMOBILE(0.0D);

    private final double speed;

    RagnarMovementSpeedClass(double speed) {
        this.speed = speed;
    }

    public double speed() {
        return this.speed;
    }

    public boolean isImmobile() {
        return this == IMMOBILE;
    }
}
