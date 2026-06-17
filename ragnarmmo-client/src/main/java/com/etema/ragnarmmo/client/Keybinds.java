package com.etema.ragnarmmo.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RagnarMMOClient.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Keybinds {
    private static final String CATEGORY = "key.categories.ragnarmmo";

    public static final KeyMapping OPEN_MENU =
            new KeyMapping("key.ragnarmmo.open_menu", GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping OPEN_STATS =
            new KeyMapping("key.ragnarmmo.open_stats", GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping OPEN_SKILLS =
            new KeyMapping("key.ragnarmmo.open_skills", GLFW.GLFW_KEY_K, CATEGORY);
    public static final KeyMapping OPEN_ACHIEVEMENTS =
            new KeyMapping("key.ragnarmmo.open_achievements", GLFW.GLFW_KEY_Y, CATEGORY);
    public static final KeyMapping OPEN_BESTIARY =
            new KeyMapping("key.ragnarmmo.open_bestiary", GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping TOGGLE_COMBAT_MODE =
            new KeyMapping("key.ragnarmmo.toggle_combat", GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping[] HOTBAR_KEYS = new KeyMapping[6];

    static {
        for (int i = 0; i < HOTBAR_KEYS.length; i++) {
            HOTBAR_KEYS[i] = new KeyMapping("key.ragnarmmo.skill_hotbar." + (i + 1),
                    GLFW.GLFW_KEY_1 + i, CATEGORY);
        }
    }

    private Keybinds() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
        event.register(OPEN_STATS);
        event.register(OPEN_SKILLS);
        event.register(OPEN_ACHIEVEMENTS);
        event.register(OPEN_BESTIARY);
        event.register(TOGGLE_COMBAT_MODE);
        for (KeyMapping key : HOTBAR_KEYS) {
            event.register(key);
        }
    }
}
