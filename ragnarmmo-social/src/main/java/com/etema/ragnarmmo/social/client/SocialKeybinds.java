package com.etema.ragnarmmo.social.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.social.RagnarMMOSocial.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SocialKeybinds {
    private static final String CATEGORY = "key.categories.ragnarmmo";

    public static final KeyMapping OPEN_ACHIEVEMENTS = new KeyMapping(
            "key.ragnarmmo.open_achievements",
            GLFW.GLFW_KEY_Y,
            CATEGORY);
    public static final KeyMapping OPEN_BESTIARY = new KeyMapping(
            "key.ragnarmmo.open_bestiary",
            GLFW.GLFW_KEY_B,
            CATEGORY);

    private SocialKeybinds() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ACHIEVEMENTS);
        event.register(OPEN_BESTIARY);
    }
}
