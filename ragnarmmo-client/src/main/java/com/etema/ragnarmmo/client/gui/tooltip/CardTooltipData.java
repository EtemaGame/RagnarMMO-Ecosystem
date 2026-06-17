package com.etema.ragnarmmo.client.gui.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CardTooltipData(String cardId, String mobId, String descriptionKey) implements TooltipComponent {
}
