package com.etema.ragnarmmo.items.client;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.items.client.gui.tooltip.CardTooltipData;
import com.etema.ragnarmmo.items.client.gui.tooltip.ClientCardTooltip;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientTooltipComponents {
    private ClientTooltipComponents() {
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CardTooltipData.class, ClientCardTooltip::new);
    }
}
