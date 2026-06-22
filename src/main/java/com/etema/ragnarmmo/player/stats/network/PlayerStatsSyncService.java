package com.etema.ragnarmmo.player.stats.network;

import com.etema.ragnarmmo.common.api.player.RoPlayerSyncDomain;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerStatsSyncService {
    private PlayerStatsSyncService() {
    }

    public static void sync(ServerPlayer player, IPlayerStats stats) {
        sync(player, stats, RoPlayerSyncDomain.allMask());
    }

    public static void sync(ServerPlayer player, IPlayerStats stats, int syncMask) {
        if (player == null || stats == null || syncMask == 0 || player.connection == null) {
            return;
        }

        Network.sendTrackingEntityAndSelf(player, new PlayerStatsSyncPacket(player.getId(), stats, syncMask));

        if (RoPlayerSyncDomain.requiresDerivedSync(syncMask)) {
            DerivedStatsService.compute(player, stats)
                    .ifPresent(derived -> Network.sendToPlayer(player, new DerivedStatsSyncPacket(derived)));
        }
    }
}
