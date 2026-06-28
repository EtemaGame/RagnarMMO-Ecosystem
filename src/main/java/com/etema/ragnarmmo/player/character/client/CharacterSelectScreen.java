package com.etema.ragnarmmo.player.character.client;

import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.player.character.data.CharacterSlot;
import com.etema.ragnarmmo.player.character.net.ServerboundCreateCharacterPacket;
import com.etema.ragnarmmo.player.character.net.ServerboundDeleteCharacterPacket;
import com.etema.ragnarmmo.player.character.net.ServerboundSelectCharacterPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CharacterSelectScreen extends Screen {
    private final Map<Integer, CharacterSlot> slotsByIndex = new HashMap<>();
    private EditBox nameBox;
    private EditBox deleteConfirmBox;
    private int createSlot = -1;
    private UUID deleteId;

    public CharacterSelectScreen() {
        super(Component.literal("Character Select"));
    }

    @Override
    protected void init() {
        rebuild();
    }

    public void refreshFromState() {
        if (minecraft != null) {
            rebuild();
        }
    }

    private void rebuild() {
        clearWidgets();
        slotsByIndex.clear();
        CharacterClientHandler.slots().forEach(slot -> slotsByIndex.put(slot.slotIndex(), slot));
        int cardWidth = 180;
        int cardHeight = 112;
        int gap = 12;
        int totalWidth = cardWidth * 3 + gap * 2;
        int startX = (width - totalWidth) / 2;
        int y = Math.max(48, height / 2 - 92);
        for (int i = 0; i < 3; i++) {
            int slotIndex = i;
            int x = startX + i * (cardWidth + gap);
            CharacterSlot slot = slotsByIndex.get(i);
            if (slot == null) {
                addRenderableWidget(Button.builder(Component.literal("Create"), button -> beginCreate(slotIndex))
                        .bounds(x + 45, y + 70, 90, 20)
                        .build());
            } else {
                addRenderableWidget(Button.builder(Component.literal("Select"), button ->
                                Network.sendToServer(new ServerboundSelectCharacterPacket(slot.characterId())))
                        .bounds(x + 20, y + 70, 65, 20)
                        .build());
                addRenderableWidget(Button.builder(Component.literal("Delete"), button -> beginDelete(slot.characterId()))
                        .bounds(x + 95, y + 70, 65, 20)
                        .build());
            }
        }
        addCreateModal();
        addDeleteModal();
    }

    private void beginCreate(int slot) {
        createSlot = slot;
        deleteId = null;
        rebuild();
    }

    private void beginDelete(UUID id) {
        deleteId = id;
        createSlot = -1;
        rebuild();
    }

    private void addCreateModal() {
        if (createSlot < 0) {
            return;
        }
        int x = width / 2 - 120;
        int y = height - 88;
        nameBox = new EditBox(font, x, y, 160, 20, Component.literal("Name"));
        nameBox.setMaxLength(16);
        nameBox.setHint(Component.literal("Character name"));
        addRenderableWidget(nameBox);
        addRenderableWidget(Button.builder(Component.literal("Create Novice"), button ->
                        Network.sendToServer(new ServerboundCreateCharacterPacket(createSlot, nameBox.getValue())))
                .bounds(x + 166, y, 92, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
                    createSlot = -1;
                    rebuild();
                })
                .bounds(x + 264, y, 60, 20)
                .build());
    }

    private void addDeleteModal() {
        if (deleteId == null) {
            return;
        }
        CharacterSlot slot = CharacterClientHandler.slots().stream()
                .filter(candidate -> candidate.characterId().equals(deleteId))
                .findFirst()
                .orElse(null);
        if (slot == null) {
            deleteId = null;
            return;
        }
        int x = width / 2 - 150;
        int y = height - 88;
        deleteConfirmBox = new EditBox(font, x, y, 180, 20, Component.literal("Confirm delete"));
        deleteConfirmBox.setMaxLength(16);
        deleteConfirmBox.setHint(Component.literal("Type " + slot.name()));
        addRenderableWidget(deleteConfirmBox);
        addRenderableWidget(Button.builder(Component.literal("Delete"), button ->
                        Network.sendToServer(new ServerboundDeleteCharacterPacket(deleteId, deleteConfirmBox.getValue())))
                .bounds(x + 186, y, 60, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
                    deleteId = null;
                    rebuild();
                })
                .bounds(x + 252, y, 60, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Select a character to enter the world"), width / 2, 32, 0xA0A0A0);
        renderCards(graphics);
        if (!CharacterClientHandler.lastMessage().isBlank()) {
            graphics.drawCenteredString(font,
                    Component.literal(CharacterClientHandler.lastMessage()),
                    width / 2,
                    height - 28,
                    CharacterClientHandler.lastSuccess() ? 0x80FF80 : 0xFF8080);
        }
        if (createSlot >= 0) {
            graphics.drawCenteredString(font, Component.literal("Class: Novice"), width / 2, height - 112, 0xFFD27D);
        }
        if (deleteId != null) {
            graphics.drawCenteredString(font, Component.literal("Type the exact name to delete"), width / 2, height - 112, 0xFF8080);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCards(GuiGraphics graphics) {
        int cardWidth = 180;
        int cardHeight = 112;
        int gap = 12;
        int totalWidth = cardWidth * 3 + gap * 2;
        int startX = (width - totalWidth) / 2;
        int y = Math.max(48, height / 2 - 92);
        for (int i = 0; i < 3; i++) {
            int x = startX + i * (cardWidth + gap);
            graphics.fill(x, y, x + cardWidth, y + cardHeight, 0xCC101820);
            graphics.renderOutline(x, y, cardWidth, cardHeight, 0xFF5C718A);
            CharacterSlot slot = slotsByIndex.get(i);
            graphics.drawString(font, "Slot " + (i + 1), x + 10, y + 10, 0xFFD6E4F0, false);
            if (slot == null) {
                graphics.drawCenteredString(font, Component.literal("Empty"), x + cardWidth / 2, y + 40, 0xFF8EA0AE);
            } else {
                var summary = slot.summary();
                graphics.drawString(font, slot.name(), x + 10, y + 30, 0xFFFFFFFF, false);
                graphics.drawString(font, "Base Lv " + summary.baseLevel(), x + 10, y + 44, 0xFFE6D39A, false);
                graphics.drawString(font, summary.jobName() + " Job " + summary.jobLevel(), x + 10, y + 58, 0xFFB8D7FF, false);
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !CharacterClientHandler.required();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
