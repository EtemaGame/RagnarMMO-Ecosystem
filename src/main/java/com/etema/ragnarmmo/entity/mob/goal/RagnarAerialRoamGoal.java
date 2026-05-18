package com.etema.ragnarmmo.entity.mob.goal;

import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementConfig;
import com.etema.ragnarmmo.entity.mob.AbstractRagnarMobEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Low hover roam goal for floating mobs.
 *
 * <p>The goal only handles local movement, hover correction, and leash behavior.
 * Ecological preferences belong to spawn definitions.</p>
 */
public final class RagnarAerialRoamGoal extends Goal {
    private final AbstractRagnarMobEntity mob;
    private final RagnarMovementConfig movement;
    private BlockPos targetPos;
    private int waitTime;

    public RagnarAerialRoamGoal(AbstractRagnarMobEntity mob, RagnarMovementConfig movement) {
        this.mob = mob;
        this.movement = movement;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
        if (this.targetPos == null) {
            return;
        }

        Vec3 center = Vec3.atCenterOf(this.targetPos);
        this.mob.getLookControl().setLookAt(center.x, center.y, center.z, 30.0F, 30.0F);
        this.mob.getNavigation().moveTo(center.x, center.y, center.z, this.movement.speedClass().speed());
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        Vec3 center = Vec3.atCenterOf(this.targetPos);
        this.mob.getLookControl().setLookAt(center.x, center.y, center.z, 30.0F, 30.0F);
        if (!isTargetStillValid(this.targetPos)) {
            this.targetPos = findTargetPos();
            if (this.targetPos == null) {
                stop();
                return;
            }
            center = Vec3.atCenterOf(this.targetPos);
            this.mob.getNavigation().moveTo(center.x, center.y, center.z, this.movement.speedClass().speed());
        }

        if (this.mob.distanceToSqr(center) <= 4.0D || this.mob.getNavigation().isDone()) {
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
        int leashRadius = Math.max(1, this.movement.leashRadius());
        int wanderRadius = Math.max(1, this.movement.wanderRadius());

        if (distanceToAnchor(anchor) > leashRadius) {
            return findReturnToAnchorTarget(anchor, leashRadius);
        }

        for (int i = 0; i < 12; i++) {
            int x = anchor.getX() + this.mob.getRandom().nextInt(wanderRadius * 2 + 1) - wanderRadius;
            int z = anchor.getZ() + this.mob.getRandom().nextInt(wanderRadius * 2 + 1) - wanderRadius;
            BlockPos candidate = findHoverTarget(x, z);
            if (candidate != null) {
                return candidate;
            }
        }

        return findHoverTarget(anchor.getX(), anchor.getZ());
    }

    private BlockPos findReturnToAnchorTarget(BlockPos anchor, int leashRadius) {
        Vec3 current = this.mob.position();
        Vec3 center = Vec3.atCenterOf(anchor);
        Vec3 toAnchor = center.subtract(current);
        Vec3 horizontal = new Vec3(toAnchor.x, 0.0D, toAnchor.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return findHoverTarget(anchor.getX(), anchor.getZ());
        }

        Vec3 direction = horizontal.normalize();
        int x = Mth.floor(current.x + direction.x * leashRadius * 0.5D);
        int z = Mth.floor(current.z + direction.z * leashRadius * 0.5D);
        BlockPos candidate = findHoverTarget(x, z);
        if (candidate != null) {
            return candidate;
        }
        return findHoverTarget(anchor.getX(), anchor.getZ());
    }

    private BlockPos findHoverTarget(int x, int z) {
        Level level = this.mob.level();
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int hoverMin = Math.max(1, (int) Math.floor(this.movement.hoverMin()));
        int hoverMax = Math.max(hoverMin, (int) Math.ceil(this.movement.hoverMax()));
        int ceilingY = findCeilingY(level, x, z, groundY + hoverMax + 2);
        if (ceilingY != Integer.MAX_VALUE) {
            hoverMax = Math.min(hoverMax, Math.max(hoverMin, ceilingY - groundY - 2));
        }
        if (hoverMax < hoverMin) {
            return null;
        }

        int y = groundY + hoverMin;
        if (hoverMax > hoverMin) {
            y += this.mob.getRandom().nextInt(hoverMax - hoverMin + 1);
        }
        y = Mth.clamp(y, level.getMinBuildHeight() + 2, level.getMaxBuildHeight() - 2);
        BlockPos candidate = new BlockPos(x, y, z);
        if (!isAirPocket(level, candidate)) {
            return null;
        }
        return candidate;
    }

    private boolean isTargetStillValid(BlockPos candidate) {
        return candidate != null && isAirPocket(this.mob.level(), candidate);
    }

    private double distanceToAnchor(BlockPos anchor) {
        return this.mob.distanceToSqr(Vec3.atCenterOf(anchor));
    }

    private static int findCeilingY(Level level, int x, int z, int startY) {
        int maxY = Math.min(level.getMaxBuildHeight() - 2, startY + 12);
        for (int y = Math.max(level.getMinBuildHeight() + 2, startY); y <= maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir()) {
                return y;
            }
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isAirPocket(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
    }
}
