package com.etema.ragnarmmo.jobs.runtime;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class JobSkillTargeting {
    private JobSkillTargeting() {
    }

    public static Optional<LivingEntity> findEntityInSight(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0F);
        Vec3 end = eye.add(view.scale(range));
        AABB searchBox = player.getBoundingBox().expandTowards(view.scale(range)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                eye,
                end,
                searchBox,
                entity -> entity instanceof LivingEntity living
                        && living.isAlive()
                        && entity != player
                        && player.hasLineOfSight(entity));
        return hit != null && hit.getEntity() instanceof LivingEntity living
                ? Optional.of(living)
                : Optional.empty();
    }
}
