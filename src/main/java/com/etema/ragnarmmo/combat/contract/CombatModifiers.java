package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;

public record CombatModifiers(
        String race,
        ElementType element,
        CombatMath.MobSize size,
        int elementLevel) {
    public CombatModifiers(String race, ElementType element, CombatMath.MobSize size) {
        this(race, element, size, 1);
    }

    public CombatModifiers {
        race = race == null || race.isBlank() ? "unknown" : race;
        element = element == null ? ElementType.NEUTRAL : element;
        size = size == null ? CombatMath.MobSize.MEDIUM : size;
        elementLevel = Math.max(1, Math.min(4, elementLevel));
    }
}
