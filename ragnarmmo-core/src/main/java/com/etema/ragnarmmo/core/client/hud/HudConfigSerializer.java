package com.etema.ragnarmmo.core.client.hud;

import com.etema.ragnarmmo.core.config.RagnarClientConfigs;

public final class HudConfigSerializer {
    private HudConfigSerializer() {
    }

    public static HudWidgetState read(RagnarClientConfigs.Hud.HudComponent config) {
        return new HudWidgetState(
                config.enabled.get(),
                config.anchorX.get(),
                config.anchorY.get(),
                config.scale.get(),
                config.backgroundAlpha.get(),
                config.showBackground.get(),
                config.zOrder.get());
    }

    public static void write(RagnarClientConfigs.Hud.HudComponent config, HudWidgetState state) {
        config.enabled.set(state.enabled());
        config.anchorX.set(state.anchorX());
        config.anchorY.set(state.anchorY());
        config.scale.set(state.scale());
        config.backgroundAlpha.set(state.backgroundAlpha());
        config.showBackground.set(state.showBackground());
        config.zOrder.set(state.zOrder());
    }
}
