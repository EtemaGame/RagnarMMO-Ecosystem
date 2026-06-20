package com.etema.ragnarmmo.items.mixin;

import com.etema.ragnarmmo.items.equipment.RagnarEquipmentProvider;
import com.etema.ragnarmmo.items.equipment.RagnarEquipmentSlot;
import com.etema.ragnarmmo.items.equipment.RagnarEquipmentSlotType;
import com.etema.ragnarmmo.items.equipment.RagnarEquipmentSlots;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ragnarmmo$addEquipmentSlots(Inventory inventory, boolean active, Player player, CallbackInfo ci) {
        RagnarEquipmentProvider.get(player).ifPresent(handler -> {
            AbstractContainerMenuAccessor accessor = (AbstractContainerMenuAccessor) this;
            accessor.ragnarmmo$addSlot(new RagnarEquipmentSlot(handler, RagnarEquipmentSlots.MID_HEAD, 77, 8,
                    RagnarEquipmentSlotType.MID_HEAD));
            accessor.ragnarmmo$addSlot(new RagnarEquipmentSlot(handler, RagnarEquipmentSlots.ACCESSORY_1, 77, 26,
                    RagnarEquipmentSlotType.ACCESSORY_1));
            accessor.ragnarmmo$addSlot(new RagnarEquipmentSlot(handler, RagnarEquipmentSlots.ACCESSORY_2, 77, 44,
                    RagnarEquipmentSlotType.ACCESSORY_2));
        });
    }
}
