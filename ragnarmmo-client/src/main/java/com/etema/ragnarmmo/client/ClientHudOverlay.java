package com.etema.ragnarmmo.client;

import com.etema.ragnarmmo.client.ui.GuiConstants;
import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.player.progression.PlayerProgressionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOClient.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientHudOverlay {
    public static final IGuiOverlay INSTANCE = ClientHudOverlay::render;

    private static final int PANEL_PADDING = 7;
    private static final int BAR_HEIGHT = 8;
    private static final int XP_BAR_HEIGHT = 4;
    private static final int LINE_SPACING = 2;
    private static final int LABEL_WIDTH = 20;
    private static final int VALUE_GAP = 4;

    private ClientHudOverlay() {
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("ragnar_status", INSTANCE);
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator()) {
            return;
        }

        RagnarCoreAPI.get(player).ifPresent(stats ->
                renderStatus(graphics, minecraft.font, stats, player, screenWidth, screenHeight));
        PartyHudOverlay.render(graphics, minecraft, screenWidth, screenHeight);
        SkillHotbarOverlay.render(graphics, minecraft, screenWidth, screenHeight);
    }

    private static void renderStatus(GuiGraphics graphics, Font font, IPlayerStats stats, Player player,
                                     int screenWidth, int screenHeight) {
        int width = 210;
        int height = PANEL_PADDING * 2 + font.lineHeight + (BAR_HEIGHT + LINE_SPACING) * 3
                + font.lineHeight + XP_BAR_HEIGHT + 2;
        int x = (screenWidth - width) / 2;
        int y = screenHeight - height - 39;
        int contentX = x + PANEL_PADDING;
        int contentW = width - PANEL_PADDING * 2;
        int cy = y + PANEL_PADDING;

        drawPanel(graphics, x, y, width, height);
        drawNameRow(graphics, font, stats, player, contentX, cy, contentW);
        cy += font.lineHeight + LINE_SPACING;

        drawBar(graphics, font, "HP", player.getHealth(), player.getMaxHealth(), contentX, cy, contentW,
                GuiConstants.COLOR_HP_BAR, GuiConstants.COLOR_HP_BAR_DARK);
        cy += BAR_HEIGHT + LINE_SPACING;

        JobType job = JobType.fromId(stats.getJobId());
        int spTop = job.isMagical() ? GuiConstants.COLOR_SP_BAR : GuiConstants.COLOR_SP_BAR_PHYS;
        int spBottom = job.isMagical() ? GuiConstants.COLOR_SP_BAR_DARK : GuiConstants.COLOR_SP_BAR_PHYS_DARK;
        drawBar(graphics, font, job.getResourceLabel(), stats.getCurrentResource(), Math.max(1.0D, stats.getMaxResource()),
                contentX, cy, contentW, spTop, spBottom);
        cy += BAR_HEIGHT + LINE_SPACING;

        drawBar(graphics, font, "FD", player.getFoodData().getFoodLevel(), 20.0D, contentX, cy, contentW,
                0xFFFFAA00, 0xFF906000);
        cy += BAR_HEIGHT + LINE_SPACING + 1;

        drawExpBars(graphics, font, stats, contentX, cy, contentW);
    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, GuiConstants.COLOR_HUD_PANEL_BG);
        graphics.renderOutline(x, y, width, height, GuiConstants.COLOR_HUD_PANEL_BORDER_OUTER);
        graphics.renderOutline(x + 1, y + 1, width - 2, height - 2, GuiConstants.COLOR_HUD_PANEL_BORDER_INNER);
    }

    private static void drawNameRow(GuiGraphics graphics, Font font, IPlayerStats stats, Player player,
                                    int x, int y, int width) {
        drawText(graphics, font, player.getName().getString(), x, y, 0xFFFFFFFF);
        String levels = "Lv." + stats.getLevel() + "  Job." + stats.getJobLevel();
        drawText(graphics, font, levels, x + width - font.width(levels), y, GuiConstants.COLOR_TEXT_DIM);

        String mode = ClientCombatState.isCombatModeEnabled() ? "AUTO" : "AUTO OFF";
        int modeColor = ClientCombatState.isCombatModeEnabled() ? 0xFF82E887 : 0xFFFF9B8F;
        drawText(graphics, font, mode, x + (width - font.width(mode)) / 2, y, modeColor);
    }

    private static void drawBar(GuiGraphics graphics, Font font, String label, double value, double max,
                                int x, int y, int width, int topColor, int bottomColor) {
        String text = (int) value + "/" + (int) max;
        int valueWidth = font.width(text);
        int barX = x + LABEL_WIDTH;
        int barWidth = Math.max(1, width - LABEL_WIDTH - valueWidth - VALUE_GAP);
        double ratio = max <= 0.0D ? 0.0D : Math.max(0.0D, Math.min(1.0D, value / max));
        int fill = (int) Math.round(barWidth * ratio);

        drawText(graphics, font, label, x, y - 1, topColor);
        graphics.fill(barX, y, barX + barWidth, y + BAR_HEIGHT, GuiConstants.COLOR_BAR_BG);
        if (fill > 0) {
            graphics.fillGradient(barX, y, barX + fill, y + BAR_HEIGHT, topColor, bottomColor);
            graphics.fill(barX, y, barX + fill, y + 1, GuiConstants.COLOR_BAR_HIGHLIGHT);
        }
        drawText(graphics, font, text, barX + barWidth + VALUE_GAP, y - 1, 0xFFFFFFFF);
    }

    private static void drawExpBars(GuiGraphics graphics, Font font, IPlayerStats stats, int x, int y, int width) {
        PlayerProgressionService progression = PlayerProgressionService.forJobId(ResourceLocation.tryParse(stats.getJobId()));
        float basePct = progression.getBaseProgressPercent(stats.getExp(), stats.getLevel());
        float jobPct = progression.getJobProgressPercent(stats.getJobExp(), stats.getJobLevel());
        int gap = 6;
        int halfW = (width - gap) / 2;

        drawThinBar(graphics, font, "EXP", x, y, halfW, basePct, GuiConstants.COLOR_XP_BAR, GuiConstants.COLOR_XP_BAR_DARK);
        drawThinBar(graphics, font, "JOB", x + halfW + gap, y, halfW, jobPct,
                GuiConstants.COLOR_JOB_XP_BAR, GuiConstants.COLOR_JOB_XP_BAR_DARK);
    }

    private static void drawThinBar(GuiGraphics graphics, Font font, String label, int x, int y, int width,
                                    float pct, int topColor, int bottomColor) {
        int labelWidth = font.width(label) + 3;
        int barX = x + labelWidth;
        int barY = y + font.lineHeight - XP_BAR_HEIGHT;
        int barW = Math.max(1, width - labelWidth);
        int fill = Math.round(barW * Math.max(0.0F, Math.min(1.0F, pct)));
        drawText(graphics, font, label, x, y, topColor);
        graphics.fill(barX, barY, barX + barW, barY + XP_BAR_HEIGHT, GuiConstants.COLOR_BAR_BG);
        if (fill > 0) {
            graphics.fillGradient(barX, barY, barX + fill, barY + XP_BAR_HEIGHT, topColor, bottomColor);
        }
    }

    private static void drawText(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, text, x + 1, y + 1, 0xAA000000, false);
        graphics.drawString(font, text, x, y, color, false);
    }
}
