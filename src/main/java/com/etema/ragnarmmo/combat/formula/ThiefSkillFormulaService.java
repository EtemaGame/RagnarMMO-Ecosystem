package com.etema.ragnarmmo.combat.formula;

public final class ThiefSkillFormulaService {
    private ThiefSkillFormulaService() {
    }

    public static double doubleAttackChance(int skillLevel) {
        return Math.max(0, skillLevel) * 0.05D;
    }

    public static int doubleAttackHitBonus(int skillLevel) {
        return Math.max(0, skillLevel);
    }

    public static int improveDodgeFleeBonus(int skillLevel) {
        return Math.max(0, skillLevel) * 3;
    }

    public static double stealBaseSuccessChance(int skillLevel) {
        return switch (Math.max(1, Math.min(10, skillLevel))) {
            case 1 -> 0.10D;
            case 2 -> 0.16D;
            case 3 -> 0.22D;
            case 4 -> 0.28D;
            case 5 -> 0.34D;
            case 6 -> 0.40D;
            case 7 -> 0.46D;
            case 8 -> 0.52D;
            case 9 -> 0.58D;
            default -> 0.64D;
        };
    }

    public static int hidingDurationTicks(int skillLevel) {
        return 20 * 30 * Math.max(1, skillLevel);
    }

    public static int hidingSpDrainIntervalTicks(int skillLevel) {
        return 20 * (4 + Math.max(1, skillLevel));
    }

    public static int envenomFlatBonus(int skillLevel) {
        return 15 * Math.max(1, skillLevel);
    }

    public static double envenomPoisonChance(int skillLevel) {
        return (10.0D + 4.0D * Math.max(1, skillLevel)) / 100.0D;
    }
}
