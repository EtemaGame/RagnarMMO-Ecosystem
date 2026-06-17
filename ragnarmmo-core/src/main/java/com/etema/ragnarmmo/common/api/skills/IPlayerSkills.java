package com.etema.ragnarmmo.common.api.skills;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface IPlayerSkills {
    int getSkillLevel(ResourceLocation skillId);

    Map<ResourceLocation, Integer> getSkillLevels();

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag nbt);
}
