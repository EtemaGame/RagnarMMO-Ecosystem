package com.etema.ragnarmmo.common.api.mobs.data;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

public record RagnarBlockProximityRule(
        RagnarBlockProximityMode mode,
        int radius,
        List<ResourceLocation> values) {

    public RagnarBlockProximityRule {
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be >= 0");
        }
        values = List.copyOf(values == null ? List.of() : values);
    }
}
