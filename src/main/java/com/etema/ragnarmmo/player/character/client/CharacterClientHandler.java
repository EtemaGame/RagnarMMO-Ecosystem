package com.etema.ragnarmmo.player.character.client;

import com.etema.ragnarmmo.player.character.data.CharacterSlot;
import com.etema.ragnarmmo.player.character.net.ClientboundCharacterActionResultPacket;
import com.etema.ragnarmmo.player.character.net.ClientboundCharacterListPacket;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class CharacterClientHandler {
    private static final List<CharacterSlot> SLOTS = new ArrayList<>();
    private static boolean required;
    private static String lastMessage = "";
    private static boolean lastSuccess = true;

    private CharacterClientHandler() {
    }

    public static void handleList(ClientboundCharacterListPacket packet) {
        SLOTS.clear();
        SLOTS.addAll(packet.slots());
        required = packet.selectionRequired();
        if (Minecraft.getInstance().screen instanceof CharacterSelectScreen screen) {
            screen.refreshFromState();
        }
    }

    public static void openSelect(boolean selectionRequired) {
        required = selectionRequired;
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof CharacterSelectScreen)) {
            minecraft.setScreen(new CharacterSelectScreen());
        }
    }

    public static void handleResult(ClientboundCharacterActionResultPacket packet) {
        lastMessage = packet.message();
        lastSuccess = packet.success();
        Minecraft minecraft = Minecraft.getInstance();
        if (packet.success() && !required && minecraft.screen instanceof CharacterSelectScreen) {
            minecraft.setScreen(null);
            return;
        }
        if (minecraft.screen instanceof CharacterSelectScreen screen) {
            screen.refreshFromState();
        }
        if (minecraft.player != null && !packet.message().isBlank()) {
            minecraft.player.displayClientMessage(Component.literal(packet.message()), true);
        }
    }

    public static List<CharacterSlot> slots() {
        return List.copyOf(SLOTS);
    }

    public static boolean required() {
        return required;
    }

    public static String lastMessage() {
        return lastMessage;
    }

    public static boolean lastSuccess() {
        return lastSuccess;
    }
}
