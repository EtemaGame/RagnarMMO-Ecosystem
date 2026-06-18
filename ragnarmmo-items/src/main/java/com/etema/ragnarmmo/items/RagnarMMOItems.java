package com.etema.ragnarmmo.items;

import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.items.cards.RagnarCardItems;
import com.etema.ragnarmmo.items.loot.RagnarLootModifiers;
import com.etema.ragnarmmo.items.runtime.ItemDerivedStatsContributor;
import com.etema.ragnarmmo.items.network.RoItemsNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RagnarMMOItems.MOD_ID)
public final class RagnarMMOItems {
    public static final String MOD_ID = "ragnarmmo_items";

    public RagnarMMOItems() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ItemsModule.init(modBus);
        Network.registerPackets(RoItemsNetwork::register);
        ItemDerivedStatsContributor.register();
    }
}
