package com.etema.ragnarmmo.combat.contract;

public record MagicAttackProfile(
        double minMagicAttack,
        double maxMagicAttack) {
    public double averageMagicAttack() {
        return Math.max(0.0D, (minMagicAttack + maxMagicAttack) * 0.5D);
    }

    public double rollMagicAttack(java.util.Random rng) {
        double min = Math.max(0.0D, Math.min(minMagicAttack, maxMagicAttack));
        double max = Math.max(min, Math.max(minMagicAttack, maxMagicAttack));
        if (rng == null || max <= min) {
            return min;
        }
        return min + rng.nextDouble() * (max - min);
    }
}
