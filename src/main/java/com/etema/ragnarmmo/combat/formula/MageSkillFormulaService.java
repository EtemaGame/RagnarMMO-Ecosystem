package com.etema.ragnarmmo.combat.formula;

public final class MageSkillFormulaService {
    private MageSkillFormulaService() {
    }

    public static double increaseSpRecoveryPerSecond(int skillLevel, double maxSp) {
        int level = clampLevel(skillLevel);
        if (level <= 0) {
            return 0.0D;
        }
        return (3.0D * level + Math.max(0.0D, maxSp) * (0.002D * level)) / 10.0D;
    }

    public static double spItemMultiplier(int skillLevel) {
        int level = clampLevel(skillLevel);
        return 1.0D + 0.02D * level;
    }

    public static double napalmBeatRatio(int skillLevel) {
        return (70.0D + 10.0D * clampLevel(skillLevel)) / 100.0D;
    }

    public static int soulStrikeHits(int skillLevel) {
        return Math.max(1, (clampLevel(skillLevel) + 1) / 2);
    }

    public static double soulStrikeUndeadMultiplier(int skillLevel) {
        return 1.0D + 0.05D * clampLevel(skillLevel);
    }

    public static int safetyWallHits(int skillLevel) {
        return clampLevel(skillLevel) + 1;
    }

    public static int safetyWallDurationTicks(int skillLevel) {
        return 20 * 5 * clampLevel(skillLevel);
    }

    public static double boltRatio() {
        return 1.0D;
    }

    public static int boltHits(int skillLevel) {
        return clampLevel(skillLevel);
    }

    public static double frostDiverRatio(int skillLevel) {
        return (100.0D + 10.0D * clampLevel(skillLevel)) / 100.0D;
    }

    public static double frostDiverFreezeChance(int skillLevel) {
        return (35.0D + 3.0D * clampLevel(skillLevel)) / 100.0D;
    }

    public static int frostDiverDurationTicks(int skillLevel) {
        return 20 * 3 * clampLevel(skillLevel);
    }

    public static double stoneCurseBaseChance(int skillLevel) {
        return (20.0D + 4.0D * clampLevel(skillLevel)) / 100.0D;
    }

    public static boolean stoneCurseConsumesGemOnCast(int skillLevel) {
        return clampLevel(skillLevel) <= 5;
    }

    public static double fireBallRatio(int skillLevel) {
        return (70.0D + 10.0D * clampLevel(skillLevel)) / 100.0D;
    }

    public static double fireWallRatioPerHit() {
        return 0.5D;
    }

    public static int fireWallHitsPerCell(int skillLevel) {
        return 4 + clampLevel(skillLevel);
    }

    public static int fireWallDurationTicks(int skillLevel) {
        return 20 * (4 + clampLevel(skillLevel));
    }

    public static double thunderStormRatioPerHit() {
        return 0.8D;
    }

    public static int thunderStormHits(int skillLevel) {
        return clampLevel(skillLevel);
    }

    private static int clampLevel(int skillLevel) {
        return Math.max(0, Math.min(10, skillLevel));
    }
}
