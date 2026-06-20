package com.etema.ragnarmmo.items.mixin;

import com.etema.ragnarmmo.items.equipment.RagnarEquipmentSlot;
import com.etema.ragnarmmo.items.equipment.RagnarEquipmentSlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {
    protected InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void ragnarmmo$renderEquipmentSlotBackgrounds(GuiGraphics graphics, float partialTick,
                                                          int mouseX, int mouseY, CallbackInfo ci) {
        for (Slot slot : this.menu.slots) {
            if (slot instanceof RagnarEquipmentSlot) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;
                graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8F8F96);
                graphics.fill(x, y, x + 16, y + 16, 0xFF303038);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void ragnarmmo$renderEmptyEquipmentTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                                                       float partialTick, CallbackInfo ci) {
        for (Slot slot : this.menu.slots) {
            if (!(slot instanceof RagnarEquipmentSlot ragnarSlot) || slot.hasItem()) {
                continue;
            }
            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.renderComponentTooltip(this.font, List.of(
                        Component.translatable(slotTranslationKey(ragnarSlot.getRagnarSlotType()))
                                .withStyle(ChatFormatting.GOLD),
                        Component.translatable("slot.ragnarmmo.empty_hint").withStyle(ChatFormatting.GRAY)
                ), mouseX, mouseY);
                return;
            }
        }
    }

    private static String slotTranslationKey(RagnarEquipmentSlotType type) {
        return switch (type) {
            case MID_HEAD -> "slot.ragnarmmo.mid_head";
            case ACCESSORY_1 -> "slot.ragnarmmo.accessory_1";
            case ACCESSORY_2 -> "slot.ragnarmmo.accessory_2";
        };
    }
}
