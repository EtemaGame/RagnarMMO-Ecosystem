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
        double bonus = 0.0D;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR && slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
                continue;
            }
            bonus += outgoingBonus(attacker.getItemBySlot(slot), targetRace, targetElement, targetSize);
        }
        return Math.max(0.0D, 1.0D + bonus);
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

    private static double outgoingBonus(ItemStack stack, String race, ElementType element, CombatMath.MobSize size) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        double bonus = modifierValue(stack, "ragnarmmo:damage_all");
        bonus += modifierValue(stack, key("damage_race", race));
        bonus += modifierValue(stack, key("damage_element", element == null ? "neutral" : element.name()));
        bonus += modifierValue(stack, key("damage_size", size == null ? "medium" : size.name()));
        return bonus;
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
}
