package com.etema.ragnarmmo.common.api.mobs.data;

import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public record RagnarSpawnDefinition(
        boolean naturalSpawn,
        List<ResourceLocation> biomeTags,
        List<ResourceLocation> structureTags,
        int weight,
        int minGroup,
        int maxGroup,
        Integer minY,
        Integer maxY,
        Integer lightMin,
        Integer lightMax,
        boolean surfaceOnly,
        boolean requiresSky,
        Double temperatureMax,
        RagnarBlockProximityRule nearBlocks,
        Set<RagnarSpawnReason> spawnReasons,
        boolean manualOnly) {

    public RagnarSpawnDefinition {
        biomeTags = List.copyOf(biomeTags == null ? List.of() : biomeTags);
        structureTags = List.copyOf(structureTags == null ? List.of() : structureTags);
        spawnReasons = Set.copyOf(spawnReasons == null ? Set.of() : spawnReasons);
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be > 0");
        }
        if (minGroup <= 0) {
            throw new IllegalArgumentException("minGroup must be > 0");
        }
        if (maxGroup < minGroup) {
            throw new IllegalArgumentException("maxGroup must be >= minGroup");
        }
        if (minY != null && maxY != null && minY > maxY) {
            throw new IllegalArgumentException("minY must be <= maxY");
        }
        if (lightMin != null && lightMax != null && lightMin > lightMax) {
            throw new IllegalArgumentException("lightMin must be <= lightMax");
        }
        if (temperatureMax != null && temperatureMax < 0.0D) {
            throw new IllegalArgumentException("temperatureMax must be >= 0");
        }
        if (!naturalSpawn && spawnReasons.contains(RagnarSpawnReason.NATURAL)) {
            throw new IllegalArgumentException("spawnReasons must not contain NATURAL when naturalSpawn=false");
        }
        boolean hasNonNaturalSpawnPath = manualOnly
                || !structureTags.isEmpty()
                || spawnReasons.contains(RagnarSpawnReason.STRUCTURE)
                || spawnReasons.contains(RagnarSpawnReason.SPAWNER)
                || spawnReasons.contains(RagnarSpawnReason.EVENT)
                || spawnReasons.contains(RagnarSpawnReason.MANUAL);
        if (!naturalSpawn && !hasNonNaturalSpawnPath) {
            throw new IllegalArgumentException("spawn must define a non-natural spawn path when naturalSpawn=false");
        }
    }
}
