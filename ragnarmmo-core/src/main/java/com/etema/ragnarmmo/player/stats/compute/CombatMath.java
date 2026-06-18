package com.etema.ragnarmmo.player.stats.compute;

import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Compatibility layer for the original combat math surface.
 *
 * <p>This class intentionally lives in core so combat can depend on it without
 * creating a module cycle.</p>
 */
public final class CombatMath {
    public enum MobSize {
        SMALL,
        MEDIUM,
        LARGE
    }

    private CombatMath() {
    }

    public static double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }

    public static double soft(double value, double constant) {
        if (constant <= 0.0D) {
            return value;
        }
        return value / (1.0D + (Math.max(0.0D, value) / constant));
    }

    public static double getWeaponSizePenalty(ItemStack weapon, MobSize size) {
        if (weapon == null || weapon.isEmpty()) {
            return 1.0D;
        }

        Item item = weapon.getItem();
        boolean isDagger = weapon.getTags().anyMatch(t -> t.location().getPath().contains("daggers"));
        boolean isMace = weapon.getTags().anyMatch(t -> t.location().getPath().contains("maces"));
        boolean isStaff = weapon.getTags().anyMatch(t -> t.location().getPath().contains("staves"));
        boolean isWand = weapon.getTags().anyMatch(t -> t.location().getPath().contains("wands"));
        boolean isTwoHanded = weapon.getTags().anyMatch(t -> t.location().getPath().contains("two_handed"));
        boolean isSpear = item instanceof TridentItem
                || weapon.getTags().anyMatch(t -> t.location().getPath().contains("spears"));
        boolean isKatar = weapon.getTags().anyMatch(t -> t.location().getPath().contains("katars"));

        if (isDagger) {
            return switch (size) {
                case SMALL -> 1.0D;
                case MEDIUM -> 0.75D;
                case LARGE -> 0.5D;
            };
        }
        if (isSpear) {
            return switch (size) {
                case SMALL -> 0.75D;
                case MEDIUM -> 0.75D;
                case LARGE -> 1.0D;
            };
        }
        if (isMace || isStaff || isWand) {
            return switch (size) {
                case SMALL -> 0.75D;
                case MEDIUM -> 1.0D;
                case LARGE -> 1.0D;
            };
        }
        if (isKatar) {
            return switch (size) {
                case SMALL -> 0.75D;
                case MEDIUM -> 1.0D;
                case LARGE -> 0.75D;
            };
        }
        if (item instanceof SwordItem) {
            if (isTwoHanded) {
                return switch (size) {
                    case SMALL -> 0.75D;
                    case MEDIUM -> 0.75D;
                    case LARGE -> 1.0D;
                };
            }
            return switch (size) {
                case SMALL -> 0.75D;
                case MEDIUM -> 1.0D;
                case LARGE -> 0.75D;
            };
        }
        if (item instanceof AxeItem) {
            return switch (size) {
                case SMALL -> 0.5D;
                case MEDIUM -> 0.75D;
                case LARGE -> 1.0D;
            };
        }
        if (item instanceof BowItem || item instanceof CrossbowItem) {
            return switch (size) {
                case SMALL -> 1.0D;
                case MEDIUM -> 1.0D;
                case LARGE -> 0.75D;
            };
        }
        return 1.0D;
    }

    public static double computeStatusATK(int str, int dex, int luk, int level, boolean isRanged) {
        return isRanged
                ? dex + Math.pow(Math.floor(dex / 10.0D), 2.0D) + Math.floor(str / 5.0D) + Math.floor(luk / 5.0D)
                : str + Math.pow(Math.floor(str / 10.0D), 2.0D) + Math.floor(dex / 5.0D) + Math.floor(luk / 5.0D);
    }

    public static double computeWeaponATK(double weaponBase, int str, int dex, boolean isRanged) {
        return Math.max(0.0D, weaponBase);
    }

    public static int computeRangedDrawTicks(int baseDrawTicks, int agi) {
        return Math.max(0, baseDrawTicks - Math.max(0, agi / 10));
    }

    public static double computeTotalATK(int str, int dex, int luk, int level,
            double weaponATK, double bonusATK, boolean isRanged) {
        return computeStatusATK(str, dex, luk, level, isRanged) + Math.max(0.0D, weaponATK) + Math.max(0.0D, bonusATK);
    }

    public static double computeDamageVariance(double baseDamage, int dex, int luk, java.util.Random rng) {
        double dexFactor = clamp(0.0D, 1.0D, dex / 150.0D);
        double lukBonus = luk / 300.0D;
        double floor = clamp(0.8D, 1.0D, 0.8D + 0.2D * (dexFactor + lukBonus));
        double roll = 0.8D + (rng == null ? 0.0D : rng.nextDouble() * 0.2D);
        return baseDamage * Math.max(floor, roll);
    }

    public static double computeDamageVarianceFloor(double baseDamage, int dex, int luk) {
        double dexFactor = clamp(0.0D, 1.0D, dex / 150.0D);
        double lukBonus = luk / 300.0D;
        double floor = clamp(0.8D, 1.0D, 0.8D + 0.2D * (dexFactor + lukBonus));
        return baseDamage * floor;
    }

    public static double computeStatusMATKMin(int intel) {
        return intel + Math.pow(Math.floor(intel / 7.0D), 2.0D);
    }

    public static double computeStatusMATKMax(int intel) {
        return intel + Math.pow(Math.floor(intel / 5.0D), 2.0D);
    }

    public static double computeStatusMATK(int intel, int dex, int luk, int level) {
        return (computeStatusMATKMin(intel) + computeStatusMATKMax(intel)) * 0.5D;
    }

    public static double computeTotalMATK(int intel, int dex, int luk, int level, double spellBase, double bonusMATK) {
        return computeStatusMATK(intel, dex, luk, level) + Math.max(0.0D, spellBase) + Math.max(0.0D, bonusMATK);
    }

    public static double computeHIT(int dex, int luk, int level, double bonus) {
        return Math.max(0.0D, dex) + Math.max(0, level) + Math.max(0.0D, bonus);
    }

    public static double computeFLEE(int agi, int luk, int level, double bonus) {
        return Math.max(0.0D, agi) + Math.max(0, level) + Math.max(0.0D, bonus);
    }

    public static double computeHitRate(double attackerHit, double defenderFlee) {
        return RoPreRenewalFormulaService.hitRate(attackerHit, defenderFlee);
    }

    public static double computePerfectDodge(int luk) {
        return RoPreRenewalFormulaService.perfectDodge(luk);
    }

    public static double computeCritChance(int luk, int dex, double bonus) {
        return RoPreRenewalFormulaService.criticalChance(luk, bonus);
    }

    public static double computeCritDamageMultiplier(int luk, int str) {
        return 1.4D;
    }

    public static boolean isRangedWeapon(ItemStack weapon) {
        return weapon != null && !weapon.isEmpty() && (weapon.getItem() instanceof ProjectileWeaponItem);
    }

    public static int getWeaponBaseASPD(ItemStack weapon) {
        return isRangedWeapon(weapon) ? 145 : 156;
    }

    public static int computeASPD_RO(int baseWeaponASPD, boolean hasShield, int agi, int dex, double bonus) {
        return RoPreRenewalFormulaService.aspdRo(baseWeaponASPD, hasShield, agi, dex, bonus);
    }

    public static double convertASPD_ToAPS(int aspdRO) {
        return RoPreRenewalFormulaService.aspdToAttacksPerSecond(aspdRO);
    }

    public static double computeAPSForAttack(ItemStack mainHand, ItemStack offHand, boolean isOffHandAttack, int agi, int dex, double bonus) {
        ItemStack activeWeapon = isOffHandAttack ? offHand : mainHand;
        boolean hasShield = (!isOffHandAttack && offHand != null && offHand.getItem() instanceof ShieldItem);
        int aspdRo = computeASPD_RO(getWeaponBaseASPD(activeWeapon), hasShield, agi, dex, bonus);
        return convertASPD_ToAPS(aspdRo);
    }

    public static double computeAPS(ItemStack weapon, boolean hasShield, int agi, int dex, double bonus) {
        int aspdRo = computeASPD_RO(getWeaponBaseASPD(weapon), hasShield, agi, dex, bonus);
        return convertASPD_ToAPS(aspdRo);
    }

    public static double computeSoftDEF(int vit, int agi, int level) {
        return Math.max(0.0D, Math.floor(vit * 0.5D) + Math.max(Math.floor(vit * 0.3D), Math.floor((vit * vit) / 150.0D) - 1.0D));
    }

    public static double computeHardDEF(double armorDEF, int vit) {
        return Math.max(0.0D, armorDEF);
    }

    public static double computePhysDR(double hardDEF) {
        return Math.max(0.0D, Math.min(0.99D, hardDEF * 0.01D));
    }

    public static double applyPhysicalDefense(double rawDamage, double softDEF, double hardDEF, double drPhys) {
        return Math.max(0.0D, (rawDamage - softDEF) * (1.0D - drPhys));
    }

    public static double computeSoftMDEF(int intel, int vit) {
        return Math.max(0.0D, intel + Math.floor(vit / 2.0D));
    }

    public static double computeHardMDEF(double equipMDEF) {
        return Math.max(0.0D, equipMDEF);
    }

    public static double computeMDEF(int intel, int vit, int dex, int level, double equipMDEF) {
        return computeSoftMDEF(intel, vit) + computeHardMDEF(equipMDEF);
    }

    public static double computeMagicDR(double hardMDEF) {
        return Math.max(0.0D, Math.min(0.99D, hardMDEF * 0.01D));
    }

    public static double applyMagicDefense(double rawDamage, double softMDEF, double hardMDEF) {
        return Math.max(0.0D, rawDamage - softMDEF - hardMDEF);
    }

    public static double computeCastTime(double baseCast, int dex, int intStat, boolean useRenewalFormula) {
        return RoPreRenewalFormulaService.variableCastSeconds(baseCast, dex, 0.0D);
    }

    public static int computeCastDelay(int baseDelayTicks, Player player) {
        return Math.max(0, baseDelayTicks);
    }

    public static double computeMaxHP(int vit, int level, String jobId) {
        return Math.floor((35.0D + (level * 5.0D)) * (1.0D + vit / 100.0D));
    }

    public static double computeHPRegen(int vit, double maxHP) {
        return Math.min(1.0D + vit * 0.2D, maxHP * 0.02D);
    }

    public static double computeMaxSP(int intel, int level, String jobId) {
        return Math.floor((100.0D + ((level - 1.0D) * 3.0D)) * (1.0D + intel / 100.0D));
    }

    public static double computeSPRegen(int intel, double maxSP) {
        return Math.min(maxSP * (0.01D + intel * 0.002D), maxSP * 0.05D);
    }

    public static boolean rollCritical(double critChance, RandomSource rng) {
        return rng != null && rng.nextDouble() < critChance;
    }

    public static boolean rollHit(double hitRate, RandomSource rng) {
        return rng != null && rng.nextDouble() < hitRate;
    }

    public static boolean rollPerfectDodge(double perfectDodge, RandomSource rng) {
        return rng != null && rng.nextDouble() < perfectDodge;
    }

    public static TargetStats getTargetStats(LivingEntity entity) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player)) {
            return new TargetStats(1, 1, 1, 1, 1, 1, 0);
        }
        return new TargetStats(
                (int) Math.round(StatAttributes.getTotal(player, StatKeys.STR)),
                (int) Math.round(StatAttributes.getTotal(player, StatKeys.DEX)),
                (int) Math.round(StatAttributes.getTotal(player, StatKeys.VIT)),
                (int) Math.round(StatAttributes.getTotal(player, StatKeys.INT)),
                (int) Math.round(StatAttributes.getTotal(player, StatKeys.LUK)),
                (int) Math.round(StatAttributes.getTotal(player, StatKeys.AGI)),
                0);
    }

    public static OptionalInt tryGetTargetLevel(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return OptionalInt.of(Math.max(1, player.experienceLevel));
        }
        return OptionalInt.empty();
    }

    public static OptionalInt tryGetResolvedMobHit(LivingEntity entity) {
        return OptionalInt.empty();
    }

    public static OptionalInt tryGetResolvedMobFlee(LivingEntity entity) {
        return OptionalInt.empty();
    }

    public static OptionalDouble tryGetResolvedMobCritChance(LivingEntity entity) {
        return OptionalDouble.empty();
    }

    public static OptionalInt tryGetResolvedMobAspd(LivingEntity entity) {
        return OptionalInt.empty();
    }

    public static OptionalInt tryGetResolvedMobAttackIntervalTicks(LivingEntity entity) {
        return OptionalInt.empty();
    }

    public static float computeStunChance(float baseChance, LivingEntity target) {
        return baseChance;
    }

    public static int computeStunDuration(int baseDurationTicks, LivingEntity target) {
        return baseDurationTicks;
    }

    public static float computeSilenceChance(float baseChance, LivingEntity target) {
        return baseChance;
    }

    public static int computeSilenceDuration(int baseDurationTicks, LivingEntity target) {
        return baseDurationTicks;
    }

    public static float computeFrozenChance(float baseChance, LivingEntity target) {
        return baseChance;
    }

    public static float computeSleepChance(float baseChance, LivingEntity target) {
        return baseChance;
    }

    public static int computeSleepDuration(int baseDurationTicks, LivingEntity target) {
        return baseDurationTicks;
    }

    public static float computePoisonChance(float baseChance, LivingEntity target) {
        return baseChance;
    }

    public static int computePoisonDuration(int baseDurationTicks, LivingEntity target) {
        return baseDurationTicks;
    }

    public record TargetStats(int str, int dex, int vit, int intel, int luk, int agi, int mdef) {
    }
}
