package com.etema.ragnarmmo.entity.mob.goal;

import com.etema.ragnarmmo.common.api.mobs.data.RagnarMetamorphosis;
import com.etema.ragnarmmo.entity.mob.AbstractRagnarMobEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraftforge.registries.ForgeRegistries;

public final class RagnarMetamorphosisGoal extends Goal {
    private final AbstractRagnarMobEntity mob;
    private final RagnarMetamorphosis metamorphosis;
    private boolean forceNow;
    private boolean transformed;

    public RagnarMetamorphosisGoal(AbstractRagnarMobEntity mob, RagnarMetamorphosis metamorphosis) {
        this.mob = mob;
        this.metamorphosis = metamorphosis;
    }

    public void forceMetamorphosisNow() {
        this.forceNow = true;
    }

    @Override
    public boolean canUse() {
        if (this.transformed
                || this.mob == null
                || this.mob.level().isClientSide
                || !this.mob.isAlive()
                || this.mob.isRemoved()
                || this.mob.getTarget() != null) {
            return false;
        }

        if (this.metamorphosis.chancePerSecond() <= 0.0D) {
            return false;
        }

        if (this.forceNow) {
            return true;
        }

        return this.mob.tickCount % 20 == 0 && this.mob.getRandom().nextDouble() < this.metamorphosis.chancePerSecond();
    }

    @Override
    public void tick() {
        if (this.transformed) {
            return;
        }
        this.transformed = true;
        transform();
    }

    @Override
    public void stop() {
        this.forceNow = false;
    }

    private void transform() {
        ResourceLocation targetId = this.metamorphosis.target();
        var targetType = ForgeRegistries.ENTITY_TYPES.getValue(targetId);
        if (targetType == null) {
            return;
        }

        Entity created = targetType.create(this.mob.level());
        if (!(created instanceof Mob newMob)) {
            return;
        }

        newMob.moveTo(this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.mob.getYRot(), this.mob.getXRot());
        if (this.mob.hasCustomName()) {
            newMob.setCustomName(this.mob.getCustomName());
            newMob.setCustomNameVisible(this.mob.isCustomNameVisible());
        }
        if (this.mob.isPersistenceRequired()) {
            newMob.setPersistenceRequired();
        }
        if (this.mob.getMaxHealth() > 0.0F && newMob.getMaxHealth() > 0.0F) {
            float ratio = this.mob.getHealth() / this.mob.getMaxHealth();
            newMob.setHealth(Math.max(1.0F, Math.min(newMob.getMaxHealth(), newMob.getMaxHealth() * ratio)));
        }

        if (newMob instanceof AbstractRagnarMobEntity target) {
            this.mob.transferRobbedItemsTo(target);
        }

        this.mob.level().addFreshEntity(newMob);
        this.mob.markMetamorphosisRemoval();
        this.mob.discard();
    }
}
