package com.etema.ragnarmmo.items.runtime;

import com.etema.ragnarmmo.common.api.attributes.RagnarAttributes;
import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import com.etema.ragnarmmo.player.stats.compute.RoPreRenewalFormulaService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ToolActions;

public final class ItemDerivedStatsContributor {
    private static final int DEFAULT_WEAPON_ASPD = 156;
    private static final EquipmentSlot[] ARMOR_DEFENSE_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private ItemDerivedStatsContributor() {
    }

    public static void register() {
        DerivedStatsService.registerContributor(ItemDerivedStatsContributor::contribute);
    }

    public static void contribute(ServerPlayer player, IPlayerStats stats, DerivedStats derived) {
        if (player == null || stats == null || derived == null) {
            return;
        }

        EquipmentStats equipment = EquipmentStats.capture(player);
        applyOffense(derived, equipment);
        applyDefense(derived, equipment);
        applyTiming(player, derived, equipment);
    }

    private static void applyOffense(DerivedStats derived, EquipmentStats equipment) {
        if (equipment.weaponAttack() > 0.0D) {
            derived.physicalAttack += equipment.weaponAttack();
            derived.physicalAttackMin += equipment.weaponAttack();
            derived.physicalAttackMax += equipment.weaponAttack();
        }

        if (equipment.weaponMagicAttack() > 0.0D) {
            derived.magicAttackMin += equipment.weaponMagicAttack();
            derived.magicAttackMax += equipment.weaponMagicAttack();
            derived.magicAttack = (derived.magicAttackMin + derived.magicAttackMax) * 0.5D;
        }
    }

    private static void applyDefense(DerivedStats derived, EquipmentStats equipment) {
        if (equipment.armorHardDefense() > 0.0D) {
            derived.hardDefense += equipment.armorHardDefense();
            derived.defense = derived.softDefense + derived.hardDefense;
            derived.physicalDamageReduction = clamp(0.0D, 0.99D, derived.hardDefense * 0.01D);
        }

        if (equipment.armorHardMagicDefense() > 0.0D) {
            derived.hardMagicDefense += equipment.armorHardMagicDefense();
            derived.magicDefense = derived.softMagicDefense + derived.hardMagicDefense;
            derived.magicDamageReduction = clamp(0.0D, 0.99D, derived.hardMagicDefense / 100.0D);
        }
    }

    private static void applyTiming(ServerPlayer player, DerivedStats derived, EquipmentStats equipment) {
        if (equipment.weaponBaseAspd() <= 0 && !equipment.hasShield()) {
            return;
        }

        int agi = totalStat(player, StatKeys.AGI);
        int dex = totalStat(player, StatKeys.DEX);
        int baseAspd = equipment.weaponBaseAspd() > 0 ? equipment.weaponBaseAspd() : DEFAULT_WEAPON_ASPD;
        int aspdRo = RoPreRenewalFormulaService.aspdRo(baseAspd, equipment.hasShield(), agi, dex, 0.0D);
        double attacksPerSecond = RoPreRenewalFormulaService.aspdToAttacksPerSecond(aspdRo);
        derived.attackSpeed = aspdRo;
        derived.globalCooldown = attacksPerSecond > 0.0D ? 1.0D / attacksPerSecond : 0.0D;
    }

    private static int totalStat(ServerPlayer player, StatKeys key) {
        double total = StatAttributes.getTotal(player, key);
        return total > 0.0D ? (int) Math.round(total) : 1;
    }

    private static double resolveWeaponAttack(ItemStack main) {
        if (main == null || main.isEmpty()) {
            return 0.0D;
        }

        double configuredAttack = WeaponStatHelper.getConfiguredPhysicalAttackBase(main);
        float enchantDamage = EnchantmentHelper.getDamageBonus(main, MobType.UNDEFINED);
        if (configuredAttack > 0.0D) {
            return Math.max(0.0D, configuredAttack + enchantDamage);
        }

        return Math.max(1.0D, 1.0D + enchantDamage);
    }

    private static double computeArmorHardDefense(ServerPlayer player) {
        double hardDefense = 0.0D;
        for (EquipmentSlot slot : ARMOR_DEFENSE_SLOTS) {
            hardDefense += armorHardDefense(player.getItemBySlot(slot), slot);
        }

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (hasShield(player)) {
            hardDefense += armorHardDefense(offhand, EquipmentSlot.OFFHAND);
        }
        return Math.max(0.0D, hardDefense);
    }

    static double armorHardDefense(ItemStack stack, EquipmentSlot slot) {
        if (stack == null || stack.isEmpty() || slot == null) {
            return 0.0D;
        }

        return armorHardDefenseFromModifiers(stack.getAttributeModifiers(slot).get(Attributes.ARMOR));
    }

    static double armorHardDefenseModifierValue(AttributeModifier modifier) {
        if (modifier == null) {
            return 0.0D;
        }
        if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
            return Math.max(0.0D, modifier.getAmount());
        }
        return 0.0D;
    }

    static double armorHardDefenseFromModifiers(Iterable<AttributeModifier> modifiers) {
        if (modifiers == null) {
            return 0.0D;
        }
        double hardDefense = 0.0D;
        for (AttributeModifier modifier : modifiers) {
            hardDefense += armorHardDefenseModifierValue(modifier);
        }
        return Math.max(0.0D, hardDefense);
    }

    private static double computeArmorHardMagicDefense(ServerPlayer player) {
        double hardMagicDefense = 0.0D;
        var magicDefense = player.getAttribute(RagnarAttributes.MAGIC_DEFENSE.get());
        if (magicDefense != null) {
            hardMagicDefense += magicDefense.getValue();
        }
        return hardMagicDefense;
    }

    private static boolean hasShield(ServerPlayer player) {
        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        return !offhand.isEmpty()
                && (offhand.canPerformAction(ToolActions.SHIELD_BLOCK)
                        || offhand.getItem() instanceof net.minecraft.world.item.ShieldItem);
    }

    private static double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }

    private record EquipmentStats(
            double weaponAttack,
            double weaponMagicAttack,
            int weaponBaseAspd,
            double armorHardDefense,
            double armorHardMagicDefense,
            boolean hasShield
    ) {
        private static EquipmentStats capture(ServerPlayer player) {
            ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
            return new EquipmentStats(
                    resolveWeaponAttack(main),
                    WeaponStatHelper.getDisplayedMagicAttack(main),
                    WeaponStatHelper.getConfiguredAspd(main),
                    computeArmorHardDefense(player),
                    computeArmorHardMagicDefense(player),
                    ItemDerivedStatsContributor.hasShield(player));
        }
    }
}
