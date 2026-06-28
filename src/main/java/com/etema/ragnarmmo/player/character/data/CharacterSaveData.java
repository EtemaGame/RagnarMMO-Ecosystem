package com.etema.ragnarmmo.player.character.data;

import net.minecraft.nbt.CompoundTag;

public record CharacterSaveData(CompoundTag data) {
    public CharacterSaveData {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public CompoundTag serializeNBT() {
        return data.copy();
    }

    public static CharacterSaveData deserializeNBT(CompoundTag tag) {
        return new CharacterSaveData(tag);
    }
}
