package com.etema.ragnarmmo.combat.resolver;

import net.minecraft.world.entity.LivingEntity;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class MobCombatProfileResolver {
    private MobCombatProfileResolver() {
    }

    public static OptionalInt tryGetResolvedMobHit(LivingEntity entity) {
        return OptionalInt.empty();
    }

    public static OptionalInt tryGetResolvedMobFlee(LivingEntity entity) {
        return OptionalInt.empty();
    }

    public static OptionalDouble tryGetResolvedMobCritChance(LivingEntity entity) {
        return OptionalDouble.empty();
    }

    public static OptionalInt tryGetResolvedMobAspd(LivingEntity entity) {
        return OptionalInt.empty();
    }

    public static OptionalInt tryGetResolvedMobAttackIntervalTicks(LivingEntity entity) {
        return OptionalInt.empty();
    }
}
