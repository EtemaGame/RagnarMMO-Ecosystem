package com.etema.ragnarmmo.combat.contract;

import net.minecraft.resources.ResourceLocation;

public sealed interface ActionIntent permits ActionIntent.BasicAttackIntent, ActionIntent.SkillIntent {
    record BasicAttackIntent(boolean offHand) implements ActionIntent {
    }

    record SkillIntent(ResourceLocation skillId, int skillLevel, Integer targetEntityId) implements ActionIntent {
    }
}
