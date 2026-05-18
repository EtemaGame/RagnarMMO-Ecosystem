package com.etema.ragnarmmo.entity.mob.goal;

import com.etema.ragnarmmo.common.api.mobs.data.RagnarLootBehavior;
import com.etema.ragnarmmo.entity.mob.AbstractRagnarMobEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class RagnarLooterGoal extends Goal {
    private final AbstractRagnarMobEntity mob;
    private final RagnarLootBehavior behavior;
    private ItemEntity targetItem;

    public RagnarLooterGoal(AbstractRagnarMobEntity mob, RagnarLootBehavior behavior) {
        this.mob = mob;
        this.behavior = behavior == null ? RagnarLootBehavior.DEFAULT : behavior;
    }

    @Override
    public boolean canUse() {
        if (this.mob == null
                || !this.mob.isAlive()
                || this.mob.getTarget() != null
                || !this.mob.isEffectiveAi()
                || !this.mob.canMove()
                || this.behavior.pickupRadius() <= 0.0D
                || !this.mob.getNavigation().isDone()) {
            return false;
        }

        this.targetItem = this.mob.level().getEntitiesOfClass(
                ItemEntity.class,
                this.mob.getBoundingBox().inflate(this.behavior.pickupRadius()),
                this::isEligibleItem).stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(this.mob), right.distanceToSqr(this.mob)))
                .orElse(null);
        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null
                && this.targetItem.isAlive()
                && this.mob.getTarget() == null
                && this.mob.distanceToSqr(this.targetItem) <= this.behavior.pickupRadius() * this.behavior.pickupRadius() * 4.0D;
    }

    @Override
    public void start() {
        if (this.targetItem != null) {
            this.mob.getNavigation().moveTo(this.targetItem, 1.0D);
        }
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) {
            return;
        }

        this.mob.getNavigation().moveTo(this.targetItem, 1.0D);
        double maxDistance = 1.5D;
        if (this.mob.distanceToSqr(this.targetItem) <= maxDistance * maxDistance) {
            ItemStack stack = this.targetItem.getItem();
            if (!stack.isEmpty()) {
                this.mob.addRobbedItem(stack);
                this.targetItem.discard();
            }
            this.targetItem = null;
        }
    }

    @Override
    public void stop() {
        this.targetItem = null;
    }

    private boolean isEligibleItem(ItemEntity itemEntity) {
        if (itemEntity == null || !itemEntity.isAlive() || itemEntity.isRemoved()) {
            return false;
        }
        if (itemEntity.hasPickUpDelay()) {
            return false;
        }
        ItemStack stack = itemEntity.getItem();
        return !stack.isEmpty() && stack.getCount() > 0;
    }
}
