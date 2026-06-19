package com.etema.ragnarmmo.core.client.hud;

public record HudWidgetState(
        boolean enabled,
        double anchorX,
        double anchorY,
        double scale,
        int backgroundAlpha,
        boolean showBackground,
        int zOrder) {
}
