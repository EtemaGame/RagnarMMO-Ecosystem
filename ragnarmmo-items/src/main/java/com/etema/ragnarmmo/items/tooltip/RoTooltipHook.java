package com.etema.ragnarmmo.items.tooltip;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.items.data.RoItemRule;
import com.etema.ragnarmmo.items.runtime.RoItemNbtHelper;
import com.etema.ragnarmmo.items.runtime.RoItemRuleResolver;
import com.etema.ragnarmmo.items.runtime.RoItemTextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RoTooltipHook {
    private RoTooltipHook() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        RoItemRule rule = RoItemRuleResolver.resolve(stack);
        int refineLevel = RoItemNbtHelper.getRefineLevel(stack);
        if ((rule == null || rule.isEmpty()) && refineLevel <= 0 && RoItemNbtHelper.getSlottedCards(stack).isEmpty()) {
            return;
        }

        if (!event.getToolTip().isEmpty()) {
            event.getToolTip().set(0, RoItemTextHelper.getDisplayName(stack));
        }

        Player player = Minecraft.getInstance().player;
        RoTooltipFormatter.addTooltipLines(event.getToolTip(), stack, rule, player);
    }
}
