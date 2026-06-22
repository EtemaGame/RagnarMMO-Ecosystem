package com.etema.ragnarmmo.items.client;

import com.etema.ragnarmmo.items.equipment.RagnarEquipmentProvider;
import com.etema.ragnarmmo.items.network.SyncRagnarEquipmentPacket;
import net.minecraft.client.Minecraft;

public final class RagnarEquipmentClientSyncHandler {
    private RagnarEquipmentClientSyncHandler() {
    }

    public static void handle(SyncRagnarEquipmentPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        RagnarEquipmentProvider.get(minecraft.player)
                .ifPresent(handler -> handler.deserializeNBT(packet.equipment()));
    }
}
