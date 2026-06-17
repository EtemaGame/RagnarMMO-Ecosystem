package com.etema.ragnarmmo.entity.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class CreamyFearEntity extends AbstractRagnarMobEntity {
    public CreamyFearEntity(EntityType<? extends CreamyFearEntity> type, Level level) {
        super(type, level);
        this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }
}
