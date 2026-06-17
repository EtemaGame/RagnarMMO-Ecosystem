package com.etema.ragnarmmo.economy;

import com.etema.ragnarmmo.economy.zeny.ZenyItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RagnarMMOEconomy.MOD_ID)
public final class RagnarMMOEconomy {
    public static final String MOD_ID = "ragnarmmo_economy";

    public RagnarMMOEconomy() {
        ZenyItems.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
