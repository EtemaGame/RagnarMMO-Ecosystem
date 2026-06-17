package com.etema.ragnarmmo.client.tooltip;

import com.etema.ragnarmmo.client.RagnarMMOClient;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.items.data.RoItemRule;
import com.etema.ragnarmmo.items.runtime.RoItemNbtHelper;
import com.etema.ragnarmmo.items.runtime.RoItemRuleResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = RagnarMMOClient.MOD_ID, value = Dist.CLIENT)
public final class RoItemTooltipHandler {
    private RoItemTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!ModList.get().isLoaded("ragnarmmo_items")) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        RoItemRule rule = RoItemRuleResolver.resolve(stack);
        int refine = RoItemNbtHelper.getRefineLevel(stack);
        List<String> cards = RoItemNbtHelper.getSlottedCards(stack);
        if ((rule == null || rule.isEmpty()) && refine <= 0 && cards.isEmpty()) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("RagnarMMO").withStyle(ChatFormatting.GOLD));

        if (refine > 0) {
            tooltip.add(Component.literal("+" + refine + " Refine").withStyle(ChatFormatting.AQUA));
        }
        if (rule != null && !rule.isEmpty()) {
            appendRuleTooltip(tooltip, rule);
        }
        if (!cards.isEmpty()) {
            tooltip.add(Component.literal("Cards: " + cards.size()).withStyle(ChatFormatting.LIGHT_PURPLE));
            if (Screen.hasShiftDown()) {
                for (String card : cards) {
                    tooltip.add(Component.literal(" - " + card).withStyle(ChatFormatting.DARK_PURPLE));
                }
            } else {
                tooltip.add(Component.literal("[SHIFT] Card IDs").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static void appendRuleTooltip(List<Component> tooltip, RoItemRule rule) {
        if (rule.requiredBaseLevel() > 0) {
            tooltip.add(Component.literal("Required Base Lv. " + rule.requiredBaseLevel())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (!rule.allowedJobs().isEmpty()) {
            String jobs = rule.allowedJobs().stream()
                    .map(job -> job.getDisplayName())
                    .sorted()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
            tooltip.add(Component.literal("Jobs: " + jobs).withStyle(ChatFormatting.GRAY));
        }
        if (rule.cardSlots() > 0) {
            tooltip.add(Component.literal("Slots: " + rule.cardSlots()).withStyle(ChatFormatting.BLUE));
        }
        for (Map.Entry<StatKeys, Integer> entry : rule.attributeBonuses().entrySet()) {
            int value = entry.getValue();
            if (value == 0) {
                continue;
            }
            String sign = value > 0 ? "+" : "";
            tooltip.add(Component.literal(entry.getKey().name() + " " + sign + value)
                    .withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }
}
