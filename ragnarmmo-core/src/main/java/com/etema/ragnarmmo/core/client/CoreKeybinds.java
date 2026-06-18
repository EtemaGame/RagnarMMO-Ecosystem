package com.etema.ragnarmmo.core.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.core.RagnarMMOCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CoreKeybinds {
    public static final KeyMapping OPEN_STATS = new KeyMapping(
            "key.ragnarmmo.open_stats",
            GLFW.GLFW_KEY_V,
            "key.categories.ragnarmmo");

    private CoreKeybinds() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS);
    }
}
