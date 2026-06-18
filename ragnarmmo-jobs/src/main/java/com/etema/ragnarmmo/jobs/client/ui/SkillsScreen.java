package com.etema.ragnarmmo.jobs.client.ui;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.jobs.client.JobSkillsClientCache;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.lwjgl.glfw.GLFW;

public class SkillsScreen extends Screen {
    private static final int PANEL_WIDTH = 1040;
    private static final int PANEL_HEIGHT = 620;
    private static final int CELL_W = 148;
    private static final int CELL_H = 62;
    private static final int ICON = 24;
    private static final int BUTTON_W = 54;
    private static final int BUTTON_H = 18;

    private static final String[] TREE_FILES = {
            "novice_1", "swordsman_1", "archer_1", "acolyte_1", "thief_1", "mage_1", "merchant_1"
    };

    private final Screen parent;
    private final Map<String, SkillTree> trees = new LinkedHashMap<>();
    private final Map<ResourceLocation, SkillDefinition> skills = new LinkedHashMap<>();
    private float uiScale = 1.0F;
    private int panelX;
    private int panelY;
    private JobType previewFirstClass = JobType.MERCHANT;
    private List<Component> deferredTooltip;

    public SkillsScreen(Screen parent) {
        super(Component.translatable("gui.ragnarmmo.skills.title"));
        this.parent = parent;
        loadData();
    }

