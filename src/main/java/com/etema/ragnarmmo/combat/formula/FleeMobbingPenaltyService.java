package com.etema.ragnarmmo.combat.formula;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class FleeMobbingPenaltyService {
    private static final double SEARCH_RADIUS = 12.0D;

    private FleeMobbingPenaltyService() {
    }

    public static double applyMonsterMobbingPenalty(Player target, double flee) {
        if (target == null || target.level().isClientSide || flee <= 0.0D) {
            return Math.max(0.0D, flee);
        }
        int attackers = countMonsterAttackers(target);
        if (attackers <= 2) {
            return Math.max(0.0D, flee);
        }
        double multiplier = Math.max(0.0D, 1.0D - ((attackers - 2) * 0.10D));
        return Math.max(0.0D, flee * multiplier);
    }

    private static int countMonsterAttackers(Player target) {
        AABB area = target.getBoundingBox().inflate(SEARCH_RADIUS);
        return target.level().getEntitiesOfClass(Mob.class, area,
                mob -> mob.isAlive() && mob.getTarget() == target)
                .size();
    }
}
