package com.etema.ragnarmmo.common.api.mobs.data;

public record RagnarLootBehavior(
        double pickupRadius,
        boolean dropLootedItemsOnDeath) {

    public static final RagnarLootBehavior DEFAULT = new RagnarLootBehavior(6.0D, true);

    public RagnarLootBehavior {
        if (pickupRadius < 0.0D) {
            throw new IllegalArgumentException("pickupRadius must be >= 0");
        }
    }
}
