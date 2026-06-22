package com.etema.ragnarmmo.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class HudOverlayScreenRegistry {
    private static final AtomicReference<Supplier<Screen>> FACTORY = new AtomicReference<>();

    private HudOverlayScreenRegistry() {
    }

    public static void register(Supplier<Screen> factory) {
        FACTORY.set(factory);
    }

    public static Optional<Screen> current() {
        Supplier<Screen> factory = FACTORY.get();
        return factory == null ? Optional.empty() : Optional.ofNullable(factory.get());
    }

    public static void open(Minecraft minecraft) {
        current().ifPresent(screen -> minecraft.setScreen(screen));
    }
}
