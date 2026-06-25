package com.etema.ragnarmmo.combat.formula;

public final class MerchantSkillFormulaService {
    private static final double[] TRADE_RATES = {
            0.0D,
            0.07D, 0.09D, 0.11D, 0.13D, 0.15D,
            0.17D, 0.19D, 0.21D, 0.23D, 0.24D
    };

    private MerchantSkillFormulaService() {
    }

    public static int enlargeWeightLimitBonus(int skillLevel) {
        return 200 * clampLevel(skillLevel);
    }

    public static double tradeRate(int skillLevel) {
        return TRADE_RATES[clampLevel(skillLevel)];
    }

    public static int discountedBuyPrice(int basePrice, int skillLevel) {
        return Math.max(1, (int) Math.floor(Math.max(0, basePrice) * (1.0D - tradeRate(skillLevel))));
    }

    public static int overchargedSellPrice(int basePrice, int skillLevel) {
        return Math.max(0, (int) Math.floor(Math.max(0, basePrice) * (1.0D + tradeRate(skillLevel))));
    }

    public static int pushcartMovementSpeedPercent(int skillLevel) {
        int level = clampLevel(skillLevel);
        return level <= 0 ? 0 : 50 + 5 * level;
    }

    public static int pushcartWeightCapacity() {
        return 8000;
    }

    public static int pushcartDistinctSlots() {
        return 100;
    }

    public static int vendingMaxStacks(int skillLevel) {
        return 2 + clampLevel(skillLevel);
    }

    public static int mammoniteDamagePercent(int skillLevel) {
        return 100 + 50 * clampLevel(skillLevel);
    }

    public static int mammoniteZenyCost(int skillLevel) {
        return 100 * clampLevel(skillLevel);
    }

    private static int clampLevel(int skillLevel) {
        return Math.max(0, Math.min(10, skillLevel));
    }
}
