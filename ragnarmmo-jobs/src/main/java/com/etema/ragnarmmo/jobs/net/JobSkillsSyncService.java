package com.etema.ragnarmmo.jobs.net;

import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import net.minecraft.server.level.ServerPlayer;

public final class JobSkillsSyncService {
    private JobSkillsSyncService() {
    }

    public static void sync(ServerPlayer player) {
        if (player == null || player.connection == null) {
            return;
        }
        PlayerJobSkillsProvider.get(player).ifPresent(skills ->
                Network.sendToPlayer(player, new JobSkillsSyncPacket(
                        skills.getSkillLevels(),
                        skills.getHotbarSnapshot())));
    }
}
