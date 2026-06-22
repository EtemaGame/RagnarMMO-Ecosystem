package com.etema.ragnarmmo.items.equipment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public final class RagnarEquipmentHandler extends ItemStackHandler {
    private final Player player;

    public RagnarEquipmentHandler(Player player) {
        super(RagnarEquipmentSlots.SLOT_COUNT);
        this.player = player;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot < 0 || slot >= RagnarEquipmentSlots.SLOT_COUNT) {
            return false;
        }
        return RagnarEquipmentRules.isValidFor(RagnarEquipmentSlots.typeForIndex(slot), stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide) {
            RagnarEquipmentSync.sync(serverPlayer);
        }
    }
}
