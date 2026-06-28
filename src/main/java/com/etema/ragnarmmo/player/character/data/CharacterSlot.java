package com.etema.ragnarmmo.player.character.data;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public record CharacterSlot(
        int slotIndex,
        UUID characterId,
        String name,
        long createdAt,
        long lastPlayedAt,
        CharacterSummary summary
) {
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SlotIndex", slotIndex);
        tag.putUUID("CharacterId", characterId);
        tag.putString("Name", name == null ? "" : name);
        tag.putLong("CreatedAt", createdAt);
        tag.putLong("LastPlayedAt", lastPlayedAt);
        tag.put("Summary", (summary == null ? CharacterSummary.novice(name) : summary).serializeNBT());
        return tag;
    }

    public static CharacterSlot deserializeNBT(CompoundTag tag) {
        UUID id = tag.hasUUID("CharacterId") ? tag.getUUID("CharacterId") : UUID.randomUUID();
        String name = tag.getString("Name");
        CharacterSummary summary = CharacterSummary.deserializeNBT(tag.getCompound("Summary"));
        if (summary.name().isBlank() && !name.isBlank()) {
            summary = new CharacterSummary(name, summary.baseLevel(), summary.jobId(), summary.jobName(), summary.jobLevel());
        }
        return new CharacterSlot(
                tag.getInt("SlotIndex"),
                id,
                name,
                tag.getLong("CreatedAt"),
                tag.getLong("LastPlayedAt"),
                summary);
    }
}
