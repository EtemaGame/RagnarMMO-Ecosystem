package com.etema.ragnarmmo.social.client;

import com.etema.ragnarmmo.core.client.HudOverlayScreenRegistry;
import com.etema.ragnarmmo.social.client.ui.HudOverlayConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.social.RagnarMMOSocial.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SocialClientSetup {
    private SocialClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> HudOverlayScreenRegistry.register(HudOverlayConfigScreen::new));
    }
}
