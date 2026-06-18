package com.etema.ragnarmmo.common.api.mobs.query;

import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class MobConsumerReadViewResolver {
    private static final AtomicReference<Function<LivingEntity, Optional<MobConsumerReadView>>> RESOLVER =
            new AtomicReference<>(entity -> Optional.empty());

    private MobConsumerReadViewResolver() {
    }

    public static void register(Function<LivingEntity, Optional<MobConsumerReadView>> resolver) {
        RESOLVER.set(Objects.requireNonNull(resolver, "resolver"));
    }

    public static Optional<MobConsumerReadView> resolve(LivingEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return RESOLVER.get().apply(entity);
    }
}