    @Override
    protected void init() {
        recalcPanelTransform();
        JobType current = currentJob();
        JobType first = current.getFirstClassAncestor();
        if (current.getTier() == 1) {
            first = current;
        }
        if (first != null) {
            previewFirstClass = first;
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        recalcPanelTransform();
    }

    private void recalcPanelTransform() {
        this.uiScale = Math.min(1.0F, Math.min((float) width / PANEL_WIDTH, (float) height / PANEL_HEIGHT));
        int scaledW = Math.round(PANEL_WIDTH * uiScale);
        int scaledH = Math.round(PANEL_HEIGHT * uiScale);
        this.panelX = (width - scaledW) / 2;
        this.panelY = (height - scaledH) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        deferredTooltip = null;
        double mx = (mouseX - panelX) / uiScale;
        double my = (mouseY - panelY) / uiScale;

        graphics.pose().pushPose();
        graphics.pose().translate(panelX, panelY, 0);
        graphics.pose().scale(uiScale, uiScale, 1.0F);

        graphics.fill(0, 0, PANEL_WIDTH, PANEL_HEIGHT, 0xF8F7F7F4);
        graphics.renderOutline(0, 0, PANEL_WIDTH, PANEL_HEIGHT, 0xFFB8B8B8);
        drawHeader(graphics, mx, my);

        JobType current = currentJob();
        JobType firstClass = firstClassFor(current);
        SkillTree novice = trees.get("novice_1");
        SkillTree first = trees.get(treeKey(firstClass));

        drawSection(graphics, jobLabel(firstClass), 8, 34, 278, novice, first, mx, my);

        drawFooter(graphics);
        graphics.pose().popPose();

        if (deferredTooltip != null && !deferredTooltip.isEmpty()) {
            graphics.renderComponentTooltip(font, deferredTooltip, mouseX, mouseY);
        }
    }

    private void drawHeader(GuiGraphics graphics, double mx, double my) {
        graphics.drawString(font, Component.translatable("screen.ragnarmmo.skills.header"), 10, 8, 0xFF222222, false);
        int x = 118;
        for (JobType job : JobType.FIRST_CLASSES) {
            Rect r = new Rect(x, 6, 78, 18);
            boolean selected = job == previewFirstClass;
            int bg = selected ? 0xFFD8E7FF : r.contains(mx, my) ? 0xFFE8EEF8 : 0xFFF7F7F7;
            graphics.fill(r.x, r.y, r.x + r.w, r.y + r.h, bg);
            graphics.renderOutline(r.x, r.y, r.w, r.h, selected ? 0xFF6E9AD8 : 0xFFCDCDCD);
            graphics.drawCenteredString(font, jobLabel(job), r.x + r.w / 2, r.y + 5,
                    selected ? 0xFF005CFF : 0xFF333333);
            x += 82;
        }
        drawSmallButton(graphics, new Rect(PANEL_WIDTH - 60, 6, 48, 18),
                Component.translatable("screen.ragnarmmo.button.close"), true);
    }

    private void drawSection(GuiGraphics graphics, Component title, int x, int y, int height,
                             SkillTree novice, SkillTree primary, double mx, double my) {
        graphics.drawString(font, title, x, y, 0xFF111111, false);
        graphics.hLine(x, PANEL_WIDTH - 10, y + 24, 0xFFD7D7D7);
        int gridY = y + 36;

        if (novice != null) {
            renderTree(graphics, novice, x + 6, gridY, 0, 0, mx, my, true);
        }
        if (primary != null) {
            int offsetX = novice != null ? 2 : 0;
            renderTree(graphics, primary, x + 6, gridY, offsetX, 0, mx, my, true);
            int total = totalMax(primary) + (novice == null ? 0 : totalMax(novice));
            int spent = totalSpent(primary) + (novice == null ? 0 : totalSpent(novice));
            graphics.drawString(font, spent + " / " + total, x, y + height - 16, 0xFF333333, false);
        }
    }

    private void renderTree(GuiGraphics graphics, SkillTree tree, int originX, int originY,
                            int offsetX, int offsetY, double mx, double my, boolean enabled) {
        for (SkillNode node : tree.skills) {
            SkillDefinition definition = skills.get(node.id);
            int cellX = originX + (node.x + offsetX) * CELL_W;
            int cellY = originY + (node.y + offsetY) * CELL_H;
            renderSkill(graphics, definition, node.id, cellX, cellY, mx, my, enabled);
        }
    }

    private void renderSkill(GuiGraphics graphics, SkillDefinition definition, ResourceLocation id,
                             int x, int y, double mx, double my, boolean enabled) {
        String name = definition == null ? id.getPath() : definition.displayName;
        int max = definition == null ? 1 : definition.maxLevel;
        ResourceLocation icon = iconFor(definition, id);

        Rect area = new Rect(x, y, 142, 48);
        boolean hovered = area.contains(mx, my);
        int titleBg = enabled ? 0xFFD8E7FF : 0xFFE8E8E8;
        graphics.fill(x, y, x + 146, y + 14, titleBg);
        graphics.renderOutline(x, y, 146, 14, 0xFFD2D2D2);
        graphics.drawCenteredString(font, clip(name, 20), x + 73, y + 3, enabled ? 0xFF0060FF : 0xFF888888);

        int iconX = x + 60;
        int iconY = y + 18;
        graphics.fill(iconX - 2, iconY - 2, iconX + ICON + 2, iconY + ICON + 2, 0xFFFFFFFF);
        graphics.renderOutline(iconX - 2, iconY - 2, ICON + 4, ICON + 4, 0xFFBFBFBF);
        graphics.blit(icon, iconX, iconY, 0, 0, ICON, ICON, ICON, ICON);
        int level = RagnarSkillsAPI.getLocalLevel(id);
        graphics.drawCenteredString(font, Integer.toString(level), iconX - 12, iconY + 8, 0xFF111111);
        graphics.drawCenteredString(font, Integer.toString(max), iconX + ICON + 12, iconY + 8, 0xFF111111);

        if (hovered) {
            deferredTooltip = tooltipFor(definition, id);
        }
    }

    private void drawFooter(GuiGraphics graphics) {
        JobType current = currentJob();
        graphics.hLine(0, PANEL_WIDTH, PANEL_HEIGHT - 34, 0xFFD7D7D7);
        graphics.drawString(font, Component.translatable("screen.ragnarmmo.skills.skill_points", skillPoints()),
                12, PANEL_HEIGHT - 22, 0xFF222222, false);
        if (current.hasPromotions()) {
            drawSmallButton(graphics, new Rect(PANEL_WIDTH - 218, PANEL_HEIGHT - 27, 88, BUTTON_H),
                    Component.translatable("screen.ragnarmmo.skills.change_class"), true);
        }
        drawSmallButton(graphics, new Rect(PANEL_WIDTH - 122, PANEL_HEIGHT - 27, BUTTON_W, BUTTON_H),
                Component.translatable("screen.ragnarmmo.skills.apply"), false);
        drawSmallButton(graphics, new Rect(PANEL_WIDTH - 62, PANEL_HEIGHT - 27, BUTTON_W, BUTTON_H),
                Component.translatable("screen.ragnarmmo.skills.reset"), false);
    }

    private void drawSmallButton(GuiGraphics graphics, Rect r, Component label, boolean enabled) {
        graphics.fill(r.x, r.y, r.x + r.w, r.y + r.h, enabled ? 0xFFF7F7F7 : 0xFFE5E5E5);
        graphics.renderOutline(r.x, r.y, r.w, r.h, 0xFFBFBFBF);
        graphics.drawCenteredString(font, label, r.x + r.w / 2, r.y + 5, enabled ? 0xFF222222 : 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double mx = (mouseX - panelX) / uiScale;
        double my = (mouseY - panelY) / uiScale;
        int x = 118;
        for (JobType job : JobType.FIRST_CLASSES) {
            if (new Rect(x, 6, 78, 18).contains(mx, my)) {
                previewFirstClass = job;
                playClick();
                return true;
            }
            x += 82;
        }
        if (new Rect(PANEL_WIDTH - 60, 6, 48, 18).contains(mx, my)) {
            Minecraft.getInstance().setScreen(parent);
            playClick();
            return true;
        }
        if (currentJob().hasPromotions() && new Rect(PANEL_WIDTH - 218, PANEL_HEIGHT - 27, 88, BUTTON_H).contains(mx, my)) {
            Minecraft.getInstance().setScreen(new ChangeClassScreen(this, previewFirstClass));
            playClick();
            return true;
        }
        ResourceLocation clicked = skillAt(mx, my);
        if (clicked != null && button == 0) {
            RagnarSkillsAPI.requestUpgrade(clicked);
            playClick();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_6) {
            assignHoveredSkillToHotbar(keyCode - GLFW.GLFW_KEY_1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void assignHoveredSkillToHotbar(int slot) {
        Minecraft minecraft = Minecraft.getInstance();
        double mx = minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth()
                / minecraft.getWindow().getScreenWidth();
        double my = minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight()
                / minecraft.getWindow().getScreenHeight();
        ResourceLocation hovered = skillAt((mx - panelX) / uiScale, (my - panelY) / uiScale);
        if (hovered == null) {
            return;
        }
        SkillDefinition definition = skills.get(hovered);
        if (definition == null || !"ACTIVE".equalsIgnoreCase(definition.usage)) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.translatable("screen.ragnarmmo.skills.active_only"), true);
            }
            return;
        }
        JobSkillsClientCache.requestSetHotbarSlot(slot, hovered);
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable(
                    "screen.ragnarmmo.skills.assigned_slot", Component.translatable(definition.displayName), slot + 1), true);
        }
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

    private JobType firstClassFor(JobType current) {
        if (current == JobType.NOVICE) {
            return previewFirstClass;
        }
        JobType ancestor = current.getFirstClassAncestor();
        return ancestor == null ? previewFirstClass : ancestor;
    }

    private int skillPoints() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }
        return RagnarCoreAPI.get(minecraft.player).map(com.etema.ragnarmmo.common.api.stats.IPlayerStats::getSkillPoints).orElse(0);
    }

    private static String treeKey(JobType job) {
        if (job == null || job == JobType.NOVICE) {
            return "novice_1";
        }
        String name = job.getId();
        int tier = job.getTier() == 2 ? 2 : 1;
        return name + "_" + tier;
    }

    private static Component jobLabel(JobType job) {
        return Component.translatable("job.ragnarmmo." + (job == null ? "novice" : job.getId()));
    }

    private ResourceLocation iconFor(SkillDefinition definition, ResourceLocation id) {
        String texture = definition == null ? "" : definition.texture;
        if (texture == null || texture.isBlank()) {
            texture = "common/common_basic_conditioning";
        }
        return ResourceLocation.fromNamespaceAndPath("ragnarmmo", "textures/gui/skills/" + texture + ".png");
    }

    private List<Component> tooltipFor(SkillDefinition definition, ResourceLocation id) {
        List<Component> lines = new ArrayList<>();
        String name = definition == null ? id.toString() : definition.displayName;
        lines.add(Component.literal(name).withStyle(ChatFormatting.YELLOW));
        lines.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
        if (definition != null) {
            int level = RagnarSkillsAPI.getLocalLevel(id);
            lines.add(Component.translatable("screen.ragnarmmo.skills.level_progress", level, definition.maxLevel)
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("screen.ragnarmmo.skills.usage_category",
                    displayType(definition.usage), displayType(definition.category)).withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("skill.ragnarmmo." + id.getPath() + ".desc").withStyle(ChatFormatting.WHITE));
            lines.add(Component.translatable("screen.ragnarmmo.skills.left_click").withStyle(ChatFormatting.GREEN));
        }
        return lines;
    }

    private void loadData() {
        if (!trees.isEmpty()) {
            return;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (String file : TREE_FILES) {
            SkillTree tree = readTree(loader, file);
            if (tree != null) {
                trees.put(file, tree);
            }
        }
        readSkills(loader);
    }

    private static SkillTree readTree(ClassLoader loader, String file) {
        String path = "data/ragnarmmo/skill_trees/" + file + ".json";
        try (var in = loader.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            SkillTree tree = new SkillTree();
            tree.job = root.has("job") ? root.get("job").getAsString() : "";
            tree.tier = root.has("tier") ? root.get("tier").getAsInt() : 1;
            List<SkillNode> nodes = new ArrayList<>();
            if (root.has("skills") && root.get("skills").isJsonArray()) {
                for (var element : root.getAsJsonArray("skills")) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject skill = element.getAsJsonObject();
                    if (!skill.has("id")) {
                        continue;
                    }
                    ResourceLocation id = ResourceLocation.tryParse(skill.get("id").getAsString());
                    if (id == null) {
                        continue;
                    }
                    SkillNode node = new SkillNode();
                    node.id = id;
                    node.x = skill.has("x") ? skill.get("x").getAsInt() : 0;
                    node.y = skill.has("y") ? skill.get("y").getAsInt() : 0;
                    nodes.add(node);
                }
            }
            tree.skills = nodes;
            return tree;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void readSkills(ClassLoader loader) {
        for (SkillTree tree : trees.values()) {
            for (SkillNode node : tree.skills) {
                if (!skills.containsKey(node.id)) {
                    SkillDefinition definition = readSkill(loader, node.id);
                    if (definition != null) {
                        skills.put(node.id, definition);
                    }
                }
            }
        }
    }

    private static SkillDefinition readSkill(ClassLoader loader, ResourceLocation id) {
        String path = "data/" + id.getNamespace() + "/skills/" + id.getPath() + ".json";
        try (var in = loader.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            String display = root.has("display_name") ? root.get("display_name").getAsString() : titleize(id.getPath());
            String category = root.has("category") ? root.get("category").getAsString() : "";
            String usage = root.has("usage") ? root.get("usage").getAsString() : "";
            int max = 1;
            if (root.has("progression") && root.get("progression").isJsonObject()) {
                JsonObject progression = root.getAsJsonObject("progression");
                if (progression.has("max_level")) {
                    max = progression.get("max_level").getAsInt();
                }
            }
            String texture = "";
            if (root.has("ui") && root.get("ui").isJsonObject()) {
                JsonObject ui = root.getAsJsonObject("ui");
                if (ui.has("texture")) {
                    texture = ui.get("texture").getAsString();
                }
            }
            return new SkillDefinition(display, category, usage, max, texture);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String titleize(String id) {
        String[] parts = id.split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return out.toString();
    }

    private static String clip(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, Math.max(0, max - 2)) + "..";
    }

    private static String displayType(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Component.translatable(value).getString();
    }

    private int totalMax(SkillTree tree) {
        int total = 0;
        for (SkillNode node : tree.skills) {
            SkillDefinition definition = skills.get(node.id);
            total += definition == null ? 1 : definition.maxLevel;
        }
        return total;
    }

    private int totalSpent(SkillTree tree) {
        int total = 0;
        for (SkillNode node : tree.skills) {
            total += RagnarSkillsAPI.getLocalLevel(node.id);
        }
        return total;
    }

    private ResourceLocation skillAt(double mx, double my) {
        JobType current = currentJob();
        JobType firstClass = firstClassFor(current);
        SkillTree novice = trees.get("novice_1");
        SkillTree first = trees.get(treeKey(firstClass));

        ResourceLocation hit = skillAtTree(novice, 14, 70, 0, 0, mx, my);
        if (hit != null) {
            return hit;
        }
        hit = skillAtTree(first, 14, 70, novice != null ? 2 : 0, 0, mx, my);
        if (hit != null) {
            return hit;
        }
        return null;
    }

    private ResourceLocation skillAtTree(SkillTree tree, int originX, int originY,
                                         int offsetX, int offsetY, double mx, double my) {
        if (tree == null) {
            return null;
        }
        for (SkillNode node : tree.skills) {
            int cellX = originX + (node.x + offsetX) * CELL_W;
            int cellY = originY + (node.y + offsetY) * CELL_H;
            if (new Rect(cellX, cellY, 142, 48).contains(mx, my)) {
                return node.id;
            }
        }
        return null;
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance
                .forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class SkillTree {
        String job;
        int tier;
        List<SkillNode> skills = List.of();
    }

    private static final class SkillNode {
        ResourceLocation id;
        int x;
        int y;
    }

    private record SkillDefinition(String displayName, String category, String usage, int maxLevel, String texture) {
    }

    private record Rect(int x, int y, int w, int h) {
        boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }
}
