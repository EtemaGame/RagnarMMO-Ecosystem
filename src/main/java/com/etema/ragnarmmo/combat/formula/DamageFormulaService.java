package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.combat.element.ElementType;

import java.util.Random;

public final class DamageFormulaService {
    private static final double DEX_TO_ATK_DIVISOR = 5.0;
    private static final double STR_TO_RANGED_ATK_DIVISOR = 5.0;
    private static final double LUK_TO_ATK_DIVISOR = 5.0;
    private static final double MIN_DAMAGE_ROLL = 0.8;
    private static final double DEX_VARIANCE_DIVISOR = 150.0;
    private static final double LUK_VARIANCE_BONUS = 300.0;
    private static final double CRIT_BASE_MULT = 1.4;

    private DamageFormulaService() {
    }

    public static double statusAtk(int str, int dex, int luk, boolean ranged) {
        if (ranged) {
            return dex
                    + Math.pow(Math.floor(dex / 10.0), 2)
                    + Math.floor(str / STR_TO_RANGED_ATK_DIVISOR)
                    + Math.floor(luk / LUK_TO_ATK_DIVISOR);
        }
        return str
                + Math.pow(Math.floor(str / 10.0), 2)
                + Math.floor(dex / DEX_TO_ATK_DIVISOR)
                + Math.floor(luk / LUK_TO_ATK_DIVISOR);
    }

    public static double weaponAtk(double weaponBase) {
        return Math.max(0.0, weaponBase);
    }

    public static double meleeWeaponAtkRoll(double weaponAtk, int dex, int weaponLevel, boolean critical, Random rng) {
        double max = weaponAtk(weaponAtk);
        if (critical || max <= 0.0D) {
            return max;
        }
        int level = Math.max(1, Math.min(4, weaponLevel));
        double min = Math.min(dex * (0.8D + 0.2D * level), max);
        return min + safeRandom(rng) * (max - min);
    }

    public static double bowWeaponAtkRoll(double weaponAtk, double arrowAtk, int dex, int weaponLevel,
            boolean critical, Random rng) {
        double base = weaponAtk(weaponAtk);
        double arrow = Math.max(0.0D, arrowAtk);
        if (critical) {
            return base + arrow;
        }
        int level = Math.max(1, Math.min(4, weaponLevel));
        double stable = Math.min(base, dex * (0.8D + 0.2D * level));
        double scaled = base <= 0.0D ? 0.0D : base / 100.0D * stable;
        double min = Math.min(scaled, base);
        double max = Math.max(scaled, base);
        double weaponRoll = min + safeRandom(rng) * (max - min);
        double arrowRoll = arrow <= 0.0D ? 0.0D : Math.floor(safeRandom(rng) * arrow);
        return weaponRoll + arrowRoll;
    }

    public static int rangedDrawTicks(int baseDrawTicks, int agi) {
        double reduction = Math.min(0.9, agi / 100.0);
        return (int) Math.max(1, Math.round(baseDrawTicks * (1.0 - reduction)));
    }

    public static double totalAtk(int str, int dex, int luk, double weaponAtk, double bonusAtk, boolean ranged) {
        return statusAtk(str, dex, luk, ranged) + weaponAtk(weaponAtk) + bonusAtk;
    }

    public static double damageVariance(double baseDamage, int dex, int luk, Random rng) {
        double floor = damageVarianceFloorMultiplier(dex, luk);
        return baseDamage * (floor + rng.nextDouble() * (1.0 - floor));
    }

    public static double damageVarianceFloor(double baseDamage, int dex, int luk) {
        return baseDamage * damageVarianceFloorMultiplier(dex, luk);
    }

    public static double statusMatkMin(int intel) {
        return intel + Math.pow(Math.floor(intel / 7.0), 2);
    }

    public static double statusMatkMax(int intel) {
        return intel + Math.pow(Math.floor(intel / 5.0), 2);
    }

    public static double statusMatk(int intel) {
        return (statusMatkMin(intel) + statusMatkMax(intel)) * 0.5;
    }

    public static double totalMatk(int intel, double spellBase, double bonusMatk) {
        return statusMatk(intel) + spellBase + bonusMatk;
    }

    public static double critDamageMultiplier() {
        return CRIT_BASE_MULT;
    }

    public static double elementMultiplier(ElementType attack, ElementType defense) {
        return elementMultiplier(attack, defense, 1);
    }

