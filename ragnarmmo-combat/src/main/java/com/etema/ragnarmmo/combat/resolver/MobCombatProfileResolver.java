package com.etema.ragnarmmo.combat.resolver;

import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileProvider;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileState;
import net.minecraft.world.entity.LivingEntity;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class MobCombatProfileResolver {
    private MobCombatProfileResolver() {
    }

    public static OptionalInt tryGetResolvedMobHit(LivingEntity entity) {
        return profile(entity).map(profile -> OptionalInt.of(profile.hit())).orElseGet(OptionalInt::empty);
    }

    public static OptionalInt tryGetResolvedMobFlee(LivingEntity entity) {
        return profile(entity).map(profile -> OptionalInt.of(profile.flee())).orElseGet(OptionalInt::empty);
    }

    public static OptionalDouble tryGetResolvedMobCritChance(LivingEntity entity) {
        return profile(entity).map(profile -> OptionalDouble.of(profile.crit() / 100.0D)).orElseGet(OptionalDouble::empty);
    }

    public static OptionalInt tryGetResolvedMobAspd(LivingEntity entity) {
        return profile(entity).map(profile -> OptionalInt.of(profile.aspd())).orElseGet(OptionalInt::empty);
    }

    public static OptionalInt tryGetResolvedMobAttackIntervalTicks(LivingEntity entity) {
        return tryGetResolvedMobAspd(entity).stream()
                .mapToObj(aspd -> OptionalInt.of(Math.max(1, (int) Math.round(20.0D / Math.max(0.25D, 50.0D / (200.0D - Math.min(190, aspd)))))))
                .findFirst()
                .orElseGet(OptionalInt::empty);
    }

    public static OptionalInt tryGetResolvedMobLevel(LivingEntity entity) {
        return profile(entity).map(profile -> OptionalInt.of(profile.level())).orElseGet(OptionalInt::empty);
    }

    public static OptionalInt tryGetResolvedMobSoftDefense(LivingEntity entity) {
        return profile(entity).map(profile -> OptionalInt.of(profile.def())).orElseGet(OptionalInt::empty);
    }

    public static OptionalInt tryGetResolvedMobHardDefense(LivingEntity entity) {
        return profile(entity).map(profile -> OptionalInt.of(profile.def())).orElseGet(OptionalInt::empty);
    }

    private static java.util.Optional<com.etema.ragnarmmo.common.api.mobs.profile.MobProfile> profile(LivingEntity entity) {
        return MobProfileProvider.get(entity).resolve()
                .filter(MobProfileState::isInitialized)
                .map(MobProfileState::profile);
    }
}
