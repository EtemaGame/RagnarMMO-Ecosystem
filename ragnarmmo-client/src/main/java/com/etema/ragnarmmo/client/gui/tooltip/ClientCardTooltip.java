package com.etema.ragnarmmo.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClientCardTooltip implements ClientTooltipComponent {
    private static final int IMAGE_WIDTH = 64;
    private static final int IMAGE_HEIGHT = 64;
    private static final int PADDING = 4;
    private static final int TEXT_WIDTH = 120;

    private final ResourceLocation mobTexture;
    private final Component description;

    public ClientCardTooltip(CardTooltipData data) {
        String mobId = data.mobId() == null || data.mobId().isBlank() ? "minecraft:pig" : data.mobId();
        String[] parts = mobId.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String path = parts.length == 2 ? parts[1] : parts[0];
        this.mobTexture = ResourceLocation.fromNamespaceAndPath("ragnarmmo",
                "textures/gui/cards/" + namespace + "/" + path + ".png");
        this.description = data.descriptionKey() == null || data.descriptionKey().isBlank()
                ? Component.empty()
                : Component.translatable(data.descriptionKey());
    }

    @Override
    public int getHeight() {
        return IMAGE_HEIGHT + PADDING * 2;
    }

    @Override
    public int getWidth(Font font) {
        return IMAGE_WIDTH + PADDING * 3 + TEXT_WIDTH;
    }

    @Override
    public void renderImage(Font font, int startX, int startY, GuiGraphics graphics) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(mobTexture, startX + PADDING, startY + PADDING, 0, 0,
                IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
        if (!description.getString().isEmpty()) {
            graphics.drawWordWrap(font, description, startX + IMAGE_WIDTH + PADDING * 2,
                    startY + PADDING, TEXT_WIDTH, 0xFFAAAAAA);
        }
        RenderSystem.disableBlend();
    }
}
