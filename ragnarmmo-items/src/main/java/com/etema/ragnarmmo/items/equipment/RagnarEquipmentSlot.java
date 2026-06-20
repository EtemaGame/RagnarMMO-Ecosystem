package com.etema.ragnarmmo.items.equipment;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class RagnarEquipmentSlot extends SlotItemHandler {
    private final RagnarEquipmentSlotType type;

    public RagnarEquipmentSlot(IItemHandler itemHandler, int index, int x, int y, RagnarEquipmentSlotType type) {
        super(itemHandler, index, x, y);
        this.type = type;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return RagnarEquipmentRules.isValidFor(type, stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public RagnarEquipmentSlotType getRagnarSlotType() {
        return type;
    }
}
