package com.etema.ragnarmmo.economy;

import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.economy.zeny.EconomyEventHandler;
import com.etema.ragnarmmo.economy.zeny.network.EconomyNetwork;
import com.etema.ragnarmmo.economy.zeny.ZenyItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RagnarMMOEconomy.MOD_ID)
public final class RagnarMMOEconomy {
    public static final String MOD_ID = "ragnarmmo_economy";

    public RagnarMMOEconomy() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ZenyItems.register(modBus);
        Network.registerPackets(EconomyNetwork::register);
    }
}
