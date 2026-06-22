package com.etema.ragnarmmo.items.equipment;

import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.items.network.SyncRagnarEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;

public final class RagnarEquipmentSync {
    private RagnarEquipmentSync() {
    }

    public static void sync(ServerPlayer player) {
        if (player == null || player.connection == null) {
            return;
        }
        RagnarEquipmentProvider.get(player).ifPresent(handler ->
                Network.sendToPlayer(player, new SyncRagnarEquipmentPacket(handler.serializeNBT())));
    }
}
