package com.etema.ragnarmmo.client;

import com.etema.ragnarmmo.core.client.CoreClientPacketHandler;
import net.minecraftforge.fml.common.Mod;

@Mod(RagnarMMOClient.MOD_ID)
public final class RagnarMMOClient {
    public static final String MOD_ID = "ragnarmmo_client";

    public RagnarMMOClient() {
        CoreClientPacketHandler.register(ClientCoreSyncHandler.INSTANCE);
    }
}
