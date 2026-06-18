package com.etema.ragnarmmo.common.api.mobs.data;

public record RagnarMovementConfig(
        RagnarMovementProfile profile,
        RagnarMovementSpeedClass speedClass,
        double hoverMin,
        double hoverMax,
        int wanderRadius,
        int leashRadius) {

    public static RagnarMovementConfig defaults() {
        return new RagnarMovementConfig(
                RagnarMovementProfile.GROUND_CRAWL,
                RagnarMovementSpeedClass.SLOW_400,
                0.0D,
                0.0D,
                8,
                16);
    }

    public RagnarMovementConfig {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null");
        }
        if (speedClass == null) {
            throw new IllegalArgumentException("speedClass must not be null");
        }
        if (hoverMin < 0.0D) {
            throw new IllegalArgumentException("hoverMin must be >= 0");
        }
        if (hoverMax < hoverMin) {
            throw new IllegalArgumentException("hoverMax must be >= hoverMin");
        }
        if (wanderRadius < 0) {
            throw new IllegalArgumentException("wanderRadius must be >= 0");
        }
        if (leashRadius < 0) {
            throw new IllegalArgumentException("leashRadius must be >= 0");
        }

        if (profile == RagnarMovementProfile.STATIONARY && !speedClass.isImmobile()) {
            throw new IllegalArgumentException("STATIONARY movement requires IMMOBILE speedClass");
        }
        if (profile != RagnarMovementProfile.STATIONARY && speedClass.isImmobile()) {
            throw new IllegalArgumentException("IMMOBILE speedClass is only valid with STATIONARY movement");
        }
        if (profile == RagnarMovementProfile.BUTTERFLY_FLIGHT && hoverMax <= 0.0D) {
            throw new IllegalArgumentException("BUTTERFLY_FLIGHT requires positive hover range");
        }
    }
}
