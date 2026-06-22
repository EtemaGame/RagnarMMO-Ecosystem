package com.etema.ragnarmmo.items.client.tooltip;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public final class RoItemTooltipHandler {
    private RoItemTooltipHandler() {
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        com.etema.ragnarmmo.items.tooltip.RoTooltipHook.onItemTooltip(event);
    }
}
