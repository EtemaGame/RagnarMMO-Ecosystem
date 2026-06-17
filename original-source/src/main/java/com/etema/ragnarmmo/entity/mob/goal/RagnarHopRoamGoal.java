package com.etema.ragnarmmo.entity.mob.goal;

import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementConfig;
import com.etema.ragnarmmo.entity.mob.AbstractRagnarMobEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.Goal;

public final class RagnarHopRoamGoal extends Goal {
    private final AbstractRagnarMobEntity mob;
    private final RagnarMovementConfig movement;
    private final double speedModifier;
    private final int minCooldown;
    private final int maxCooldown;
    private BlockPos targetPos;
    private int waitTime;
    private int hopCooldown;

    public RagnarHopRoamGoal(
            AbstractRagnarMobEntity mob,
            RagnarMovementConfig movement,
            double speedModifier,
            int minCooldown,
            int maxCooldown) {
        this.mob = mob;
        this.movement = movement;
        this.speedModifier = speedModifier;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.waitTime > 0) {
            this.waitTime--;
            return false;
        }
        if (this.mob == null
                || !this.mob.isAlive()
                || !this.mob.isEffectiveAi()
                || !this.mob.canMove()
                || this.mob.getTarget() != null
                || !this.mob.getNavigation().isDone()) {
            return false;
        }

        this.targetPos = findTargetPos();
        return this.targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPos != null
                && this.mob.getTarget() == null
                && !this.mob.getNavigation().isDone()
                && this.mob.canMove();
    }

    @Override
    public void start() {
        this.hopCooldown = this.minCooldown + this.mob.getRandom().nextInt(Math.max(1, this.maxCooldown - this.minCooldown + 1));
        if (this.targetPos != null) {
            Vec3 center = Vec3.atCenterOf(this.targetPos);
            this.mob.getLookControl().setLookAt(center.x, center.y, center.z, 30.0F, 30.0F);
            this.mob.getNavigation().moveTo(center.x, center.y, center.z, this.speedModifier);
        }
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        Vec3 center = Vec3.atCenterOf(this.targetPos);
        this.mob.getLookControl().setLookAt(center.x, center.y, center.z, 30.0F, 30.0F);
        if (this.mob.onGround() && this.hopCooldown-- <= 0) {
            this.mob.getJumpControl().jump();
            Vec3 delta = center.subtract(this.mob.position());
            Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
            if (horizontal.lengthSqr() > 1.0E-4D) {
                horizontal = horizontal.normalize().scale(this.speedModifier);
                this.mob.setDeltaMovement(
                        this.mob.getDeltaMovement().add(horizontal.x, 0.0D, horizontal.z));
            }
            this.hopCooldown = this.minCooldown + this.mob.getRandom().nextInt(Math.max(1, this.maxCooldown - this.minCooldown + 1));
        }

        if (this.mob.distanceToSqr(center) <= 2.25D || this.mob.getNavigation().isDone()) {
            stop();
        }
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.waitTime = 20 + this.mob.getRandom().nextInt(40);
    }

    private BlockPos findTargetPos() {
        BlockPos anchor = this.mob.movementAnchor();
        int wander = Math.max(1, this.movement.wanderRadius());
        int leash = Math.max(wander, this.movement.leashRadius());
        double distanceFromAnchor = Math.sqrt(this.mob.distanceToSqr(Vec3.atCenterOf(anchor)));
        int range = distanceFromAnchor >= leash ? leash : wander;
        if (range <= 0) {
            return null;
        }

        for (int i = 0; i < 10; i++) {
            int x = anchor.getX() + this.mob.getRandom().nextInt(range * 2 + 1) - range;
            int z = anchor.getZ() + this.mob.getRandom().nextInt(range * 2 + 1) - range;
            int y = this.mob.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            if (this.mob.level().getBlockState(candidate).isAir()
                    && this.mob.level().getBlockState(candidate.above()).isAir()) {
                return candidate;
            }
        }
        return null;
    }
}
