package com.etema.ragnarmmo.items.runtime;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import com.etema.ragnarmmo.items.data.RoCombatProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class RangedWeaponStatsHelper {
    public static final String SNAPSHOT_TAG = "ragnarmmo_snapshot";
    public static final int SNAPSHOT_VERSION = 1;
    public static final String DAMAGE_MODE_DEFAULT = "default";
    public static final String DAMAGE_MODE_ATK_OVERRIDE = "atk_override";
    public static final String SKILL_DAMAGE_MULTIPLIER_TAG = "ragnarmmo_skill_damage_multiplier";
    public static final String FORCED_DRAW_RATIO_TAG = "ragnarmmo_forced_draw_ratio";

    private static final double DEFAULT_BOW_ATK = 15.0D;
    private static final int DEFAULT_BOW_DRAW_TICKS = 20;
    private static final float DEFAULT_BOW_VELOCITY = 1.0F;

    private static final double DEFAULT_CROSSBOW_ATK = 22.0D;
    private static final int DEFAULT_CROSSBOW_DRAW_TICKS = 25;
    private static final float DEFAULT_CROSSBOW_VELOCITY = 1.0F;

    private RangedWeaponStatsHelper() {
    }

    public static Optional<ResolvedRangedWeaponStats> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        if (stack.getItem() instanceof RagnarRangedWeaponStats rangedStats) {
            return Optional.of(new ResolvedRangedWeaponStats(
                    rangedStats.getRangedWeaponAtk(stack) + RoRefineMath.getAttackBonus(stack),
                    rangedStats.getBaseRangedAspd(stack),
                    rangedStats.getBaseDrawTicks(stack),
                    rangedStats.getProjectileVelocity(stack)));
        }

        RoCombatProfile profile = RoItemRuleResolver.resolve(stack).combatProfile();
        if (profile.isRanged()) {
            double atk = WeaponStatHelper.getConfiguredPhysicalAttackBase(stack);
            if (atk <= 0.0D) {
                atk = DEFAULT_BOW_ATK;
            }
            int aspd = profile.aspd() > 0 ? profile.aspd() : com.etema.ragnarmmo.player.stats.compute.CombatMath.getWeaponBaseASPD(stack);
            int drawTicks = profile.drawTicks() > 0 ? profile.drawTicks() : DEFAULT_BOW_DRAW_TICKS;
            float velocity = profile.projectileVelocity() > 0.0F ? profile.projectileVelocity() : DEFAULT_BOW_VELOCITY;
            return Optional.of(new ResolvedRangedWeaponStats(atk + RoRefineMath.getAttackBonus(stack), aspd, drawTicks, velocity));
        }

        if (stack.getItem() instanceof BowItem) {
            return Optional.of(new ResolvedRangedWeaponStats(
                    DEFAULT_BOW_ATK + RoRefineMath.getAttackBonus(stack),
                    com.etema.ragnarmmo.player.stats.compute.CombatMath.getWeaponBaseASPD(stack),
                    DEFAULT_BOW_DRAW_TICKS,
                    DEFAULT_BOW_VELOCITY));
        }

        if (stack.getItem() instanceof CrossbowItem) {
            return Optional.of(new ResolvedRangedWeaponStats(
                    DEFAULT_CROSSBOW_ATK + RoRefineMath.getAttackBonus(stack),
                    com.etema.ragnarmmo.player.stats.compute.CombatMath.getWeaponBaseASPD(stack),
                    DEFAULT_CROSSBOW_DRAW_TICKS,
                    DEFAULT_CROSSBOW_VELOCITY));
        }

        return Optional.empty();
    }

    public static boolean supports(ItemStack stack) {
        return resolve(stack).isPresent();
    }

    public static boolean hasManualProfile(ItemStack stack) {
        return stack != null && !stack.isEmpty() && RoItemRuleResolver.resolve(stack).combatProfile().isRanged();
    }

    public static float estimateDrawRatio(AbstractArrow arrow, ItemStack weapon) {
        if (arrow == null) {
            return 1.0F;
        }
        if (arrow.getPersistentData().contains(FORCED_DRAW_RATIO_TAG)) {
            return Mth.clamp((float) arrow.getPersistentData().getDouble(FORCED_DRAW_RATIO_TAG), 0.1F, 1.0F);
        }
        if (weapon.getItem() instanceof CrossbowItem) {
            return 1.0F;
        }

        double speed = arrow.getDeltaMovement().length();
        double expectedFullSpeed = resolve(weapon).map(stats -> 3.0D * Math.max(0.01F, stats.projectileVelocity())).orElse(3.0D);
        if (expectedFullSpeed <= 0.0D) {
            return 1.0F;
        }
        return Mth.clamp((float) (speed / expectedFullSpeed), 0.1F, 1.0F);
    }

    public static void snapshotArrow(AbstractArrow arrow, Player player, ItemStack weapon, float drawRatio) {
        if (arrow == null || player == null || weapon == null || weapon.isEmpty()) {
            return;
        }
        Optional<ResolvedRangedWeaponStats> resolvedOpt = resolve(weapon);
        if (resolvedOpt.isEmpty()) {
            return;
        }

        IPlayerStats stats = RagnarCoreAPI.get(player).orElse(null);
        DerivedStats derived = stats != null
                ? DerivedStatsService.compute((net.minecraft.server.level.ServerPlayer) player, stats).orElseGet(DerivedStats::new)
                : new DerivedStats();

        ResolvedRangedWeaponStats resolved = resolvedOpt.get();
        CompoundTag snapshot = new CompoundTag();
        snapshot.putInt("version", SNAPSHOT_VERSION);
        snapshot.putString("family", "bow");
        snapshot.putDouble("atk", resolved.weaponAtk());
        snapshot.putInt("dex", (int) StatAttributes.getTotal(player, StatKeys.DEX));
        snapshot.putInt("luk", (int) StatAttributes.getTotal(player, StatKeys.LUK));
        snapshot.putDouble("crit_chance", derived.criticalChance);
        snapshot.putDouble("crit_damage", derived.criticalDamageMultiplier);
        snapshot.putDouble("draw_ratio", Mth.clamp(drawRatio, 0.1F, 1.0F));
        snapshot.putDouble("skill_damage_multiplier", arrow.getPersistentData().getDouble(SKILL_DAMAGE_MULTIPLIER_TAG));
        snapshot.putBoolean("bypass_iframes", false);
        snapshot.putString("damage_mode", DAMAGE_MODE_DEFAULT);
        snapshot.putString("element", "neutral");
        snapshot.putUUID("shooter_uuid", player.getUUID());
        arrow.getPersistentData().put(SNAPSHOT_TAG, snapshot);
    }

    public record ResolvedRangedWeaponStats(double weaponAtk, int baseAspd, int drawTicks, float projectileVelocity) {
    }
}
