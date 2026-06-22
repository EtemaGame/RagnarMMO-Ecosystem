package com.etema.ragnarmmo.core.client;

import com.etema.ragnarmmo.core.RagnarMMOCore;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCore.MOD_ID, value = Dist.CLIENT)
public final class CoreClientEvents {
    private CoreClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Stats screen removed.
    }
}
