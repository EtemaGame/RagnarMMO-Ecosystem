package com.etema.ragnarmmo.combat.element;

public record ElementProperty(ElementType type, int level) {
    public ElementProperty {
        type = type == null ? ElementType.NEUTRAL : type;
        level = Math.max(1, Math.min(4, level));
    }
}
