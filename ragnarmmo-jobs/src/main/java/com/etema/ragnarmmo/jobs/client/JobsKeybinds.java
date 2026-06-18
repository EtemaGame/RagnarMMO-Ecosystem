package com.etema.ragnarmmo.jobs.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.jobs.RagnarMMOJobs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class JobsKeybinds {
    private static final String CATEGORY = "key.categories.ragnarmmo";

    public static final KeyMapping OPEN_SKILLS = new KeyMapping(
            "key.ragnarmmo.open_skills",
            GLFW.GLFW_KEY_K,
            CATEGORY);
    public static final KeyMapping[] HOTBAR_KEYS = new KeyMapping[6];

    static {
        for (int i = 0; i < HOTBAR_KEYS.length; i++) {
            HOTBAR_KEYS[i] = new KeyMapping("key.ragnarmmo.skill_hotbar." + (i + 1),
                    GLFW.GLFW_KEY_1 + i, CATEGORY);
        }
    }

    private JobsKeybinds() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SKILLS);
        for (KeyMapping key : HOTBAR_KEYS) {
            event.register(key);
        }
    }
}
