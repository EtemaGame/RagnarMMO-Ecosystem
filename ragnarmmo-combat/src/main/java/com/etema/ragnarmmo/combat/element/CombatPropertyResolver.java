package com.etema.ragnarmmo.combat.element;

import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;

public final class CombatPropertyResolver {
    private CombatPropertyResolver() {
    }

    public static ElementType getDefensiveElement(LivingEntity entity) {
        return ElementType.NEUTRAL;
    }

    public static CombatMath.MobSize getEntitySize(LivingEntity entity) {
        if (entity == null) {
            return CombatMath.MobSize.MEDIUM;
        }
        float width = entity.getBbWidth();
        if (width < 0.7F) {
            return CombatMath.MobSize.SMALL;
        }
        if (width > 1.2F) {
            return CombatMath.MobSize.LARGE;
        }
        return CombatMath.MobSize.MEDIUM;
    }
}
