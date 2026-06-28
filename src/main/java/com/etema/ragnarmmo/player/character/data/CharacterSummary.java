package com.etema.ragnarmmo.player.character.data;

import net.minecraft.nbt.CompoundTag;

public record CharacterSummary(
        String name,
        int baseLevel,
        String jobId,
        String jobName,
        int jobLevel
) {
    public static CharacterSummary novice(String name) {
        return new CharacterSummary(name, 1, "ragnarmmo:novice", "Novice", 1);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", name == null ? "" : name);
        tag.putInt("BaseLevel", Math.max(1, baseLevel));
        tag.putString("JobId", jobId == null || jobId.isBlank() ? "ragnarmmo:novice" : jobId);
        tag.putString("JobName", jobName == null || jobName.isBlank() ? "Novice" : jobName);
        tag.putInt("JobLevel", Math.max(1, jobLevel));
        return tag;
    }

    public static CharacterSummary deserializeNBT(CompoundTag tag) {
        if (tag == null) {
            return novice("");
        }
        return new CharacterSummary(
                tag.getString("Name"),
                Math.max(1, tag.getInt("BaseLevel")),
                tag.getString("JobId").isBlank() ? "ragnarmmo:novice" : tag.getString("JobId"),
                tag.getString("JobName").isBlank() ? "Novice" : tag.getString("JobName"),
                Math.max(1, tag.getInt("JobLevel")));
    }
}
