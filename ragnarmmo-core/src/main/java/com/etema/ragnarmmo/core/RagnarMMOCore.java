package com.etema.ragnarmmo.core;

import com.etema.ragnarmmo.core.config.RagnarCoreConfigs;
import com.etema.ragnarmmo.common.api.attributes.RagnarAttributes;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.core.client.ClientCoreSyncHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import com.etema.ragnarmmo.player.stats.compute.CoreDerivedStatsCalculator;

@Mod(RagnarMMOCore.MOD_ID)
public final class RagnarMMOCore {
    public static final String MOD_ID = "ragnarmmo_core";

    public RagnarMMOCore() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        RagnarAttributes.register(modBus);
        Network.registerCorePackets();
        DerivedStatsService.register(CoreDerivedStatsCalculator::compute);
        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RagnarCoreConfigs.SERVER_SPEC);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.etema.ragnarmmo.core.client.CoreClientPacketHandler.register(ClientCoreSyncHandler.INSTANCE));
    }
}
