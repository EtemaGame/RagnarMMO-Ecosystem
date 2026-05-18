package com.etema.ragnarmmo.common.api.mobs.data;

import net.minecraft.resources.ResourceLocation;

public record RagnarMetamorphosis(
        ResourceLocation target,
        double chancePerSecond) {

    public RagnarMetamorphosis {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
        if (chancePerSecond < 0.0D || chancePerSecond > 1.0D) {
            throw new IllegalArgumentException("chancePerSecond must be between 0 and 1");
        }
    }
}
