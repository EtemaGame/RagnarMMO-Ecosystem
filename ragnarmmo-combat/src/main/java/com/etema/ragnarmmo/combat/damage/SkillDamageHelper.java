package com.etema.ragnarmmo.combat.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class SkillDamageHelper {
    private SkillDamageHelper() {
    }

    public static boolean dealSkillDamage(LivingEntity target, DamageSource source, float amount) {
        if (target == null || !target.isAlive() || amount <= 0.0F) {
            return false;
        }
        int savedHurtTime = target.hurtTime;
        int savedHurtDuration = target.hurtDuration;
        int savedInvulnerableTime = target.invulnerableTime;
        target.hurtTime = 0;
        target.hurtDuration = 0;
        target.invulnerableTime = 0;
        boolean hit = target.hurt(source, amount);
        if (target.isAlive()) {
            target.hurtTime = Math.max(target.hurtTime, savedHurtTime);
            target.hurtDuration = Math.max(target.hurtDuration, savedHurtDuration);
            if (savedInvulnerableTime <= 0) {
                target.invulnerableTime = 0;
            }
        }
        return hit;
    }
}