    public static double elementMultiplier(ElementType attack, ElementType defense, int defenseLevel) {
        ElementType atk = attack == null ? ElementType.NEUTRAL : attack;
        ElementType def = defense == null ? ElementType.NEUTRAL : defense;
        int levelIndex = Math.max(1, Math.min(4, defenseLevel)) - 1;
        return PRE_RENEWAL_ELEMENT_PERCENT[levelIndex][atk.ordinal()][def.ordinal()] / 100.0D;
    }

    private static final int[][][] PRE_RENEWAL_ELEMENT_PERCENT = {
            {
                    {100, 100, 100, 100, 100, 100, 100, 100, 25, 100},
                    {100, 25, 100, 150, 50, 100, 75, 100, 100, 100},
                    {100, 100, 100, 50, 150, 100, 75, 100, 100, 100},
                    {100, 50, 150, 25, 100, 100, 75, 100, 100, 125},
                    {100, 175, 50, 100, 25, 100, 75, 100, 100, 100},
                    {100, 100, 125, 125, 125, 0, 75, 50, 100, -25},
                    {100, 100, 100, 100, 100, 100, 0, 125, 100, 150},
                    {100, 100, 100, 100, 100, 50, 125, 0, 100, -25},
                    {25, 100, 100, 100, 100, 100, 75, 75, 125, 100},
                    {100, 100, 100, 100, 100, 50, 100, 0, 100, 0}
            },
            {
                    {100, 100, 100, 100, 100, 100, 100, 100, 25, 100},
                    {100, 0, 100, 175, 25, 100, 50, 75, 100, 100},
                    {100, 100, 50, 25, 175, 100, 50, 75, 100, 100},
                    {100, 25, 175, 0, 100, 100, 50, 75, 100, 150},
                    {100, 175, 25, 100, 0, 100, 50, 75, 100, 100},
                    {100, 75, 125, 125, 125, 0, 50, 25, 75, -50},
                    {100, 100, 100, 100, 100, 100, -25, 150, 100, 175},
                    {100, 100, 100, 100, 100, 25, 150, -25, 100, -50},
                    {0, 75, 75, 75, 75, 75, 50, 50, 150, 125},
                    {100, 75, 75, 75, 75, 25, 125, 0, 100, 0}
            },
            {
                    {100, 100, 100, 100, 100, 100, 100, 100, 0, 100},
                    {100, -25, 100, 200, 0, 100, 25, 50, 100, 125},
                    {100, 100, 0, 0, 200, 100, 25, 50, 100, 75},
                    {100, 0, 200, -25, 100, 100, 25, 50, 100, 175},
                    {100, 200, 0, 100, -25, 100, 25, 50, 100, 100},
                    {100, 50, 100, 100, 100, 0, 25, 0, 50, -75},
                    {100, 100, 100, 100, 100, 125, -50, 175, 100, 200},
                    {100, 100, 100, 100, 100, 0, 175, -50, 100, -75},
                    {0, 50, 50, 50, 50, 50, 25, 25, 175, 150},
                    {100, 50, 50, 50, 50, 0, 150, 0, 100, 0}
            },
            {
                    {100, 100, 100, 100, 100, 100, 100, 100, 0, 100},
                    {100, -50, 100, 200, 0, 75, 0, 25, 100, 150},
                    {100, 100, -25, 0, 200, 75, 0, 25, 100, 50},
                    {100, 0, 200, -50, 100, 75, 0, 25, 100, 200},
                    {100, 200, 0, 100, -50, 75, 0, 25, 100, 100},
                    {100, 25, 75, 75, 75, 0, 0, -25, 25, -100},
                    {100, 75, 75, 75, 75, 125, -100, 200, 100, 200},
                    {100, 75, 75, 75, 75, -25, 200, -100, 100, -100},
                    {0, 25, 25, 25, 25, 25, 0, 0, 200, 175},
                    {100, 25, 25, 25, 25, -25, 175, 0, 100, 0}
            }
    };

    private static double damageVarianceFloorMultiplier(int dex, int luk) {
        double dexFactor = FormulaUtil.clamp(0, 1, dex / DEX_VARIANCE_DIVISOR);
        double lukBonus = LUK_VARIANCE_BONUS > 0 ? luk / LUK_VARIANCE_BONUS : 0;
        return FormulaUtil.clamp(MIN_DAMAGE_ROLL, 1.0, MIN_DAMAGE_ROLL + (1 - MIN_DAMAGE_ROLL) * (dexFactor + lukBonus));
    }

    private static double safeRandom(Random rng) {
        return rng == null ? 0.0D : rng.nextDouble();
    }
}
