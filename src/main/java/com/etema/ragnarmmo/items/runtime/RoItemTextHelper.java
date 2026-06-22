package com.etema.ragnarmmo.items.runtime;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class RoItemTextHelper {
    private RoItemTextHelper() {
    }

    public static Component getDisplayName(ItemStack stack) {
        return stack.getHoverName().copy();
    }

    public static String getDisplayNameString(ItemStack stack) {
        return getDisplayName(stack).getString();
    }
}
