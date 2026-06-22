package com.etema.ragnarmmo.combat.formula;

import java.util.Random;

public final class DefenseFormulaService {
    private static final double HARD_DEF_REDUCTION_MULT = 0.01;
    private static final double DR_PHYS_MAX = 0.99;
    private static final double DR_MAGIC_MAX = 0.99;

    private DefenseFormulaService() {
    }

    public static double softDef(int vit) {
        double vitComponent = Math.floor(vit * 0.5);
        double scalingComponent = Math.max(Math.floor(vit * 0.3), Math.floor((vit * vit) / 150.0) - 1.0);
        return Math.max(0.0, vitComponent + scalingComponent);
    }

    public static double playerSoftDefRoll(int vit, Random rng) {
        int safeVit = Math.max(0, vit);
        double base = Math.floor(safeVit * 0.5D);
        int min = (int) Math.floor(safeVit * 0.3D);
        int max = (int) Math.max(min, Math.floor((safeVit * safeVit) / 150.0D) - 1.0D);
        return base + randomIntInclusive(min, max, rng);
    }

    public static double mobSoftDefRoll(int vit, Random rng) {
        int safeVit = Math.max(0, vit);
        int max = (int) Math.max(0.0D, Math.pow(Math.floor(safeVit / 20.0D), 2.0D) - 1.0D);
        return safeVit + randomIntInclusive(0, max, rng);
    }

    public static double hardDef(double armorDef) {
        return armorDef;
    }

    public static double physicalDamageReduction(double hardDef) {
        return FormulaUtil.clamp(0, DR_PHYS_MAX, hardDef * HARD_DEF_REDUCTION_MULT);
    }

    public static double applyPhysicalDefense(double rawDamage, double softDef, double drPhys) {
        double afterHard = rawDamage * (1.0 - drPhys);
        return Math.max(1.0, afterHard - softDef);
    }

    public static double applyPhysicalDefense(double rawDamage, double softDef, double hardDef, boolean critical) {
        if (critical) {
            return Math.max(1.0D, rawDamage);
        }
        return applyPhysicalDefense(rawDamage, softDef, physicalDamageReduction(hardDef));
    }

    public static double applyPreRenewalMobPhysicalDefense(double rawDamage, double hardDef, double softDef,
            double bonusReduction) {
        double reduction = FormulaUtil.clamp(0.0D, DR_PHYS_MAX, physicalDamageReduction(hardDef) + Math.max(0.0D, bonusReduction));
        double afterHard = Math.max(0.0D, rawDamage) * (1.0D - reduction);
        return Math.max(1.0D, afterHard - Math.max(0.0D, softDef));
    }

    public static double softMdef(int intel, int vit) {
        return Math.max(0.0, intel + Math.floor(vit / 2.0));
    }

    public static double hardMdef(double equipMdef) {
        return Math.max(0.0, equipMdef);
    }

    public static double magicDamageReduction(double hardMdef) {
        return FormulaUtil.clamp(0.0, DR_MAGIC_MAX, hardMdef / 100.0);
    }

    public static double applyMagicDefense(double rawDamage, double softMdef, double hardMdef) {
        double afterHard = rawDamage * (1.0 - magicDamageReduction(hardMdef));
        return Math.max(1.0, afterHard - softMdef);
    }

    private static int randomIntInclusive(int min, int max, Random rng) {
        if (max <= min) {
            return min;
        }
        return min + (rng == null ? 0 : rng.nextInt(max - min + 1));
    }
}
