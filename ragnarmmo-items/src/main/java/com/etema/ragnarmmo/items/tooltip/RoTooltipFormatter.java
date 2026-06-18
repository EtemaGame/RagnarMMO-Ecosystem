package com.etema.ragnarmmo.items.tooltip;

import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.items.data.RoItemRule;
import com.etema.ragnarmmo.items.runtime.RoItemNbtHelper;
import com.etema.ragnarmmo.items.runtime.RoItemTextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public final class RoTooltipFormatter {
    private RoTooltipFormatter() {
    }

    public static void addTooltipLines(List<Component> tooltip, ItemStack stack, RoItemRule rule, Player player) {
        int refine = RoItemNbtHelper.getRefineLevel(stack);
        List<String> cards = RoItemNbtHelper.getSlottedCards(stack);
        boolean hasRule = rule != null && !rule.isEmpty();
        if (!hasRule && refine <= 0 && cards.isEmpty()) {
            return;
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.ragnarmmo.header").withStyle(ChatFormatting.GOLD));
        if (refine > 0) {
            tooltip.add(Component.translatable("tooltip.ragnarmmo.refine", refine).withStyle(ChatFormatting.AQUA));
        }
        if (hasRule) {
            appendRuleTooltip(tooltip, rule);
        }
        if (!cards.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ragnarmmo.cards", cards.size()).withStyle(ChatFormatting.LIGHT_PURPLE));
            if (Screen.hasShiftDown()) {
                for (String card : cards) {
                    tooltip.add(Component.literal(" - " + card).withStyle(ChatFormatting.DARK_PURPLE));
                }
            } else {
                tooltip.add(Component.translatable("tooltip.ragnarmmo.cards_shift").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static void appendRuleTooltip(List<Component> tooltip, RoItemRule rule) {
        if (rule.requiredBaseLevel() > 0) {
            tooltip.add(Component.translatable("tooltip.ragnarmmo.required_base", rule.requiredBaseLevel()).withStyle(ChatFormatting.GRAY));
        }
        if (!rule.allowedJobs().isEmpty()) {
            String jobs = rule.allowedJobs().stream().map(job -> job.getDisplayName()).sorted().reduce((left, right) -> left + ", " + right).orElse("");
            tooltip.add(Component.translatable("tooltip.ragnarmmo.jobs", jobs).withStyle(ChatFormatting.GRAY));
        }
        if (rule.cardSlots() > 0) {
            tooltip.add(Component.translatable("tooltip.ragnarmmo.slots", rule.cardSlots()).withStyle(ChatFormatting.BLUE));
        }
        for (Map.Entry<StatKeys, Integer> entry : rule.attributeBonuses().entrySet()) {
            int value = entry.getValue();
            if (value == 0) {
                continue;
            }
            String sign = value > 0 ? "+" : "";
            tooltip.add(Component.translatable("tooltip.ragnarmmo.stat_bonus", entry.getKey().name(), sign + value)
                    .withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }
}
