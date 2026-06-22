package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public final class CombatPropertyModifierService {
    private static final String CARD_MODIFIERS = "card_modifiers";
    private static final String RO_COMPOUNDED_CARD_MODIFIERS = "RoCompoundedCardModifiers";

    private CombatPropertyModifierService() {
    }

    public static double outgoingDamageMultiplier(Player attacker, String targetRace, ElementType targetElement,
            CombatMath.MobSize targetSize) {
        if (attacker == null) {
            return 1.0D;
        }
        DamageBonus bonus = DamageBonus.ZERO;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR && slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
                continue;
            }
            bonus = bonus.plus(outgoingBonus(attacker.getItemBySlot(slot), targetRace, targetElement, targetSize));
        }
        return bonus.multiplier();
    }

    public static double incomingElementReduction(Player defender, ElementType attackElement) {
        if (defender == null || attackElement == null) {
            return 0.0D;
        }
        double reduction = 0.0D;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR && slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
                continue;
            }
            reduction += modifierValue(defender.getItemBySlot(slot), key("resist_element", attackElement.name()));
            reduction += modifierValue(defender.getItemBySlot(slot), "ragnarmmo:resist_all_elements");
        }
        return FormulaUtil.clamp(0.0D, 0.95D, reduction);
    }

    private static DamageBonus outgoingBonus(ItemStack stack, String race, ElementType element, CombatMath.MobSize size) {
        if (stack == null || stack.isEmpty()) {
            return DamageBonus.ZERO;
        }
        return new DamageBonus(
                modifierValue(stack, key("damage_size", size == null ? "medium" : size.name())),
                modifierValue(stack, key("damage_race", race)),
                modifierValue(stack, key("damage_element", element == null ? "neutral" : element.name())),
                modifierValue(stack, "ragnarmmo:damage_all"));
    }

    private static double modifierValue(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty() || stack.getTag() == null || key == null || key.isBlank()) {
            return 0.0D;
        }
        CompoundTag tag = stack.getTag();
        double value = 0.0D;
        if (tag.contains(key, Tag.TAG_DOUBLE) || tag.contains(key, Tag.TAG_FLOAT) || tag.contains(key, Tag.TAG_INT)) {
            value += tag.getDouble(key);
        }
        value += modifierValue(tag.getCompound(CARD_MODIFIERS), key);
        value += modifierValue(tag.getCompound(RO_COMPOUNDED_CARD_MODIFIERS), key);
        return value;
    }

    private static double modifierValue(CompoundTag modifiers, String key) {
        return modifiers != null && modifiers.contains(key) ? modifiers.getDouble(key) : 0.0D;
    }

    private static String key(String prefix, String value) {
        String normalized = value == null || value.isBlank() ? "unknown" : value.trim().toLowerCase(Locale.ROOT);
        return "ragnarmmo:" + prefix + "_" + normalized;
    }

    private record DamageBonus(double size, double race, double element, double special) {
        private static final DamageBonus ZERO = new DamageBonus(0.0D, 0.0D, 0.0D, 0.0D);

        private DamageBonus plus(DamageBonus other) {
            if (other == null) {
                return this;
            }
            return new DamageBonus(size + other.size, race + other.race, element + other.element,
                    special + other.special);
        }

        private double multiplier() {
            return Math.max(0.0D, (1.0D + size) * (1.0D + race) * (1.0D + element) * (1.0D + special));
        }
    }
}
