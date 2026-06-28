package com.etema.ragnarmmo.player.character.runtime;

import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.items.equipment.RagnarEquipmentSync;
import com.etema.ragnarmmo.jobs.net.JobSkillsSyncService;
import com.etema.ragnarmmo.lifeskills.LifeSkillCapability;
import com.etema.ragnarmmo.lifeskills.LifeSkillSyncPacket;
import com.etema.ragnarmmo.player.character.data.CharacterNameValidator;
import com.etema.ragnarmmo.player.character.data.CharacterSaveData;
import com.etema.ragnarmmo.player.character.data.CharacterSlot;
import com.etema.ragnarmmo.player.character.data.CharacterSummary;
import com.etema.ragnarmmo.player.character.net.CharacterNetwork;
import com.etema.ragnarmmo.player.character.net.ClientboundCharacterActionResultPacket;
import com.etema.ragnarmmo.player.character.net.ClientboundCharacterListPacket;
import com.etema.ragnarmmo.player.character.net.ClientboundOpenCharacterSelectPacket;
import com.etema.ragnarmmo.player.stats.capability.PlayerStatsProvider;
import com.etema.ragnarmmo.player.stats.compute.StatResolutionService;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CharacterSelectionService {
    private static final Map<UUID, CharacterSelectionState> STATES = new ConcurrentHashMap<>();

    private CharacterSelectionService() {
    }

    public static void requireSelection(ServerPlayer player) {
        state(player).selectionRequired(true);
        migrateIfNeeded(player);
        sendSelection(player, true);
    }

    public static void returnToSelection(ServerPlayer player) {
        CharacterSelectionState state = state(player);
        if (state.selectedCharacterId() != null) {
            saveActive(player);
            state.selectedCharacterId(null);
        }
        state.selectionRequired(true);
        sendSelection(player, true);
    }

    public static boolean isSelectionRequired(ServerPlayer player) {
        return player != null && state(player).selectionRequired();
    }

    public static boolean hasActiveCharacter(ServerPlayer player) {
        return player != null && state(player).selectedCharacterId() != null && !state(player).selectionRequired();
    }

    public static void createCharacter(ServerPlayer player, int slotIndex, String rawName) {
        if (player == null) {
            return;
        }
        CharacterAccount account = account(player);
        String name = CharacterNameValidator.normalize(rawName);
        if (slotIndex < 0 || slotIndex >= CharacterAccount.MAX_SLOTS) {
            sendResult(player, false, "Invalid character slot.");
            return;
        }
        if (account.slotByIndex(slotIndex).isPresent()) {
            sendResult(player, false, "That slot is already occupied.");
            return;
        }
        if (!CharacterNameValidator.isValid(name)) {
            sendResult(player, false, "Character name must be 3-16 letters, numbers, spaces or underscores.");
            return;
        }
        if (account.hasName(name)) {
            sendResult(player, false, "You already have a character with that name.");
            return;
        }
        UUID characterId = UUID.randomUUID();
        long now = Instant.now().toEpochMilli();
        CharacterSummary summary = CharacterSummary.novice(name);
        CharacterSlot slot = new CharacterSlot(slotIndex, characterId, name, now, now, summary);
        CharacterSaveData saveData = CharacterStateSerializer.createNew(player);
        account.put(slot, saveData);
        store(player).setDirty();
        selectCharacter(player, characterId);
    }

    public static void selectCharacter(ServerPlayer player, UUID characterId) {
        if (player == null || characterId == null) {
            return;
        }
        CharacterAccount account = account(player);
        var slotOpt = account.slotById(characterId);
        if (slotOpt.isEmpty()) {
            sendResult(player, false, "Character not found.");
            sendSelection(player, true);
            return;
        }
        CharacterSelectionState state = state(player);
        if (state.selectedCharacterId() != null && !state.selectedCharacterId().equals(characterId)) {
            saveActive(player);
        }

        CharacterStateSerializer.apply(player, account.save(characterId));
        state.selectedCharacterId(characterId);
        state.selectionRequired(false);
        long now = Instant.now().toEpochMilli();
        CharacterSlot selected = slotOpt.get();
        account.updateSlot(new CharacterSlot(
                selected.slotIndex(),
                selected.characterId(),
                selected.name(),
                selected.createdAt(),
                now,
                summarize(player, selected.name())));
        store(player).setDirty();
        syncAll(player);
        sendSelection(player, false);
        sendResult(player, true, "Loaded " + selected.name() + ".");
    }

    public static void deleteCharacter(ServerPlayer player, UUID characterId, String typedName) {
        if (player == null || characterId == null) {
            return;
        }
        CharacterAccount account = account(player);
        var slotOpt = account.slotById(characterId);
        if (slotOpt.isEmpty()) {
            sendResult(player, false, "Character not found.");
            sendSelection(player, true);
            return;
        }
        CharacterSelectionState state = state(player);
        if (characterId.equals(state.selectedCharacterId())) {
            sendResult(player, false, "Return to character select before deleting the active character.");
            sendSelection(player, true);
            return;
        }
        CharacterSlot slot = slotOpt.get();
        if (!slot.name().equals(typedName)) {
            sendResult(player, false, "Type the exact character name to delete it.");
            sendSelection(player, true);
            return;
        }
        account.remove(characterId);
        store(player).setDirty();
        state.selectionRequired(true);
        sendResult(player, true, "Deleted " + slot.name() + ".");
        sendSelection(player, true);
    }

    public static void saveActive(ServerPlayer player) {
        CharacterSelectionState state = state(player);
        UUID characterId = state.selectedCharacterId();
        if (characterId == null) {
            return;
        }
        CharacterAccount account = account(player);
        var slotOpt = account.slotById(characterId);
        if (slotOpt.isEmpty()) {
            return;
        }
        CharacterSlot slot = slotOpt.get();
        account.updateSave(characterId, CharacterStateSerializer.capture(player));
        account.updateSlot(new CharacterSlot(
                slot.slotIndex(),
                slot.characterId(),
                slot.name(),
                slot.createdAt(),
                Instant.now().toEpochMilli(),
                summarize(player, slot.name())));
        store(player).setDirty();
    }

    public static void clearRuntime(ServerPlayer player) {
        if (player != null) {
            STATES.remove(player.getUUID());
        }
    }

    private static void migrateIfNeeded(ServerPlayer player) {
        CharacterAccount account = account(player);
        if (account.isMigrated() || !account.isEmpty()) {
            account.setMigrated(true);
            store(player).setDirty();
            return;
        }
        String name = CharacterNameValidator.normalize(player.getGameProfile().getName());
        if (!CharacterNameValidator.isValid(name)) {
            name = "Novice_" + player.getStringUUID().substring(0, 4);
        }
        UUID characterId = UUID.randomUUID();
        long now = Instant.now().toEpochMilli();
        CharacterSlot slot = new CharacterSlot(0, characterId, name, now, now, summarize(player, name));
        account.put(slot, CharacterStateSerializer.capture(player));
        account.setMigrated(true);
        store(player).setDirty();
    }

    private static CharacterSummary summarize(ServerPlayer player, String name) {
        return player.getCapability(PlayerStatsProvider.CAP)
                .map(stats -> {
                    JobType job = JobType.fromId(stats.getJobId());
                    return new CharacterSummary(
                            name,
                            Math.max(1, stats.getLevel()),
                            stats.getJobId(),
                            job.getDisplayName(),
                            Math.max(1, stats.getJobLevel()));
                })
                .orElseGet(() -> CharacterSummary.novice(name));
    }

    private static void syncAll(ServerPlayer player) {
        player.getCapability(PlayerStatsProvider.CAP).ifPresent(stats -> {
            StatResolutionService.resolve(player, stats);
            PlayerStatsSyncService.sync(player, stats);
        });
        JobSkillsSyncService.sync(player);
        LifeSkillCapability.get(player).ifPresent(manager ->
                com.etema.ragnarmmo.common.net.Network.sendToPlayer(player,
                        new LifeSkillSyncPacket(manager.serializeNBT())));
        RagnarEquipmentSync.sync(player);
    }

    private static void sendSelection(ServerPlayer player, boolean open) {
        CharacterAccount account = account(player);
        CharacterNetwork.sendToPlayer(player, new ClientboundCharacterListPacket(account.slots(), isSelectionRequired(player)));
        if (open) {
            CharacterNetwork.sendToPlayer(player, new ClientboundOpenCharacterSelectPacket(isSelectionRequired(player)));
        }
    }

    private static void sendResult(ServerPlayer player, boolean success, String message) {
        CharacterNetwork.sendToPlayer(player, new ClientboundCharacterActionResultPacket(success, message));
        player.sendSystemMessage(Component.literal(message).withStyle(success ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private static CharacterSelectionState state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.getUUID(), ignored -> new CharacterSelectionState());
    }

    private static CharacterAccount account(ServerPlayer player) {
        return store(player).account(player.getUUID());
    }

    private static CharacterAccountStore store(ServerPlayer player) {
        return CharacterAccountStore.get(player.server);
    }
}
