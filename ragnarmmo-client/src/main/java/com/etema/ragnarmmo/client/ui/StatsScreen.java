package com.etema.ragnarmmo.client.ui;

import com.etema.ragnarmmo.client.DerivedStatsClientCache;
import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.core.config.RagnarCoreConfigs;
import com.etema.ragnarmmo.player.progression.PlayerProgressionService;
import com.etema.ragnarmmo.player.stats.network.AllocateStatPacket;
import com.etema.ragnarmmo.player.stats.network.DeallocateStatPacket;
import com.etema.ragnarmmo.player.stats.progression.StatCost;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsScreen extends Screen {
    private static final StatKeys[] BASE_STATS = {
            StatKeys.STR, StatKeys.AGI, StatKeys.VIT, StatKeys.INT, StatKeys.DEX, StatKeys.LUK
    };

    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 320;
    private static final int HEADER_HEIGHT = 35;
    private static final int FOOTER_HEIGHT = 40;
    private static final int BTN_SIZE_SMALL = 14;

    private static final Rect BTN_CLOSE = new Rect(PANEL_WIDTH - 28, 6, 18, 18);
    private static final Rect BTN_SKILLS = new Rect(PANEL_WIDTH - 80, 6, 50, 18);
    private static final Rect BTN_CHANGE_CLASS = new Rect(PANEL_WIDTH - 164, 6, 80, 18);
    private static final Rect BTN_RESET_STATS = new Rect(PANEL_WIDTH - 90, PANEL_HEIGHT - FOOTER_HEIGHT + 10, 80, 18);

    private final Map<StatKeys, Rect> plusRect = new EnumMap<>(StatKeys.class);
    private final Map<StatKeys, Rect> minusRect = new EnumMap<>(StatKeys.class);
    private final Map<StatKeys, Integer> statRowY = new EnumMap<>(StatKeys.class);

    private float uiScale = 1.0F;
    private int panelX;
    private int panelY;
    private List<Component> deferredTooltip;

    public StatsScreen() {
        super(Component.translatable("screen.ragnarmmo.title"));
    }

    @Override
    protected void init() {
        recalcPanelTransform();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        recalcPanelTransform();
    }

    private void recalcPanelTransform() {
        float sx = (float) this.width / (float) PANEL_WIDTH;
        float sy = (float) this.height / (float) PANEL_HEIGHT;
        this.uiScale = Math.min(1.0F, Math.min(sx, sy));
        int scaledW = Math.round(PANEL_WIDTH * uiScale);
        int scaledH = Math.round(PANEL_HEIGHT * uiScale);
        this.panelX = (this.width - scaledW) / 2;
        this.panelY = (this.height - scaledH) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        this.renderBackground(graphics);
        this.deferredTooltip = null;

        double mx = (mouseX - panelX) / uiScale;
        double my = (mouseY - panelY) / uiScale;

        graphics.pose().pushPose();
        graphics.pose().translate(panelX, panelY, 0);
        graphics.pose().scale(uiScale, uiScale, 1.0F);

        drawPanel(graphics);
        graphics.drawString(minecraft.font, Component.translatable("screen.ragnarmmo.title"), 16, 12, 0xFFFFAA00, true);

        if (minecraft.player == null) {
            drawMissingStats(graphics);
        } else {
            RagnarCoreAPI.get(minecraft.player).ifPresentOrElse(
                    stats -> drawStatsContent(graphics, minecraft, stats, mx, my),
                    () -> drawMissingStats(graphics));
        }

        drawHeaderButtons(graphics, minecraft, mx, my);
        graphics.pose().popPose();

        if (deferredTooltip != null && !deferredTooltip.isEmpty()) {
            graphics.renderComponentTooltip(this.font, deferredTooltip, mouseX, mouseY);
        }
    }

    private void drawPanel(GuiGraphics graphics) {
        graphics.fill(0, 0, PANEL_WIDTH, PANEL_HEIGHT, GuiConstants.COLOR_PANEL_BG);
        graphics.renderOutline(0, 0, PANEL_WIDTH, PANEL_HEIGHT, GuiConstants.COLOR_PANEL_BORDER);
        graphics.fill(0, 0, PANEL_WIDTH, HEADER_HEIGHT, 0x66242832);
        graphics.fill(0, PANEL_HEIGHT - FOOTER_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT, 0x44111117);
    }

    private void drawMissingStats(GuiGraphics graphics) {
        graphics.drawCenteredString(this.font, Component.translatable("commands.ragnarmmo.player.missing_stats"),
                PANEL_WIDTH / 2, PANEL_HEIGHT / 2 - 4, 0xFFFF9B8F);
    }

    private void drawStatsContent(GuiGraphics graphics, Minecraft minecraft, IPlayerStats stats, double mx, double my) {
        ResourceLocation jobId = ResourceLocation.tryParse(stats.getJobId());
        PlayerProgressionService progression = PlayerProgressionService.forJobId(jobId);

        Component levelText = Component.translatable("screen.ragnarmmo.stats.level_format",
                stats.getLevel(), stats.getExp(), progression.baseExpToNext(stats.getLevel()));
        graphics.drawString(minecraft.font, levelText, 19, 28, 0xFFFFFFFF, false);

        int contentStartY = HEADER_HEIGHT + 17;
        int leftX = 20;
        int statsY = contentStartY;
        int maxStat = RagnarCoreConfigs.SERVER.caps.maxStatValue.get();

        graphics.drawString(minecraft.font, Component.translatable("screen.ragnarmmo.stats.base_title"),
                leftX, statsY, 0xFFFFAA00, true);
        statsY += 16;
        graphics.drawString(minecraft.font, Component.translatable("screen.ragnarmmo.stats.points",
                stats.getStatPoints(), stats.getSkillPoints()), leftX, statsY, 0xFF00FF00, false);
        statsY += 30;

        plusRect.clear();
        minusRect.clear();
        statRowY.clear();

        for (StatKeys key : BASE_STATS) {
            statRowY.put(key, statsY);
            drawBaseStatRow(graphics, minecraft, stats, key, leftX, statsY, maxStat, mx, my);
            statsY += 30;
        }

        drawDerivedColumn(graphics, minecraft, stats);
        drawFooter(graphics, minecraft, stats, progression, mx, my);
    }

    private void drawBaseStatRow(GuiGraphics graphics, Minecraft minecraft, IPlayerStats stats, StatKeys key,
                                 int leftX, int y, int maxStat, double mx, double my) {
        int value = getStatValue(stats, key);
        int bonus = stats.getBonus(key);
        int cost = value >= maxStat ? 0 : StatCost.costToIncrease(value);
        boolean canSpend = value < maxStat && stats.getStatPoints() >= cost;
        boolean canRefund = value > 1;

        graphics.fill(leftX - 2, y - 2, leftX + 205, y + 12, 0x44000000);
        Component statName = getStatName(key);
        graphics.drawString(minecraft.font, statName.copy().append(":"), leftX, y, 0xFFCCCCCC, false);
        graphics.drawString(minecraft.font, Integer.toString(value), leftX + 75, y, 0xFFFFFFFF, true);

        if (bonus != 0) {
            String bonusText = (bonus > 0 ? "+" : "") + bonus;
            graphics.drawString(minecraft.font, bonusText, leftX + 92, y, bonus > 0 ? 0xFF00FF00 : 0xFFFF5555, true);
            if (new Rect(leftX, y - 2, 120, 12).contains(mx, my)) {
                deferredTooltip = List.of(
                        Component.translatable("screen.ragnarmmo.stats.total", value + bonus).withStyle(ChatFormatting.GOLD),
                        Component.translatable("screen.ragnarmmo.stats.base", value).withStyle(ChatFormatting.WHITE),
                        Component.translatable("screen.ragnarmmo.stats.buff_bonus", bonus).withStyle(ChatFormatting.AQUA));
            }
        }

        graphics.pose().pushPose();
        graphics.pose().translate(leftX + 118, y + 2, 0);
        graphics.pose().scale(0.7F, 0.7F, 1.0F);
        if (value < maxStat) {
            graphics.drawString(minecraft.font, String.format(Locale.ROOT, "(%d pts)", cost), 0, 0, 0xFF888888, false);
        } else {
            graphics.drawString(minecraft.font, Component.translatable("screen.ragnarmmo.stats.max"), 0, 0, 0xFFFFD700, false);
        }
        graphics.pose().popPose();

        int buttonX = leftX + 155;
        Rect minus = new Rect(buttonX, y - 1, BTN_SIZE_SMALL, BTN_SIZE_SMALL);
        Rect plus = new Rect(buttonX + 16, y - 1, BTN_SIZE_SMALL, BTN_SIZE_SMALL);
        minusRect.put(key, minus);
        plusRect.put(key, plus);

        drawSmallButton(graphics, minus, "-", minus.contains(mx, my), canRefund);
        drawSmallButton(graphics, plus, "+", plus.contains(mx, my), canSpend);

        if (plus.contains(mx, my) && !canSpend) {
            deferredTooltip = List.of(statName.copy().withStyle(ChatFormatting.YELLOW),
                    createTooltipLine(value, maxStat, cost));
        }
    }

    private void drawDerivedColumn(GuiGraphics graphics, Minecraft minecraft, IPlayerStats stats) {
        int rightX = PANEL_WIDTH / 2 + 40;
        int yStr = statRowY.getOrDefault(StatKeys.STR, HEADER_HEIGHT + 48);
        int yLuk = statRowY.getOrDefault(StatKeys.LUK, yStr + 150);
        int span = Math.max(1, yLuk - yStr);
        DerivedStats derived = DerivedStatsClientCache.get();
        int[] y = weightedLines(yStr, span);

        graphics.pose().pushPose();
        graphics.pose().translate(rightX, 0, 0);
        graphics.drawString(minecraft.font, Component.translatable("screen.ragnarmmo.derived.offense_title"),
                0, y[0], 0xFFFF8800, true);
        renderDerivedStat(graphics, 0, y[1], "ATK", fmtRange(derived.physicalAttackMin, derived.physicalAttackMax), 0xFFFFFFFF);
        renderDerivedStat(graphics, 0, y[2], "MATK", fmtRange(derived.magicAttackMin, derived.magicAttackMax), 0xFFFFFFFF);
        renderDerivedStat(graphics, 0, y[3], "HIT", whole(derived.accuracy), 0xFFFFFFFF);
        renderDerivedStat(graphics, 0, y[4], "CRIT", percent(derived.criticalChance), 0xFFFFFFFF);
        renderDerivedStat(graphics, 0, y[5], "ASPD", whole(derived.attackSpeed), 0xFFFFFFFF);

        graphics.drawString(minecraft.font, Component.translatable("screen.ragnarmmo.derived.defense_title"),
                0, y[6], 0xFF4488FF, true);
        renderDerivedStat(graphics, 0, y[7], "DEF", one(derived.defense), 0xFFFFFFFF);
        renderDerivedStat(graphics, 0, y[8], "MDEF", one(derived.magicDefense), 0xFFFFFFFF);
        renderDerivedStat(graphics, 0, y[9], "HP", minecraft.player == null ? "--" : whole(minecraft.player.getMaxHealth()), 0xFFFF5555);
        renderDerivedStat(graphics, 0, y[10], "SP", whole(stats.getMaxResource()), 0xFF5555FF);
        renderDerivedStat(graphics, 0, y[11], "FLEE", whole(derived.flee), 0xFFFFFFFF);
        renderDerivedStat(graphics, 0, y[12], "P.DODGE", percent(derived.perfectDodge), 0xFFFFFFFF);
        graphics.pose().popPose();
    }

    private static int[] weightedLines(int startY, int span) {
        int lines = 13;
        float[] weights = new float[lines - 1];
        java.util.Arrays.fill(weights, 1.0F);
        weights[0] = 1.6F;
        weights[5] = 1.9F;
        weights[6] = 1.6F;

        float total = 0.0F;
        for (float weight : weights) {
            total += weight;
        }

        int[] y = new int[lines];
        float acc = 0.0F;
        y[0] = startY;
        for (int i = 1; i < lines; i++) {
            acc += weights[i - 1] * span / total;
            y[i] = Math.round(startY + acc);
        }
        return y;
    }

    private void drawFooter(GuiGraphics graphics, Minecraft minecraft, IPlayerStats stats,
                            PlayerProgressionService progression, double mx, double my) {
        int footerY = PANEL_HEIGHT - 22;
        Component jobName = Component.translatable("job.ragnarmmo." + jobPath(stats.getJobId()));
        Component jobText = Component.translatable("screen.ragnarmmo.stats.job_format",
                jobName, stats.getJobLevel(), stats.getJobExp(), progression.jobExpToNext(stats.getJobLevel()));
        graphics.drawString(minecraft.font, jobText, 16, footerY, 0xFF99CCFF, false);

        boolean hoverReset = BTN_RESET_STATS.contains(mx, my);
        drawButton(graphics, BTN_RESET_STATS, Component.translatable("screen.ragnarmmo.button.reset_stats"), hoverReset, false);
        if (hoverReset) {
            deferredTooltip = List.of(Component.literal("Reset packet is pending modular port.").withStyle(ChatFormatting.GRAY));
        }
    }

    private void drawHeaderButtons(GuiGraphics graphics, Minecraft minecraft, double mx, double my) {
        boolean hoverSkills = BTN_SKILLS.contains(mx, my);
        drawButton(graphics, BTN_SKILLS, Component.translatable("screen.ragnarmmo.button.skills"), hoverSkills, true);

        if (minecraft.player != null) {
            RagnarCoreAPI.get(minecraft.player).ifPresent(stats -> {
                if (JobType.fromId(stats.getJobId()).hasPromotions()) {
                    drawButton(graphics, BTN_CHANGE_CLASS, Component.translatable("screen.ragnarmmo.button.change_class"),
                            BTN_CHANGE_CLASS.contains(mx, my), true);
                }
            });
        }

        drawSmallButton(graphics, BTN_CLOSE, "x", BTN_CLOSE.contains(mx, my), true);
    }

    private void drawButton(GuiGraphics graphics, Rect rect, Component label, boolean hovered, boolean enabled) {
        int bg = !enabled ? 0xFF2A2A2A : hovered ? 0xFF505050 : 0xFF3A3A3A;
        int border = hovered ? 0xFFFFAA00 : GuiConstants.COLOR_PANEL_BORDER;
        int text = enabled ? (hovered ? 0xFFFFFFFF : 0xFFDDDDDD) : 0xFF777777;
        graphics.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, bg);
        graphics.renderOutline(rect.x, rect.y, rect.w, rect.h, border);
        int tx = rect.x + (rect.w - this.font.width(label)) / 2;
        int ty = rect.y + (rect.h - 8) / 2;
        graphics.drawString(this.font, label, tx, ty, text, false);
    }

    private void drawSmallButton(GuiGraphics graphics, Rect rect, String label, boolean hovered, boolean enabled) {
        int bg = !enabled ? 0xFF232323 : hovered ? 0xFF505050 : 0xFF3A3A3A;
        int border = hovered ? 0xFFFFAA00 : GuiConstants.COLOR_PANEL_BORDER;
        int text = enabled ? (hovered ? 0xFFFFFFFF : 0xFFDDDDDD) : 0xFF777777;
        graphics.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, bg);
        graphics.renderOutline(rect.x, rect.y, rect.w, rect.h, border);
        int tx = rect.x + (rect.w - this.font.width(label)) / 2;
        int ty = rect.y + (rect.h - 8) / 2;
        graphics.drawString(this.font, label, tx, ty, text, false);
    }

    private void renderDerivedStat(GuiGraphics graphics, int x, int y, String label, String value, int valueColor) {
        graphics.drawString(this.font, label + ":", x, y, 0xFFAAAAAA, false);
        graphics.drawString(this.font, value, x + 45, y, valueColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double mx = (mouseX - panelX) / uiScale;
        double my = (mouseY - panelY) / uiScale;
        Minecraft minecraft = Minecraft.getInstance();

        if (BTN_CLOSE.contains(mx, my)) {
            minecraft.setScreen(null);
            playClickSound(1.0F);
            return true;
        }
        if (BTN_SKILLS.contains(mx, my)) {
            minecraft.setScreen(new SkillsScreen(this));
            playClickSound(1.0F);
            return true;
        }
        if (BTN_CHANGE_CLASS.contains(mx, my) && minecraft.player != null) {
            var statsOpt = RagnarCoreAPI.get(minecraft.player);
            if (statsOpt.map(stats -> JobType.fromId(stats.getJobId()).hasPromotions()).orElse(false)) {
                minecraft.setScreen(new ChangeClassScreen(this));
                playClickSound(1.0F);
                return true;
            }
        }

        if (minecraft.player != null) {
            var statsOpt = RagnarCoreAPI.get(minecraft.player);
            if (statsOpt.isPresent()) {
                IPlayerStats stats = statsOpt.get();
                for (StatKeys key : BASE_STATS) {
                    Rect minus = minusRect.get(key);
                    Rect plus = plusRect.get(key);
                    if (minus == null || plus == null) {
                        continue;
                    }

                    int value = getStatValue(stats, key);
                    int maxStat = RagnarCoreConfigs.SERVER.caps.maxStatValue.get();
                    int cost = value >= maxStat ? 0 : StatCost.costToIncrease(value);
                    int amount = Screen.hasControlDown() ? 10 : 1;

                    if (minus.contains(mx, my)) {
                        if (value > 1) {
                            Network.sendToServer(new DeallocateStatPacket(key, amount));
                            playClickSound(0.9F);
                        }
                        return true;
                    }
                    if (plus.contains(mx, my)) {
                        if (value < maxStat && stats.getStatPoints() >= cost) {
                            Network.sendToServer(new AllocateStatPacket(key, amount));
                            playClickSound(1.0F);
                        }
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (com.etema.ragnarmmo.client.Keybinds.OPEN_STATS.matches(keyCode, scanCode) || keyCode == 256) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playClickSound(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    private Component createTooltipLine(int value, int maxStat, int cost) {
        if (value >= maxStat) {
            return Component.translatable("screen.ragnarmmo.tooltip.max").withStyle(ChatFormatting.RED);
        }
        return Component.translatable("screen.ragnarmmo.tooltip.points", cost).withStyle(ChatFormatting.GRAY);
    }

    private static int getStatValue(IPlayerStats stats, StatKeys key) {
        return switch (key) {
            case STR -> stats.getSTR();
            case AGI -> stats.getAGI();
            case VIT -> stats.getVIT();
            case INT -> stats.getINT();
            case DEX -> stats.getDEX();
            case LUK -> stats.getLUK();
            default -> throw new IllegalStateException("Unhandled StatKeys: " + key);
        };
    }

    private static Component getStatName(StatKeys key) {
        return Component.translatable("stat.ragnarmmo." + key.id());
    }

    private static String jobPath(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return "novice";
        }
        return jobId.contains(":") ? jobId.substring(jobId.indexOf(':') + 1) : jobId;
    }

    private static String fmtRange(double min, double max) {
        if (min <= 0.0D && max <= 0.0D) {
            return "0";
        }
        return one(min) + "-" + one(max);
    }

    private static String one(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String whole(double value) {
        return String.format(Locale.ROOT, "%.0f", value);
    }

    private static String percent(double value) {
        return String.format(Locale.ROOT, "%.1f%%", value * 100.0D);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class Rect {
        final int x;
        final int y;
        final int w;
        final int h;

        Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }
}
