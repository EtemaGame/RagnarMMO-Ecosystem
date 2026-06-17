package com.etema.ragnarmmo.client.ui;

import com.etema.ragnarmmo.achievements.capability.IPlayerAchievements;
import com.etema.ragnarmmo.achievements.capability.PlayerAchievementsProvider;
import com.etema.ragnarmmo.achievements.data.AchievementCategory;
import com.etema.ragnarmmo.achievements.data.AchievementDefinition;
import com.etema.ragnarmmo.achievements.data.AchievementRegistry;
import com.etema.ragnarmmo.achievements.network.ClaimAchievementPacket;
import com.etema.ragnarmmo.achievements.network.SetTitlePacket;
import com.etema.ragnarmmo.common.net.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class AchievementsScreen extends Screen {
    private static final int PANEL_WIDTH = 480;
    private static final int PANEL_HEIGHT = 326;
    private static final int PAD = 8;
    private static final int HEADER_HEIGHT = 36;
    private static final int TAB_HEIGHT = 20;
    private static final int SEARCH_HEIGHT = 18;
    private static final int ITEM_HEIGHT = 48;

    private AchievementCategory selectedCategory = AchievementCategory.BASIC;
    private List<AchievementDefinition> visibleAchievements = List.of();
    private EditBox searchBox;
    private int left;
    private int top;
    private int listY;
    private int listHeight;
    private int scrollIndex;

    public AchievementsScreen() {
        super(Component.translatable("gui.ragnarmmo.achievements.title"));
    }

    @Override
    protected void init() {
        left = (this.width - PANEL_WIDTH) / 2;
        top = (this.height - PANEL_HEIGHT) / 2;
        listY = top + HEADER_HEIGHT + TAB_HEIGHT + SEARCH_HEIGHT + PAD * 2;
        listHeight = PANEL_HEIGHT - HEADER_HEIGHT - TAB_HEIGHT - SEARCH_HEIGHT - PAD * 3;

        rebuildAchievementWidgets();
        refreshList();
    }

    private void rebuildAchievementWidgets() {
        clearWidgets();

        int tabWidth = (PANEL_WIDTH - PAD * 2) / AchievementCategory.values().length;
        int tabY = top + HEADER_HEIGHT;
        AchievementCategory[] categories = AchievementCategory.values();
        for (int i = 0; i < categories.length; i++) {
            AchievementCategory category = categories[i];
            int tabX = left + PAD + i * tabWidth;
            addRenderableWidget(Button.builder(Component.translatable(category.getTranslationKey()), button -> {
                        selectedCategory = category;
                        scrollIndex = 0;
                        refreshList();
                    })
                    .bounds(tabX, tabY, tabWidth - 2, TAB_HEIGHT)
                    .build());
        }

        searchBox = new EditBox(this.font, left + PAD, top + HEADER_HEIGHT + TAB_HEIGHT + PAD,
                PANEL_WIDTH - PAD * 2 - 88, SEARCH_HEIGHT,
                Component.translatable("gui.ragnarmmo.achievements.search"));
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.translatable("gui.ragnarmmo.achievements.search"));
        searchBox.setResponder(value -> {
            scrollIndex = 0;
            refreshList();
        });
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.ragnarmmo.achievements.unequip"),
                        button -> Network.sendToServer(new SetTitlePacket("")))
                .bounds(left + PANEL_WIDTH - PAD - 78, top + HEADER_HEIGHT + TAB_HEIGHT + PAD, 78, SEARCH_HEIGHT)
                .build());
    }

    private void refreshList() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        visibleAchievements = AchievementRegistry.getInstance().getAll().values().stream()
                .filter(definition -> definition.category() == selectedCategory)
                .filter(definition -> query.isEmpty() || matches(definition, query))
                .sorted(Comparator.comparing(AchievementDefinition::id))
                .collect(Collectors.toList());
        scrollIndex = Mth.clamp(scrollIndex, 0, maxScroll());
    }

    private boolean matches(AchievementDefinition definition, String query) {
        return definition.id().toLowerCase(Locale.ROOT).contains(query)
                || display(definition.name()).toLowerCase(Locale.ROOT).contains(query)
                || display(definition.description()).toLowerCase(Locale.ROOT).contains(query)
                || (definition.title() != null
                && display(definition.title()).toLowerCase(Locale.ROOT).contains(query));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        drawPanel(graphics);
        drawHeader(graphics);
        drawList(graphics, mouseX, mouseY);
        drawScrollbar(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawPanel(GuiGraphics graphics) {
        graphics.fill(left + 4, top + 4, left + PANEL_WIDTH + 4, top + PANEL_HEIGHT + 4, 0x99000000);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xE0151520);
        graphics.fill(left, top, left + PANEL_WIDTH, top + HEADER_HEIGHT, 0xEE242832);
        graphics.renderOutline(left, top, PANEL_WIDTH, PANEL_HEIGHT, GuiConstants.COLOR_HUD_PANEL_BORDER_OUTER);
        graphics.renderOutline(left + 1, top + 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2,
                GuiConstants.COLOR_HUD_PANEL_BORDER_INNER);
    }

    private void drawHeader(GuiGraphics graphics) {
        graphics.drawString(font, this.title, left + PAD, top + PAD, 0xFFE9D8A6, false);
        localAchievements().ifPresent(cap -> {
            String points = cap.getTotalPoints() + " pts";
            graphics.drawString(font, points, left + PANEL_WIDTH - PAD - font.width(points), top + PAD,
                    0xFFFFC857, false);
            String activeTitle = cap.getActiveTitle();
            if (activeTitle != null && !activeTitle.isBlank()) {
                String text = "[" + display(activeTitle) + "]";
                text = font.plainSubstrByWidth(text, 190);
                graphics.drawCenteredString(font, text, left + PANEL_WIDTH / 2, top + PAD, 0xFF81D4FA);
            }
        });

        String category = Component.translatable(selectedCategory.getTranslationKey()).getString();
        graphics.drawString(font, category + " (" + visibleAchievements.size() + ")",
                left + PAD, listY - 11, GuiConstants.COLOR_TEXT_DIM, false);
    }

    private void drawList(GuiGraphics graphics, int mouseX, int mouseY) {
        int itemX = left + PAD;
        int itemWidth = PANEL_WIDTH - PAD * 2 - 8;
        int visibleRows = Math.max(1, listHeight / ITEM_HEIGHT);

        graphics.enableScissor(itemX, listY, itemX + itemWidth, listY + listHeight);
        Optional<IPlayerAchievements> maybeCap = localAchievements();
        if (maybeCap.isEmpty()) {
            drawCenteredListText(graphics, "No achievement data");
        } else if (visibleAchievements.isEmpty()) {
            drawCenteredListText(graphics, Component.translatable("gui.ragnarmmo.achievements.empty").getString());
        } else {
            IPlayerAchievements cap = maybeCap.get();
            for (int i = 0; i < visibleRows + 1; i++) {
                int index = scrollIndex + i;
                if (index >= visibleAchievements.size()) {
                    break;
                }
                int y = listY + i * ITEM_HEIGHT;
                boolean hovered = mouseX >= itemX && mouseX < itemX + itemWidth
                        && mouseY >= y && mouseY < y + ITEM_HEIGHT - 3;
                drawAchievement(graphics, visibleAchievements.get(index), cap, itemX, y, itemWidth, hovered);
            }
        }
        graphics.disableScissor();
    }

    private void drawAchievement(GuiGraphics graphics, AchievementDefinition definition, IPlayerAchievements cap,
                                 int x, int y, int width, boolean hovered) {
        boolean unlocked = cap.isUnlocked(definition.id());
        boolean claimed = cap.isClaimed(definition.id());
        int bg = hovered ? 0xE02A2F3B : 0xB01D2028;
        graphics.fill(x, y, x + width, y + ITEM_HEIGHT - 3, bg);
        graphics.renderOutline(x, y, width, ITEM_HEIGHT - 3, unlocked ? 0xFF5D8C65 : 0xFF404654);

        int titleColor = unlocked ? 0xFFFFFFFF : 0xFF9AA0AA;
        graphics.drawString(font, display(definition.name()), x + 6, y + 5, titleColor, false);

        String description = font.plainSubstrByWidth(display(definition.description()), width - 118);
        graphics.drawString(font, description, x + 6, y + 17, GuiConstants.COLOR_TEXT_DIM, false);

        int progress = cap.getProgress(definition.id());
        int required = Math.max(1, definition.requiredAmount());
        float ratio = unlocked ? 1.0F : Mth.clamp((float) progress / required, 0.0F, 1.0F);
        int barX = x + 6;
        int barY = y + 34;
        int barW = width - 118;
        graphics.fill(barX, barY, barX + barW, barY + 5, GuiConstants.COLOR_BAR_BG);
        graphics.fill(barX, barY, barX + Math.round(barW * ratio), barY + 5,
                unlocked ? GuiConstants.COLOR_XP_BAR : GuiConstants.COLOR_JOB_XP_BAR);

        String state = stateText(definition, cap, unlocked, claimed);
        graphics.drawString(font, state, x + width - 104, y + 7, stateColor(unlocked, claimed), false);
        if (definition.title() != null && !definition.title().isBlank()) {
            String title = font.plainSubstrByWidth(display(definition.title()), 98);
            graphics.drawString(font, title, x + width - 104, y + 21, 0xFF81D4FA, false);
        }
        graphics.drawString(font, definition.points() + " pts", x + width - 104, y + 34, 0xFFFFC857, false);
    }

    private String stateText(AchievementDefinition definition, IPlayerAchievements cap, boolean unlocked,
                             boolean claimed) {
        if (!unlocked) {
            return Component.translatable("gui.ragnarmmo.achievements.locked").getString();
        }
        if (!claimed) {
            return Component.translatable("gui.ragnarmmo.achievements.claimable").getString();
        }
        if (definition.title() != null && definition.title().equals(cap.getActiveTitle())) {
            return Component.translatable("gui.ragnarmmo.achievements.equipped").getString();
        }
        if (definition.title() != null && !definition.title().isBlank()) {
            return Component.translatable("gui.ragnarmmo.achievements.equip").getString();
        }
        return Component.translatable("gui.ragnarmmo.achievements.claimed").getString();
    }

    private int stateColor(boolean unlocked, boolean claimed) {
        if (!unlocked) {
            return 0xFF858B95;
        }
        return claimed ? 0xFF9BE39F : 0xFFFFD166;
    }

    private void drawCenteredListText(GuiGraphics graphics, String text) {
        graphics.drawCenteredString(font, text, left + PANEL_WIDTH / 2, listY + listHeight / 2 - 4,
                GuiConstants.COLOR_TEXT_DIM);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int maxScroll = maxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int x = left + PANEL_WIDTH - PAD - 5;
        graphics.fill(x, listY, x + 4, listY + listHeight, 0xAA101018);
        int thumbHeight = Math.max(18, listHeight * Math.max(1, listHeight / ITEM_HEIGHT) / visibleAchievements.size());
        int thumbRange = listHeight - thumbHeight;
        int thumbY = listY + Math.round((float) scrollIndex / maxScroll * thumbRange);
        graphics.fill(x, thumbY, x + 4, thumbY + thumbHeight, 0xFF666A80);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollIndex = Mth.clamp(scrollIndex - (int) Math.signum(delta), 0, maxScroll());
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= listY && mouseY < listY + listHeight) {
            int itemX = left + PAD;
            int itemWidth = PANEL_WIDTH - PAD * 2 - 8;
            if (mouseX >= itemX && mouseX < itemX + itemWidth) {
                int row = ((int) mouseY - listY) / ITEM_HEIGHT;
                int index = scrollIndex + row;
                if (index >= 0 && index < visibleAchievements.size()) {
                    localAchievements().ifPresent(cap -> handleClick(visibleAchievements.get(index), cap));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleClick(AchievementDefinition definition, IPlayerAchievements cap) {
        if (!cap.isUnlocked(definition.id())) {
            return;
        }
        if (!cap.isClaimed(definition.id())) {
            Network.sendToServer(new ClaimAchievementPacket(definition.id()));
            return;
        }
        if (definition.title() == null || definition.title().isBlank()) {
            return;
        }
        Network.sendToServer(new SetTitlePacket(
                definition.title().equals(cap.getActiveTitle()) ? "" : definition.title()));
    }

    private int maxScroll() {
        int visibleRows = Math.max(1, listHeight / ITEM_HEIGHT);
        return Math.max(0, visibleAchievements.size() - visibleRows);
    }

    private Optional<IPlayerAchievements> localAchievements() {
        if (Minecraft.getInstance().player == null) {
            return Optional.empty();
        }
        return Minecraft.getInstance().player.getCapability(PlayerAchievementsProvider.PLAYER_ACHIEVEMENTS).resolve();
    }

    private static String display(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Component.translatable(value).getString();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
