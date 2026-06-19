package com.etema.ragnarmmo.core.client.hud;

import net.minecraft.network.chat.Component;

public record HudWidgetDefinition(
        String id,
        Component displayName,
        int defaultZOrder) {
}
