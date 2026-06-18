package com.etema.ragnarmmo.social.client;

import com.etema.ragnarmmo.social.client.ui.AchievementsScreen;
import com.etema.ragnarmmo.social.client.ui.BestiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.social.RagnarMMOSocial.MOD_ID, value = Dist.CLIENT)
public final class SocialClientEvents {
    private SocialClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        while (SocialKeybinds.OPEN_ACHIEVEMENTS.consumeClick()) {
            minecraft.setScreen(new AchievementsScreen());
        }
        while (SocialKeybinds.OPEN_BESTIARY.consumeClick()) {
            minecraft.setScreen(new BestiaryScreen());
        }
    }
}
