package com.etema.ragnarmmo.items.equipment;

import net.minecraft.world.item.ItemStack;

public final class RagnarEquipmentRules {
    private RagnarEquipmentRules() {
    }

    public static boolean isValidFor(RagnarEquipmentSlotType type, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        return switch (type) {
            case MID_HEAD -> isMidHead(stack);
            case ACCESSORY_1, ACCESSORY_2 -> isAccessory(stack);
        };
    }

    public static boolean isMidHead(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(RagnarEquipmentTags.MID_HEAD_EQUIPPABLE);
    }

    public static boolean isAccessory(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(RagnarEquipmentTags.ACCESSORY_EQUIPPABLE);
    }
}
