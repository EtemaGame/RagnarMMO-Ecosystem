package com.etema.ragnarmmo.combat.profile;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.contract.CombatStats;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.ArcherSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.DamageFormulaService;
import com.etema.ragnarmmo.combat.formula.AspdFormulaService;
import com.etema.ragnarmmo.combat.formula.WeaponAspdTableService;
import com.etema.ragnarmmo.combat.hand.AttackHandResolver;
import com.etema.ragnarmmo.items.runtime.RangedWeaponStatsHelper;
import com.etema.ragnarmmo.items.runtime.WeaponStatHelper;
import com.etema.ragnarmmo.combat.timing.AttackCadenceCalculator;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class HandAttackProfileResolver {
    private HandAttackProfileResolver() {
    }

    public static Optional<HandAttackProfile> resolve(ServerPlayer player, boolean offHand) {
        if (player == null) {
            return Optional.empty();
        }
        ItemStack weapon = offHand ? player.getOffhandItem() : player.getMainHandItem();
        int str = totalStat(player, StatKeys.STR);
        int dex = totalStat(player, StatKeys.DEX);
        int luk = totalStat(player, StatKeys.LUK);
        int agi = totalStat(player, StatKeys.AGI);
        int level = RagnarCoreAPI.get(player).map(stats -> Math.max(1, stats.getLevel())).orElse(1);
        int aspdRo = AspdFormulaService.aspdRo(WeaponAspdTableService.baseAspd(player, weapon),
                !offHand && player.getOffhandItem().getItem() instanceof net.minecraft.world.item.ShieldItem,
                agi,
                dex,
                offHand ? -8.0D : 0.0D);
        double hit = CombatMath.computeHIT(dex, luk, level, 0.0D);
        double crit = CombatMath.applyWeaponCriticalChanceModifier(
                CombatMath.computeCritChance(luk, dex, 0.0D),
                weapon);
        double critMult = CombatMath.computeCritDamageMultiplier(luk, str);
        boolean ranged = isRangedWeapon(weapon);
        double statusAttack = DamageFormulaService.statusAtk(str, dex, luk, ranged);
        double weaponAttack = weaponAttack(player, weapon);
        double arrowAttack = arrowAttack(weapon);
        double attack = statusAttack + weaponAttack + arrowAttack;
        return Optional.of(new HandAttackProfile(offHand, attack, hit, crit, critMult, aspdRo, weapon,
                statusAttack, weaponAttack, arrowAttack, weaponLevel(weapon), ranged));
    }

    private static double weaponAttack(ServerPlayer player, ItemStack weapon) {
        if (isRangedWeapon(weapon)) {
            return RangedWeaponStatsHelper.resolve(weapon)
                    .map(RangedWeaponStatsHelper.ResolvedRangedWeaponStats::weaponAtk)
                    .orElse(1.0D);
        }
        double configuredAttack = WeaponStatHelper.getConfiguredPhysicalAttackBase(weapon);
        if (configuredAttack > 0.0D) {
            return configuredAttack;
        }
        if (weapon == player.getMainHandItem()) {
            return Math.max(1.0D, player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
        double attack = 1.0D;
        for (var modifier : weapon.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION) {
                attack += modifier.getAmount();
            }
        }
        return Math.max(1.0D, attack);
    }

    private static double arrowAttack(ItemStack weapon) {
        return isRangedWeapon(weapon) ? 25.0D : 0.0D;
    }

    private static int weaponLevel(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty() || weapon.getTag() == null) {
            return 1;
        }
        var tag = weapon.getTag();
        if (tag.contains("ragnarmmo_weapon_level")) {
            return Math.max(1, Math.min(4, tag.getInt("ragnarmmo_weapon_level")));
        }
        if (tag.contains("ro_weapon_level")) {
            return Math.max(1, Math.min(4, tag.getInt("ro_weapon_level")));
        }
        return 1;
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && (stack.getItem() instanceof ProjectileWeaponItem
                || RangedWeaponStatsHelper.hasManualProfile(stack));
    }

    private static int totalStat(ServerPlayer player, StatKeys key) {
        return Math.max(1, (int) Math.round(StatAttributes.getTotal(player, key))
                + AcolyteSkillFormulaService.statusStatModifier(player, key)
                + ArcherSkillFormulaService.statusStatModifier(player, key));
    }
}
