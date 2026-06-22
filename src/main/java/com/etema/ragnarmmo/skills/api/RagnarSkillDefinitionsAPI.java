package com.etema.ragnarmmo.skills.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class RagnarSkillDefinitionsAPI {
    @FunctionalInterface
    public interface DefinitionAccessor extends Function<ResourceLocation, Optional<ISkillDefinition>> {
    }

    private static final DefinitionAccessor DEFAULT_ACCESSOR = id -> Optional.empty();
    private static final AtomicReference<DefinitionAccessor> ACCESSOR = new AtomicReference<>(DEFAULT_ACCESSOR);

    private RagnarSkillDefinitionsAPI() {
    }

    public static void registerAccessor(DefinitionAccessor accessor) {
        ACCESSOR.set(accessor != null ? accessor : DEFAULT_ACCESSOR);
    }

    public static Optional<ISkillDefinition> get(ResourceLocation id) {
        if (id == null) {
            return Optional.empty();
        }
        return ACCESSOR.get().apply(id);
    }

    public static Map<ResourceLocation, ISkillDefinition> getLocalDefinitionsSnapshot() {
        return Map.of();
    }
}
