package com.etema.ragnarmmo.jobs.runtime;

import com.etema.ragnarmmo.jobs.data.SkillDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public record JobSkillContext(
        ServerPlayer player,
        ResourceLocation skillId,
        SkillDefinition definition,
        int level,
        Optional<LivingEntity> target) {
}
