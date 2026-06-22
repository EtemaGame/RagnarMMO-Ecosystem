package com.etema.ragnarmmo.core.client;

import com.etema.ragnarmmo.core.client.CoreClientPacketHandler;
import com.etema.ragnarmmo.player.stats.capability.PlayerStats;
import com.etema.ragnarmmo.player.stats.capability.PlayerStatsProvider;
import com.etema.ragnarmmo.player.stats.network.DerivedStatsSyncPacket;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncPacket;
import net.minecraft.client.Minecraft;

public final class ClientCoreSyncHandler implements CoreClientPacketHandler.Handler {
    public static final ClientCoreSyncHandler INSTANCE = new ClientCoreSyncHandler();

    private ClientCoreSyncHandler() {
    }

    @Override
    public void handlePlayerStatsSync(PlayerStatsSyncPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        var entity = minecraft.level.getEntity(packet.entityId);
        if (entity == null) {
            return;
        }
        entity.getCapability(PlayerStatsProvider.CAP).ifPresent(stats -> {
            if (stats instanceof PlayerStats playerStats) {
                playerStats.applyMirrorState(packet);
            }
        });
    }

    @Override
    public void handleDerivedStatsSync(DerivedStatsSyncPacket packet) {
        DerivedStatsClientCache.update(packet.toDerivedStats());
    }
}
