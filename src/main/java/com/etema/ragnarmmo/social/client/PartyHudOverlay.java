package com.etema.ragnarmmo.social.client;

import com.etema.ragnarmmo.core.client.hud.HudConfigSerializer;
import com.etema.ragnarmmo.core.client.hud.HudLayoutManager;
import com.etema.ragnarmmo.core.client.hud.HudWidgetState;
import com.etema.ragnarmmo.core.client.ui.GuiConstants;
import com.etema.ragnarmmo.core.config.RagnarClientConfigs;
import com.etema.ragnarmmo.player.party.PartyClientData;
import com.etema.ragnarmmo.player.party.net.PartyMemberData;
import com.etema.ragnarmmo.social.RagnarMMOSocial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.social.RagnarMMOSocial.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PartyHudOverlay {
    private static final int MAX_MEMBERS = 6;
    private static final int WIDTH = 126;
    private static final int PADDING = 5;
    private static final int ROW_HEIGHT = 18;
    private static final int BAR_HEIGHT = 5;
    private static final int PREVIEW_ROWS = 3;
    public static final IGuiOverlay INSTANCE = PartyHudOverlay::render;

    private PartyHudOverlay() {
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("ragnar_party_hud", INSTANCE);
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getPreviewHeight(Font font) {
        return PADDING * 2 + font.lineHeight + 3 + PREVIEW_ROWS * ROW_HEIGHT;
    }

    public static int renderPreview(GuiGraphics graphics, Font font, int width) {
        drawPanel(graphics, 0, 0, width, getPreviewHeight(font));
        drawText(graphics, font, "Party", PADDING, PADDING, 0xFFE9D8A6);
        int rowY = PADDING + font.lineHeight + 3;
        for (int i = 0; i < PREVIEW_ROWS; i++) {
            PartyMemberData member = PartyMemberData.offline(
                    new UUID(0L, i + 1L),
                    "Member " + (i + 1),
                    i == 0);
            renderMember(graphics, font, member, PADDING, rowY + i * ROW_HEIGHT, width - PADDING * 2);
        }
        return getPreviewHeight(font);
    }

    public static int renderPreview(GuiGraphics graphics, Font font) {
        return renderPreview(graphics, font, WIDTH);
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        render(graphics, Minecraft.getInstance(), screenWidth, screenHeight);
    }

    public static void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight) {
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.player.isSpectator()) {
            return;
        }
        if (!ModList.get().isLoaded(RagnarMMOSocial.MOD_ID) || !RagnarClientConfigs.CLIENT.hud.enabled.get()) {
            return;
        }

        HudWidgetState state = HudConfigSerializer.read(RagnarClientConfigs.CLIENT.hud.partyFrame);
        if (!state.enabled()) {
            return;
        }

        if (!PartyClientData.hasParty()) {
            return;
        }

        List<PartyMemberData> members = PartyClientData.getMembersSortedLeaderFirst();
        if (members.isEmpty()) {
            return;
        }

        Font font = minecraft.font;
        int visible = Math.min(MAX_MEMBERS, members.size());
        int height = PADDING * 2 + font.lineHeight + 3 + visible * ROW_HEIGHT;
        HudLayoutManager.HudBounds bounds = HudLayoutManager.bounds(state, WIDTH, height, screenWidth, screenHeight);
        HudLayoutManager.renderBackground(graphics, state, bounds);
        HudLayoutManager.pushWidgetTransform(graphics, bounds);

        drawPanel(graphics, 0, 0, WIDTH, height);
        String title = ellipsize(font, PartyClientData.getPartyName(), WIDTH - PADDING * 2);
        drawText(graphics, font, title.isBlank() ? "Party" : title, PADDING, PADDING, 0xFFE9D8A6);

        int rowY = PADDING + font.lineHeight + 3;
        for (int i = 0; i < visible; i++) {
            renderMember(graphics, font, members.get(i), PADDING, rowY + i * ROW_HEIGHT, WIDTH - PADDING * 2);
        }

        HudLayoutManager.popWidgetTransform(graphics);
    }

    private static void renderMember(GuiGraphics graphics, Font font, PartyMemberData member, int x, int y, int width) {
        String prefix = member.isLeader() ? "* " : "";
        String name = ellipsize(font, prefix + member.name(), width - 34);
        String level = "Lv." + member.level();
        int nameColor = member.isOnline() ? 0xFFFFFFFF : 0xFF8E8E92;
        int levelColor = member.isOnline() ? GuiConstants.COLOR_TEXT_DIM : 0xFF68686E;

        drawText(graphics, font, name, x, y, nameColor);
        drawText(graphics, font, level, x + width - font.width(level), y, levelColor);

        int barY = y + font.lineHeight + 1;
        graphics.fill(x, barY, x + width, barY + BAR_HEIGHT, member.isOnline() ? 0xAA151515 : 0x66151515);
        if (member.isOnline()) {
            float hp = Mth.clamp(member.getHealthProgress(), 0.0F, 1.0F);
            int fill = Math.round(width * hp);
            if (fill > 0) {
                graphics.fill(x, barY, x + fill, barY + BAR_HEIGHT, hpColor(hp));
                graphics.fill(x, barY, x + fill, barY + 1, GuiConstants.COLOR_BAR_HIGHLIGHT);
            }
        }
    }

    private static int hpColor(float hp) {
        if (hp <= 0.25F) {
            return 0xFFE24D42;
        }
        if (hp <= 0.55F) {
            return 0xFFE0BC38;
        }
        return 0xFF55C46A;
    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, GuiConstants.COLOR_HUD_PANEL_BG);
        graphics.renderOutline(x, y, width, height, GuiConstants.COLOR_HUD_PANEL_BORDER_OUTER);
        graphics.renderOutline(x + 1, y + 1, width - 2, height - 2, GuiConstants.COLOR_HUD_PANEL_BORDER_INNER);
    }

    private static String ellipsize(Font font, String text, int maxWidth) {
        if (text == null || text.isBlank() || maxWidth <= 0) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        if (ellipsisWidth >= maxWidth) {
            return font.plainSubstrByWidth(text, maxWidth);
        }
        return font.plainSubstrByWidth(text, maxWidth - ellipsisWidth) + ellipsis;
    }

    private static void drawText(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, text, x + 1, y + 1, 0xAA000000, false);
        graphics.drawString(font, text, x, y, color, false);
    }
}
