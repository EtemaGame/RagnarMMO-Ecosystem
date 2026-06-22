package com.etema.ragnarmmo.items;

import com.etema.ragnarmmo.RagnarMMO;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.items.runtime.ItemDerivedStatsContributor;
import com.etema.ragnarmmo.items.network.RoItemsNetwork;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class RagnarMMOItems {
    public static final String MOD_ID = RagnarMMO.MOD_ID;

    private RagnarMMOItems() {
    }

    public static void init() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ItemsModule.init(modBus);
        Network.registerPackets(RoItemsNetwork::register);
        ItemDerivedStatsContributor.register();
    }
}
