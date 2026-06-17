package com.etema.ragnarmmo.mobs;

import com.etema.ragnarmmo.mobs.entity.RagnarMobEntities;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RagnarMMOMobs.MOD_ID)
public final class RagnarMMOMobs {
    public static final String MOD_ID = "ragnarmmo_mobs";

    public RagnarMMOMobs() {
        RagnarMobEntities.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
