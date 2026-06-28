package com.etema.ragnarmmo.player.character.runtime;

import com.etema.ragnarmmo.player.character.data.CharacterSaveData;
import com.etema.ragnarmmo.player.character.data.CharacterSlot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public final class CharacterAccount {
    public static final int MAX_SLOTS = 3;

    private final List<CharacterSlot> slots = new ArrayList<>();
    private final java.util.Map<UUID, CharacterSaveData> saves = new java.util.LinkedHashMap<>();
    private boolean migrated;

    public List<CharacterSlot> slots() {
        return slots.stream().sorted(Comparator.comparingInt(CharacterSlot::slotIndex)).toList();
    }

    public boolean isMigrated() {
        return migrated;
    }

    public void setMigrated(boolean migrated) {
        this.migrated = migrated;
    }

    public boolean isEmpty() {
        return slots.isEmpty();
    }

    public Optional<CharacterSlot> slotByIndex(int index) {
        return slots.stream().filter(slot -> slot.slotIndex() == index).findFirst();
    }

    public Optional<CharacterSlot> slotById(UUID id) {
        return slots.stream().filter(slot -> slot.characterId().equals(id)).findFirst();
    }

    public boolean hasName(String name) {
        return slots.stream().anyMatch(slot -> slot.name().equalsIgnoreCase(name));
    }

    public void put(CharacterSlot slot, CharacterSaveData saveData) {
        slots.removeIf(existing -> existing.slotIndex() == slot.slotIndex()
                || existing.characterId().equals(slot.characterId()));
        slots.add(slot);
        saves.put(slot.characterId(), saveData);
    }

    public void remove(UUID id) {
        slots.removeIf(slot -> slot.characterId().equals(id));
        saves.remove(id);
    }

    public CharacterSaveData save(UUID id) {
        return saves.getOrDefault(id, new CharacterSaveData(new CompoundTag()));
    }

    public void updateSave(UUID id, CharacterSaveData saveData) {
        if (id != null && saveData != null) {
            saves.put(id, saveData);
        }
    }

    public void updateSlot(CharacterSlot slot) {
        slots.removeIf(existing -> existing.characterId().equals(slot.characterId()));
        slots.add(slot);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Migrated", migrated);
        ListTag slotList = new ListTag();
        for (CharacterSlot slot : slots()) {
            slotList.add(slot.serializeNBT());
        }
        tag.put("Slots", slotList);
        CompoundTag saveTags = new CompoundTag();
        saves.forEach((id, save) -> saveTags.put(id.toString(), save.serializeNBT()));
        tag.put("Saves", saveTags);
        return tag;
    }

    public static CharacterAccount deserializeNBT(CompoundTag tag) {
        CharacterAccount account = new CharacterAccount();
        account.migrated = tag.getBoolean("Migrated");
        ListTag slotList = tag.getList("Slots", 10);
        for (int i = 0; i < slotList.size(); i++) {
            account.slots.add(CharacterSlot.deserializeNBT(slotList.getCompound(i)));
        }
        CompoundTag saveTags = tag.getCompound("Saves");
        for (String key : saveTags.getAllKeys()) {
            try {
                account.saves.put(UUID.fromString(key), CharacterSaveData.deserializeNBT(saveTags.getCompound(key)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return account;
    }
}
