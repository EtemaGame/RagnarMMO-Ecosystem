package com.etema.ragnarmmo.combat.engine;

import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import net.minecraft.util.RandomSource;

public class RagnarHitCalculator {
    public double hitRate(double attackerHit, double defenderFlee) {
        return Math.max(0.05D, Math.min(0.95D, (80.0D + attackerHit - defenderFlee) / 100.0D));
    }

    public boolean rollPerfectDodge(double perfectDodge, RandomSource rng) {
        return rng.nextDouble() < Math.max(0.0D, perfectDodge);
    }

    public CombatHitResultType rollHitWithCrit(double attackerHit, double defenderFlee, double critChance,
            double critShield, RandomSource rng) {
        double effectiveCritChance = Math.max(0.0D, critChance - Math.max(0.0D, critShield) / 100.0D);
        if (rng.nextDouble() < effectiveCritChance) {
            return CombatHitResultType.CRIT;
        }
        return rng.nextDouble() < hitRate(attackerHit, defenderFlee) ? CombatHitResultType.HIT : CombatHitResultType.MISS;
    }
}
