package com.etema.ragnarmmo.jobs.client.ui;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.jobs.RagnarJobsAPI;
import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.core.config.RagnarCoreConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class ChangeClassScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 300;
    private static final int CARD_W = 126;
    private static final int CARD_H = 54;
    private static final ResourceLocation BASIC_SKILL = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "basic_skill");
    private static final List<JobType> FIRST_CLASSES = List.of(
            JobType.SWORDSMAN, JobType.ARCHER, JobType.ACOLYTE, JobType.THIEF, JobType.MERCHANT, JobType.MAGE);

    private final Screen parent;
    private JobType selected = JobType.SWORDSMAN;
    private Button confirmButton;
    private int panelX;
    private int panelY;

    public ChangeClassScreen(Screen parent) {
        this(parent, JobType.SWORDSMAN);
    }

    public ChangeClassScreen(Screen parent, JobType initialSelection) {
        super(Component.translatable("screen.ragnarmmo.job_selection.title"));
        this.parent = parent;
        if (initialSelection != null && JobType.FIRST_CLASSES.contains(initialSelection)) {
            this.selected = initialSelection;
        }
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - PANEL_HEIGHT) / 2;
        int bottom = panelY + PANEL_HEIGHT - 32;

        confirmButton = Button.builder(Component.translatable("screen.ragnarmmo.button.confirm"), button -> {
                    RagnarJobsAPI.requestChangeJob(selected);
                    Minecraft.getInstance().setScreen(parent);
                })
                .bounds(panelX + PANEL_WIDTH - 170, bottom, 74, 20)
                .build();
        addRenderableWidget(confirmButton);

        addRenderableWidget(Button.builder(Component.translatable("screen.ragnarmmo.button.back"), button -> Minecraft.getInstance().setScreen(parent))
                .bounds(panelX + PANEL_WIDTH - 88, bottom, 70, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        drawPanel(graphics, mouseX, mouseY);
        confirmButton.active = canConfirm();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xEE101014);
        graphics.renderOutline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFF4F5564);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 34, 0xEE242832);
        graphics.drawString(font, title, panelX + 14, panelY + 12, 0xFFE9D8A6, false);
        graphics.drawString(font, Component.translatable("screen.ragnarmmo.job_selection.subtitle"),
                panelX + 14, panelY + 24, 0xFFB8C8E8, false);

        int requiredJobLevel = RagnarCoreConfigs.SERVER.caps.noviceMaxJobLevel.get();
        JobType current = currentJob();
        int jobLevel = currentJobLevel();
        int basic = RagnarSkillsAPI.getLocalLevel(BASIC_SKILL);
        int skillPoints = skillPoints();

        int infoX = panelX + 16;
        int infoY = panelY + 50;
        graphics.drawString(font, Component.translatable("screen.ragnarmmo.job_selection.current",
                jobLabel(current), jobLevel), infoX, infoY, 0xFFFFFFFF, false);
        graphics.drawString(font, requirementLine(current == JobType.NOVICE, "screen.ragnarmmo.job_selection.novice"),
                infoX, infoY + 14,
                current == JobType.NOVICE ? 0xFF7CFF8A : 0xFFFF8A7C, false);
        graphics.drawString(font, requirementLine(jobLevel >= requiredJobLevel, "screen.ragnarmmo.job_selection.job_level", requiredJobLevel),
                infoX, infoY + 28, jobLevel >= requiredJobLevel ? 0xFF7CFF8A : 0xFFFF8A7C, false);
        graphics.drawString(font, requirementLine(basic >= 9, "screen.ragnarmmo.job_selection.basic_skill"),
                infoX + 146, infoY + 14, basic >= 9 ? 0xFF7CFF8A : 0xFFFF8A7C, false);
        graphics.drawString(font, requirementLine(skillPoints == 0, "screen.ragnarmmo.job_selection.no_points"),
                infoX + 146, infoY + 28, skillPoints == 0 ? 0xFF7CFF8A : 0xFFFF8A7C, false);

        if (!RagnarJobsAPI.hasChangeJobRequest()) {
            graphics.drawString(font, Component.translatable("screen.ragnarmmo.job_selection.unavailable"),
                    infoX, infoY + 46, 0xFFFFC06A, false);
        } else {
            graphics.drawString(font, Component.translatable("screen.ragnarmmo.job_selection.choose"),
                    infoX, infoY + 46, 0xFFB8C8E8, false);
        }

        int startX = panelX + 16;
        int startY = panelY + 116;
        for (int i = 0; i < FIRST_CLASSES.size(); i++) {
            JobType job = FIRST_CLASSES.get(i);
            int x = startX + (i % 3) * (CARD_W + 10);
            int y = startY + (i / 3) * (CARD_H + 10);
            drawJobCard(graphics, job, x, y, mouseX, mouseY);
        }
    }

    private void drawJobCard(GuiGraphics graphics, JobType job, int x, int y, int mouseX, int mouseY) {
        boolean selectedCard = selected == job;
        boolean hovered = mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H;
        int bg = selectedCard ? 0xFF243F67 : hovered ? 0xFF252C38 : 0xFF191D25;
        int border = selectedCard ? 0xFF78AAFF : hovered ? 0xFFE9D8A6 : 0xFF4F5564;
        graphics.fill(x, y, x + CARD_W, y + CARD_H, bg);
        graphics.renderOutline(x, y, CARD_W, CARD_H, border);
        graphics.drawString(font, jobLabel(job), x + 8, y + 8, selectedCard ? 0xFFFFFFFF : 0xFFE5E5E5, false);
        graphics.drawString(font, Component.translatable("job.ragnarmmo." + job.getId() + ".role"),
                x + 8, y + 22, 0xFFB6C7DE, false);
        graphics.drawString(font, Component.translatable("job.ragnarmmo." + job.getId() + ".primary"),
                x + 8, y + 36, 0xFF9ED79B, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = panelX + 16;
        int startY = panelY + 116;
        for (int i = 0; i < FIRST_CLASSES.size(); i++) {
            int x = startX + (i % 3) * (CARD_W + 10);
            int y = startY + (i / 3) * (CARD_H + 10);
            if (mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H) {
                selected = FIRST_CLASSES.get(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean canConfirm() {
        int requiredJobLevel = RagnarCoreConfigs.SERVER.caps.noviceMaxJobLevel.get();
        return RagnarJobsAPI.hasChangeJobRequest()
                && currentJob() == JobType.NOVICE
                && currentJobLevel() >= requiredJobLevel
                && RagnarSkillsAPI.getLocalLevel(BASIC_SKILL) >= 9
                && skillPoints() == 0;
    }

    private JobType currentJob() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return JobType.NOVICE;
        }
        return RagnarCoreAPI.get(minecraft.player)
                .map(stats -> JobType.fromId(stats.getJobId()))
                .orElse(JobType.NOVICE);
    }

    private int currentJobLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 1;
        }
        return RagnarCoreAPI.get(minecraft.player)
                .map(com.etema.ragnarmmo.common.api.stats.IPlayerStats::getJobLevel)
                .orElse(1);
    }

    private int skillPoints() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }
        return RagnarCoreAPI.get(minecraft.player)
                .map(com.etema.ragnarmmo.common.api.stats.IPlayerStats::getSkillPoints)
                .orElse(0);
    }

    private static String requirementLine(boolean passed, String key) {
        return (passed ? "[OK] " : "[--] ") + Component.translatable(key).getString();
    }

    private static String requirementLine(boolean passed, String key, int value) {
        return (passed ? "[OK] " : "[--] ") + Component.translatable(key, value).getString();
    }

    private static Component jobLabel(JobType job) {
        return Component.translatable("job.ragnarmmo." + (job == null ? "novice" : job.getId()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
