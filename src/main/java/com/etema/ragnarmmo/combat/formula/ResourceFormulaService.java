package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.player.stats.compute.RoPreRenewalFormulaService;

public final class ResourceFormulaService {
    private static final double HP_REGEN_BASE = 1.0D;
    private static final double VIT_TO_HP_REGEN = 0.2D;
    private static final double HP_REGEN_MAX_PERCENT = 0.02D;
    private static final double MANA_REGEN_BASE_PERCENT = 0.01D;
    private static final double INT_TO_MANA_REGEN = 0.002D;
    private static final double MANA_REGEN_MAX_PERCENT = 0.05D;

    private ResourceFormulaService() {
    }

    public static double variableCastSeconds(double baseCast, int dex) {
        return RoPreRenewalFormulaService.variableCastSeconds(baseCast, dex, 0.0D);
    }

    public static double maxHp(int vit, int level, String jobId) {
        double hpBase = 35.0D + (level * 5.0D * hpJobMultiplier(jobId));
        return Math.floor(hpBase * (1.0D + vit / 100.0D));
    }

    public static double hpRegen(int vit, double maxHp) {
        double regen = HP_REGEN_BASE + vit * VIT_TO_HP_REGEN;
        return Math.min(regen, maxHp * HP_REGEN_MAX_PERCENT);
    }

    public static double maxSp(int intel, int level, String jobId) {
        double spBase = 100.0D + ((level - 1.0D) * 3.0D * spJobMultiplier(jobId));
        return Math.floor(spBase * (1.0D + intel / 100.0D));
    }

    public static double spRegen(int intel, double maxSp) {
        double regen = maxSp * (MANA_REGEN_BASE_PERCENT + intel * INT_TO_MANA_REGEN);
        return Math.min(regen, maxSp * MANA_REGEN_MAX_PERCENT);
    }

    private static double hpJobMultiplier(String jobId) {
        return switch (JobType.fromId(jobId)) {
            case SWORDSMAN -> 1.5D;
            case THIEF, MERCHANT -> 1.2D;
            case MAGE -> 0.8D;
            default -> 1.0D;
        };
    }

    private static double spJobMultiplier(String jobId) {
        return switch (JobType.fromId(jobId)) {
            case MAGE, ACOLYTE -> 1.5D;
            case ARCHER -> 1.2D;
            case THIEF -> 0.8D;
            case SWORDSMAN -> 0.7D;
            default -> 1.0D;
        };
    }
}
