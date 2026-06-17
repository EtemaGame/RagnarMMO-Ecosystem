package com.etema.ragnarmmo.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PlaceholderInfoScreen extends Screen {
    private final Component lineOne;
    private final Component lineTwo;

    public static PlaceholderInfoScreen skills() {
        return new PlaceholderInfoScreen(
                Component.translatable("gui.ragnarmmo.skills.title"),
                Component.translatable("gui.ragnarmmo.skills.placeholder.1"),
                Component.translatable("gui.ragnarmmo.skills.placeholder.2"));
    }

    public static PlaceholderInfoScreen achievements() {
        return new PlaceholderInfoScreen(
                Component.translatable("gui.ragnarmmo.achievements.title"),
                Component.translatable("gui.ragnarmmo.achievements.placeholder.1"),
                Component.translatable("gui.ragnarmmo.achievements.placeholder.2"));
    }

    private PlaceholderInfoScreen(Component title, Component lineOne, Component lineTwo) {
        super(title);
        this.lineOne = lineOne;
        this.lineTwo = lineTwo;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),
                        button -> Minecraft.getInstance().setScreen(new RagnarMainMenuScreen()))
                .bounds(this.width / 2 - 80, this.height / 2 + 48, 160, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int panelW = Math.min(300, this.width - 32);
        int panelH = 136;
        int x = (this.width - panelW) / 2;
        int y = (this.height - panelH) / 2;
        graphics.fill(x, y, x + panelW, y + panelH, 0xDD111217);
        graphics.fill(x, y, x + panelW, y + 28, 0xEE242832);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, y + 10, 0xFFE9D8A6);
        graphics.drawCenteredString(this.font, lineOne, this.width / 2, y + 48, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, lineTwo, this.width / 2, y + 64, 0xFFB8C0CC);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
