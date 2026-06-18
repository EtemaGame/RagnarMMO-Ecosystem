package com.etema.ragnarmmo.jobs.client;

import com.etema.ragnarmmo.combat.client.ClientCombatState;
import com.etema.ragnarmmo.core.client.ui.GuiConstants;
import com.etema.ragnarmmo.jobs.data.SkillDefinition;
import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.jobs.RagnarMMOJobs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JobsHudOverlay {
    private static final int SLOTS = 6;
    private static final int SLOT = 24;
    private static final int GAP = 3;
    private static final int HEIGHT = SLOT + 12;
    public static final IGuiOverlay INSTANCE = JobsHudOverlay::render;

    private JobsHudOverlay() {
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("ragnar_skill_hotbar", INSTANCE);
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.player.isSpectator()) {
            return;
        }
        boolean hasAny = false;
        for (int i = 0; i < SLOTS; i++) {
            if (JobSkillsClientCache.getHotbarSlot(i) != null) {
                hasAny = true;
                break;
            }
        }
        if (!hasAny && !ClientCombatState.isCombatModeEnabled()) {
            return;
        }

        int width = SLOTS * SLOT + (SLOTS - 1) * GAP;
        int x = (screenWidth - width) / 2;
        int y = screenHeight - 93;
        Font font = minecraft.font;

        for (int i = 0; i < SLOTS; i++) {
            int slotX = x + i * (SLOT + GAP);
            renderSlot(graphics, font, i, JobSkillsClientCache.getHotbarSlot(i), slotX, y);
        }

        if (ClientCombatState.isCombatModeEnabled()) {
            graphics.drawCenteredString(font, "COMBAT", screenWidth / 2, y + HEIGHT - 8, 0xFFFFD166);
        }
    }

    private static void renderSlot(GuiGraphics graphics, Font font, int index, ResourceLocation skillId, int x, int y) {
        int border = ClientCombatState.isCombatModeEnabled() ? 0xFFFFD166 : GuiConstants.COLOR_HUD_PANEL_BORDER_INNER;
        graphics.fill(x, y, x + SLOT, y + SLOT, 0xD0151520);
        graphics.renderOutline(x, y, SLOT, SLOT, border);
        graphics.drawString(font, Integer.toString(index + 1), x + 2, y + 2, 0xFFBFC7D5, false);

        if (skillId == null) {
            graphics.drawCenteredString(font, "-", x + SLOT / 2, y + 12, 0xFF6A6F7B);
            return;
        }

        String label = SkillDefinitionRegistry.get(skillId)
                .map(SkillDefinition::displayName)
                .orElse(skillId.getPath());
        label = abbreviate(label);
        graphics.drawCenteredString(font, label, x + SLOT / 2, y + 13, 0xFFFFFFFF);
    }

    private static String abbreviate(String label) {
        if (label == null || label.isBlank()) {
            return "?";
        }
        String[] parts = label.trim().split("[ _-]+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(3, parts[0].length())).toUpperCase(java.util.Locale.ROOT);
        }
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                out.append(Character.toUpperCase(part.charAt(0)));
            }
            if (out.length() >= 3) {
                break;
            }
        }
        return out.toString();
    }
}
