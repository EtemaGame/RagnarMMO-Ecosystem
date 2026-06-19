package com.etema.ragnarmmo.lifeskills;

import com.etema.ragnarmmo.core.client.hud.HudConfigSerializer;
import com.etema.ragnarmmo.core.client.hud.HudLayoutManager;
import com.etema.ragnarmmo.core.client.hud.HudWidgetState;
import com.etema.ragnarmmo.core.config.RagnarClientConfigs;
import com.etema.ragnarmmo.common.api.lifeskills.LifeSkillType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = RagnarMMOLifeSkills.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LifeSkillHudOverlay {
    private static final int WIDTH = 190;
    private static final int BASE_HEIGHT = 58;
    public static final IGuiOverlay INSTANCE = LifeSkillHudOverlay::render;

    private LifeSkillHudOverlay() {
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("ragnar_lifeskill_notifications", INSTANCE);
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.player.isSpectator()) {
            return;
        }
        if (!RagnarClientConfigs.CLIENT.hud.enabled.get()) {
            return;
        }

        HudWidgetState state = HudConfigSerializer.read(RagnarClientConfigs.CLIENT.hud.notifications);
        if (!state.enabled()) {
            return;
        }

        List<Line> lines = buildLines();
        if (lines.isEmpty()) {
            return;
        }

        Font font = mc.font;
        int height = Math.max(BASE_HEIGHT, 18 + lines.size() * 12);
        HudLayoutManager.HudBounds bounds = HudLayoutManager.bounds(state, WIDTH, height, screenWidth, screenHeight);
        HudLayoutManager.renderBackground(graphics, state, bounds);
        HudLayoutManager.pushWidgetTransform(graphics, bounds);

        drawPanel(graphics, 0, 0, WIDTH, height);
        int y = 6;
        for (Line line : lines) {
            graphics.drawString(font, line.text(), 8, y, line.color(), false);
            y += 12;
        }

        HudLayoutManager.popWidgetTransform(graphics);
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getHeight() {
        return BASE_HEIGHT;
    }

    public static int renderPreview(GuiGraphics graphics, Font font, int width) {
        drawPanel(graphics, 0, 0, width, BASE_HEIGHT);
        graphics.drawString(font, Component.literal("Level Up!"), 8, 6, 0xFFFFD166, false);
        graphics.drawString(font, Component.literal("+32 Mining"), 8, 18, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.literal("+5 Woodcutting"), 8, 30, 0xFFB6BECF, false);
        return BASE_HEIGHT;
    }

    private static List<Line> buildLines() {
        List<Line> lines = new ArrayList<>();

        if (LifeSkillClientHandler.hasLevelUpToShow()) {
            LifeSkillType skill = LifeSkillClientHandler.getLevelUpSkill();
            lines.add(new Line(Component.literal("Level Up! " + LifeSkillClientHandler.getSkillIcon(skill)
                    + " " + skill.name() + " " + LifeSkillClientHandler.getLevelUpLevel()).getString(), 0xFFFFD166));
        }

        for (Map.Entry<LifeSkillType, Integer> entry : LifeSkillClientHandler.consumeAccumulatedPoints().entrySet()) {
            lines.add(new Line("+" + entry.getValue() + " " + LifeSkillClientHandler.getSkillIcon(entry.getKey())
                    + " " + entry.getKey().name(), 0xFFFFFFFF));
        }

        return lines;
    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xD0151520);
        graphics.renderOutline(x, y, width, height, 0xFF202030);
        graphics.renderOutline(x + 1, y + 1, width - 2, height - 2, 0xFF404060);
    }

    private record Line(String text, int color) {
    }
}
