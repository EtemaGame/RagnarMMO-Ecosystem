package com.etema.ragnarmmo.social.client.ui;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.core.client.CoreHudOverlay;
import com.etema.ragnarmmo.core.client.hud.HudConfigSerializer;
import com.etema.ragnarmmo.core.client.hud.HudLayoutManager;
import com.etema.ragnarmmo.core.client.hud.HudWidgetDefinition;
import com.etema.ragnarmmo.core.client.hud.HudWidgetState;
import com.etema.ragnarmmo.core.config.RagnarClientConfigs;
import com.etema.ragnarmmo.social.client.PartyHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class HudOverlayConfigScreen extends Screen {
    private static final int SNAP_DISTANCE = 6;
    private static final int HOTBAR_WIDTH = 159;
    private static final int HOTBAR_HEIGHT = 36;
    private static final int NOTIFICATIONS_WIDTH = 190;
    private static final int NOTIFICATIONS_HEIGHT = 58;

    private final List<DraggableWidget> widgets = new ArrayList<>();
    private DraggableWidget selectedWidget;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    private RangeSlider scaleSlider;
    private RangeSlider alphaSlider;
    private ToggleCheckbox enabledCheckbox;
    private ToggleCheckbox backgroundCheckbox;

    public HudOverlayConfigScreen() {
        super(Component.translatable("screen.ragnarmmo.overlay.title"));
    }

    @Override
    protected void init() {
        widgets.clear();
        addWidget(
                RagnarClientConfigs.CLIENT.hud.status,
                Component.translatable("screen.ragnarmmo.overlay.widget.status"),
                font -> CoreHudOverlay.getStatusWidth(),
                CoreHudOverlay::getStatusHeight,
                this::renderStatusPreview);
        addWidget(
                RagnarClientConfigs.CLIENT.hud.partyFrame,
                Component.translatable("screen.ragnarmmo.overlay.widget.party_frame"),
                font -> PartyHudOverlay.getWidth(),
                PartyHudOverlay::getPreviewHeight,
                this::renderPartyPreview);
        addWidget(
                RagnarClientConfigs.CLIENT.hud.skillHotbar,
                Component.translatable("screen.ragnarmmo.overlay.widget.skill_hotbar"),
                font -> HOTBAR_WIDTH,
                font -> HOTBAR_HEIGHT,
                this::renderHotbarPreview);
        addWidget(
                RagnarClientConfigs.CLIENT.hud.notifications,
                Component.translatable("screen.ragnarmmo.overlay.widget.notifications"),
                font -> NOTIFICATIONS_WIDTH,
                font -> NOTIFICATIONS_HEIGHT,
                this::renderNotificationsPreview);
        widgets.sort(Comparator.comparingInt(widget -> widget.zOrder));

        int buttonY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.ragnarmmo.overlay.button.done"),
                btn -> this.onClose()).bounds(this.width / 2 - 40, buttonY, 80, 20).build());

        int controlX = 10;
        int controlY = 36;
        int controlWidth = 150;
        scaleSlider = new RangeSlider(controlX, controlY, controlWidth, 20,
                Component.translatable("screen.ragnarmmo.overlay.control.scale"), 0.1D, 3.0D, 1.0D, 0.1D, true,
                value -> {
                    if (selectedWidget != null) {
                        selectedWidget.setScale(value);
                    }
                });
        alphaSlider = new RangeSlider(controlX, controlY + 24, controlWidth, 20,
                Component.translatable("screen.ragnarmmo.overlay.control.alpha"), 0.0D, 255.0D, 100.0D, 1.0D, false,
                value -> {
                    if (selectedWidget != null) {
                        selectedWidget.setBackgroundAlpha(value.intValue());
                    }
                });
        enabledCheckbox = new ToggleCheckbox(controlX, controlY + 48, controlWidth, 20,
                Component.translatable("screen.ragnarmmo.overlay.control.enabled"), true,
                value -> {
                    if (selectedWidget != null) {
                        selectedWidget.setEnabled(value);
                    }
                });
        backgroundCheckbox = new ToggleCheckbox(controlX, controlY + 72, controlWidth, 20,
                Component.translatable("screen.ragnarmmo.overlay.control.show_background"), true,
                value -> {
                    if (selectedWidget != null) {
                        selectedWidget.setShowBackground(value);
                    }
                });
        this.addRenderableWidget(scaleSlider);
        this.addRenderableWidget(alphaSlider);
        this.addRenderableWidget(enabledCheckbox);
        this.addRenderableWidget(backgroundCheckbox);

        selectWidget(widgets.isEmpty() ? null : widgets.get(0));
    }

    private void addWidget(
            RagnarClientConfigs.Hud.HudComponent config,
            Component name,
            ToIntFunction<Font> widthProvider,
            ToIntFunction<Font> heightProvider,
            PreviewRenderer previewRenderer) {
        widgets.add(new DraggableWidget(
                config,
                new HudWidgetDefinition(name.getString(), name, config.zOrder.get()),
                widthProvider,
                heightProvider,
                previewRenderer));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        if (selectedWidget == null && !widgets.isEmpty()) {
            selectWidget(widgets.get(0));
        }
    }

    private void selectWidget(DraggableWidget widget) {
        this.selectedWidget = widget;
        updateControls();
    }

    private void updateControls() {
        boolean hasSelection = selectedWidget != null;
        scaleSlider.active = hasSelection;
        alphaSlider.active = hasSelection;
        enabledCheckbox.active = hasSelection;
        backgroundCheckbox.active = hasSelection;
        if (!hasSelection) {
            enabledCheckbox.setSelected(false);
            backgroundCheckbox.setSelected(false);
            return;
        }
        scaleSlider.setValue(selectedWidget.getScale());
        alphaSlider.setValue(selectedWidget.getBackgroundAlpha());
        enabledCheckbox.setSelected(selectedWidget.isEnabled());
        backgroundCheckbox.setSelected(selectedWidget.isShowBackground());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, Component.translatable("screen.ragnarmmo.overlay.instructions.move"),
                this.width / 2, 14, 0xFFE9D8A6);
        graphics.drawString(this.font,
                selectedWidget == null
                        ? Component.translatable("screen.ragnarmmo.overlay.select_component")
                        : Component.translatable("screen.ragnarmmo.overlay.selected", selectedWidget.definition.displayName()),
                10, 16, selectedWidget == null ? 0xFFAAAAAA : 0xFFFFFFFF, false);

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null) {
            RagnarCoreAPI.get(player).ifPresent(stats -> {
                for (DraggableWidget widget : widgets) {
                    widget.render(graphics, this.font, stats, player, this.width, this.height);
                    if (widget == selectedWidget || widget.isMouseOver(mouseX, mouseY, this.font, this.width, this.height)) {
                        widget.renderSelection(graphics, this.font, this.width, this.height);
                    }
                }
            });
        } else {
            for (DraggableWidget widget : widgets) {
                widget.render(graphics, this.font, null, null, this.width, this.height);
                if (widget == selectedWidget || widget.isMouseOver(mouseX, mouseY, this.font, this.width, this.height)) {
                    widget.renderSelection(graphics, this.font, this.width, this.height);
                }
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }

            for (int i = widgets.size() - 1; i >= 0; i--) {
                DraggableWidget widget = widgets.get(i);
                if (widget.isMouseOver(mouseX, mouseY, this.font, this.width, this.height)) {
                    selectWidget(widget);
                    dragging = true;
                    int[] position = widget.getPosition(this.font, this.width, this.height);
                    dragOffsetX = (int) Math.round(mouseX) - position[0];
                    dragOffsetY = (int) Math.round(mouseY) - position[1];
                    return true;
                }
            }

            selectWidget(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && selectedWidget != null && dragging) {
            int newX = (int) Math.round(mouseX) - dragOffsetX;
            int newY = (int) Math.round(mouseY) - dragOffsetY;
            selectedWidget.updateAnchor(newX, newY, this.font, this.width, this.height);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void onClose() {
        for (DraggableWidget widget : widgets) {
            widget.save();
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderStatusPreview(GuiGraphics graphics, Font font, int width) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null) {
            RagnarCoreAPI.get(player).ifPresentOrElse(
                    stats -> CoreHudOverlay.renderPreview(graphics, font, stats, player, width),
                    () -> CoreHudOverlay.renderPlaceholder(graphics, font, width));
            return;
        }
        CoreHudOverlay.renderPlaceholder(graphics, font, width);
    }

    private void renderPartyPreview(GuiGraphics graphics, Font font, int width) {
        PartyHudOverlay.renderPreview(graphics, font, width);
    }

    private void renderHotbarPreview(GuiGraphics graphics, Font font, int width) {
        int slots = 6;
        int slot = 24;
        int gap = 3;
        for (int i = 0; i < slots; i++) {
            int sx = i * (slot + gap);
            graphics.fill(sx, 0, sx + slot, slot, 0xD0151520);
            graphics.renderOutline(sx, 0, slot, slot, 0xFF404060);
            graphics.drawString(font, Integer.toString(i + 1), sx + 2, 2, 0xFFBFC7D5, false);
            graphics.drawCenteredString(font, "SK", sx + slot / 2, 13, 0xFFFFFFFF);
        }
        graphics.drawCenteredString(font, Component.literal("COMBAT"), width / 2, HOTBAR_HEIGHT - 8, 0xFFFFD166);
    }

    private void renderNotificationsPreview(GuiGraphics graphics, Font font, int width) {
        graphics.fill(0, 0, width, NOTIFICATIONS_HEIGHT, 0xD0151520);
        graphics.renderOutline(0, 0, width, NOTIFICATIONS_HEIGHT, 0xFF202030);
        graphics.renderOutline(1, 1, width - 2, NOTIFICATIONS_HEIGHT - 2, 0xFF404060);
        graphics.drawString(font, Component.literal("Level Up!"), 8, 6, 0xFFFFD166, false);
        graphics.drawString(font, Component.literal("+32 Mining"), 8, 18, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.literal("+5 Woodcutting"), 8, 30, 0xFFB6BECF, false);
    }

    private interface PreviewRenderer {
        void render(GuiGraphics graphics, Font font, int width);
    }

    private final class DraggableWidget {
        final RagnarClientConfigs.Hud.HudComponent config;
        final HudWidgetDefinition definition;
        final ToIntFunction<Font> widthProvider;
        final ToIntFunction<Font> heightProvider;
        final PreviewRenderer previewRenderer;

        double anchorX;
        double anchorY;
        double scale;
        int backgroundAlpha;
        boolean showBackground;
        boolean enabled;
        int zOrder;

        DraggableWidget(
                RagnarClientConfigs.Hud.HudComponent config,
                HudWidgetDefinition definition,
                ToIntFunction<Font> widthProvider,
                ToIntFunction<Font> heightProvider,
                PreviewRenderer previewRenderer) {
            this.config = config;
            this.definition = definition;
            this.widthProvider = widthProvider;
            this.heightProvider = heightProvider;
            this.previewRenderer = previewRenderer;
            HudWidgetState state = HudConfigSerializer.read(config);
            this.anchorX = state.anchorX();
            this.anchorY = state.anchorY();
            this.scale = state.scale();
            this.backgroundAlpha = state.backgroundAlpha();
            this.showBackground = state.showBackground();
            this.enabled = state.enabled();
            this.zOrder = state.zOrder();
        }

        int[] getPosition(Font font, int screenWidth, int screenHeight) {
            HudLayoutManager.HudBounds bounds = getBounds(font, screenWidth, screenHeight);
            return new int[] { bounds.x(), bounds.y() };
        }

        void render(GuiGraphics graphics, Font font, IPlayerStats stats, Player player, int screenWidth, int screenHeight) {
            HudLayoutManager.HudBounds bounds = getBounds(font, screenWidth, screenHeight);
            HudLayoutManager.renderBackground(graphics, currentState(), bounds);
            if (!enabled) {
                return;
            }
            HudLayoutManager.pushWidgetTransform(graphics, bounds);
            previewRenderer.render(graphics, font, bounds.width());
            HudLayoutManager.popWidgetTransform(graphics);
        }

        void renderSelection(GuiGraphics graphics, Font font, int screenWidth, int screenHeight) {
            HudLayoutManager.HudBounds bounds = getBounds(font, screenWidth, screenHeight);
            graphics.fillGradient(bounds.x() - 2, bounds.y() - 2,
                    bounds.x() + bounds.realWidth() + 2, bounds.y() + bounds.realHeight() + 2,
                    0x40FFFFFF, 0x40FFFFFF);
            graphics.renderOutline(bounds.x() - 2, bounds.y() - 2,
                    bounds.realWidth() + 4, bounds.realHeight() + 4, 0xFFFFFFFF);
        }

        boolean isMouseOver(double mouseX, double mouseY, Font font, int screenWidth, int screenHeight) {
            HudLayoutManager.HudBounds bounds = getBounds(font, screenWidth, screenHeight);
            return mouseX >= bounds.x() - 2
                    && mouseX <= bounds.x() + bounds.realWidth() + 2
                    && mouseY >= bounds.y() - 2
                    && mouseY <= bounds.y() + bounds.realHeight() + 2;
        }

        void updateAnchor(int pixelX, int pixelY, Font font, int screenWidth, int screenHeight) {
            int width = getWidth(font);
            int height = getHeight(font);
            int realWidth = Math.max(1, (int) Math.round(width * scale));
            int realHeight = Math.max(1, (int) Math.round(height * scale));
            pixelX = snapCoordinate(pixelX, realWidth, screenWidth);
            pixelY = snapCoordinate(pixelY, realHeight, screenHeight);
            HudLayoutManager.Anchor anchor = HudLayoutManager.anchorFromPixel(
                    pixelX, pixelY, currentState(), width, height, screenWidth, screenHeight);
            this.anchorX = anchor.x();
            this.anchorY = anchor.y();
        }

        int getWidth(Font font) {
            return Math.max(1, widthProvider.applyAsInt(font));
        }

        int getHeight(Font font) {
            return Math.max(1, heightProvider.applyAsInt(font));
        }

        void setScale(double scale) {
            this.scale = scale;
        }

        double getScale() {
            return scale;
        }

        void setBackgroundAlpha(int alpha) {
            this.backgroundAlpha = alpha;
        }

        int getBackgroundAlpha() {
            return backgroundAlpha;
        }

        void setShowBackground(boolean showBackground) {
            this.showBackground = showBackground;
        }

        boolean isShowBackground() {
            return showBackground;
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        boolean isEnabled() {
            return enabled;
        }

        void save() {
            HudConfigSerializer.write(config, currentState());
        }

        private HudLayoutManager.HudBounds getBounds(Font font, int screenWidth, int screenHeight) {
            return HudLayoutManager.bounds(currentState(), getWidth(font), getHeight(font), screenWidth, screenHeight);
        }

        private HudWidgetState currentState() {
            return new HudWidgetState(enabled, anchorX, anchorY, scale, backgroundAlpha, showBackground, zOrder);
        }
    }

    private static int snapCoordinate(int pixel, int widgetSize, int screenSize) {
        int max = Math.max(0, screenSize - widgetSize);
        int center = max / 2;
        if (Math.abs(pixel) <= SNAP_DISTANCE) {
            return 0;
        }
        if (Math.abs(pixel - center) <= SNAP_DISTANCE) {
            return center;
        }
        if (Math.abs(pixel - max) <= SNAP_DISTANCE) {
            return max;
        }
        return Mth.clamp(pixel, 0, max);
    }

    private static class RangeSlider extends AbstractSliderButton {
        private final double minValue;
        private final double maxValue;
        private final double stepSize;
        private final Component prefix;
        private final Consumer<Double> onChange;
        private final boolean showDecimals;

        RangeSlider(int x, int y, int width, int height, Component prefix, double minValue, double maxValue,
                double currentValue, double stepSize, boolean showDecimals, Consumer<Double> onChange) {
            super(x, y, width, height, Component.empty(), 0.0D);
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.stepSize = stepSize;
            this.prefix = prefix;
            this.onChange = onChange;
            this.showDecimals = showDecimals;
            this.value = (Mth.clamp(currentValue, minValue, maxValue) - minValue) / (maxValue - minValue);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double currentValue = getValue();
            String text = showDecimals ? String.format(java.util.Locale.ROOT, "%.1f", currentValue)
                    : Integer.toString((int) Math.round(currentValue));
            setMessage(prefix.copy().append(text));
        }

        @Override
        protected void applyValue() {
            onChange.accept(getValue());
        }

        void setValue(double value) {
            this.value = (Mth.clamp(value, minValue, maxValue) - minValue) / (maxValue - minValue);
            updateMessage();
        }

        private double getValue() {
            double value = minValue + this.value * (maxValue - minValue);
            if (stepSize > 0.0D) {
                value = Math.round(value / stepSize) * stepSize;
            }
            return Mth.clamp(value, minValue, maxValue);
        }
    }

    private static class ToggleCheckbox extends net.minecraft.client.gui.components.AbstractButton {
        private boolean selected;
        private final Consumer<Boolean> onChange;

        ToggleCheckbox(int x, int y, int width, int height, Component message, boolean selected,
                Consumer<Boolean> onChange) {
            super(x, y, width, height, message);
            this.selected = selected;
            this.onChange = onChange;
        }

        @Override
        public void onPress() {
            this.selected = !this.selected;
            this.onChange.accept(this.selected);
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int color = this.active ? 0xFFFFFFFF : 0xFFA0A0A0;
            int boxSize = 11;
            int boxX = this.getX();
            int boxY = this.getY() + (this.height - boxSize) / 2;

            graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFF000000);
            graphics.renderOutline(boxX, boxY, boxSize, boxSize, 0xFFAAAAAA);

            if (this.selected) {
                graphics.drawString(Minecraft.getInstance().font, "\u2714", boxX + 2, boxY + 1, 0xFF00FF00, false);
            }

            graphics.drawString(Minecraft.getInstance().font, this.getMessage(), boxX + boxSize + 4,
                    this.getY() + (this.height - 8) / 2, color, false);
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
