package com.etema.ragnarmmo.combat.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.combat.RagnarMMOCombat.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CombatKeybinds {
    private static final String CATEGORY = "key.categories.ragnarmmo";

    public static final KeyMapping TOGGLE_COMBAT_MODE = new KeyMapping(
            "key.ragnarmmo.toggle_combat",
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY);

    private CombatKeybinds() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_COMBAT_MODE);
    }
}
