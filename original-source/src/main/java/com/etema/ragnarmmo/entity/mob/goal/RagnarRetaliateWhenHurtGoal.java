package com.etema.ragnarmmo.entity.mob.goal;

import com.etema.ragnarmmo.entity.mob.AbstractRagnarMobEntity;
import com.etema.ragnarmmo.entity.mob.RagnarRetaliateMemory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public final class RagnarRetaliateWhenHurtGoal extends Goal {
    private final AbstractRagnarMobEntity mob;

    public RagnarRetaliateWhenHurtGoal(AbstractRagnarMobEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (this.mob == null || !this.mob.isAlive() || !this.mob.isEffectiveAi() || !this.mob.canAttack()) {
            return false;
        }

        LivingEntity attacker = RagnarRetaliateMemory.resolve(this.mob);
        if (attacker == null) {
            attacker = this.mob.getLastHurtByMob();
        }
        return isValidTarget(attacker);
    }

    @Override
    public void start() {
        LivingEntity attacker = RagnarRetaliateMemory.resolve(this.mob);
        if (attacker == null) {
            attacker = this.mob.getLastHurtByMob();
        }
        if (isValidTarget(attacker)) {
            this.mob.setTarget(attacker);
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    private boolean isValidTarget(LivingEntity attacker) {
        return attacker != null
                && attacker.isAlive()
                && attacker != this.mob
                && !(attacker instanceof net.minecraft.world.entity.player.Player player && (player.isSpectator() || player.isCreative()));
    }
}
