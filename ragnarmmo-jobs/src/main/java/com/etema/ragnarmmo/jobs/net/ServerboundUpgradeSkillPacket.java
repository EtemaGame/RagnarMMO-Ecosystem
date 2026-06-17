package com.etema.ragnarmmo.jobs.net;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.player.RoPlayerSyncDomain;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundUpgradeSkillPacket(ResourceLocation skillId) {
    public static void encode(ServerboundUpgradeSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.skillId);
    }

    public static ServerboundUpgradeSkillPacket decode(FriendlyByteBuf buf) {
        return new ServerboundUpgradeSkillPacket(buf.readResourceLocation());
    }

    public static void handle(ServerboundUpgradeSkillPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ServerPlayer player = ctx.getSender();
        if (player == null) {
            ctx.setPacketHandled(true);
            return;
        }

        ctx.enqueueWork(() -> tryUpgrade(player, msg.skillId));
        ctx.setPacketHandled(true);
    }

    private static void tryUpgrade(ServerPlayer player, ResourceLocation skillId) {
        SkillDefinitionRegistry.get(skillId).ifPresent(definition ->
                RagnarCoreAPI.get(player).ifPresent(stats ->
                        PlayerJobSkillsProvider.get(player).ifPresent(skills -> {
                            JobType job = JobType.fromId(stats.getJobId());
                            if (!definition.canUpgradeWithPoints()) {
                                return;
                            }
                            if (!SkillDefinitionRegistry.isAllowedForJob(skillId, job)) {
                                return;
                            }
                            for (var requirement : definition.requirements().entrySet()) {
                                if (skills.getSkillLevel(requirement.getKey()) < requirement.getValue()) {
                                    return;
                                }
                            }
                            int current = skills.getSkillLevel(skillId);
                            if (current >= definition.maxLevel()) {
                                return;
                            }
                            int cost = Math.max(0, definition.upgradeCost());
                            if (stats.getSkillPoints() < cost) {
                                return;
                            }
                            stats.setSkillPoints(stats.getSkillPoints() - cost);
                            skills.setSkillLevel(skillId, current + 1);
                            PlayerStatsSyncService.sync(player, stats, RoPlayerSyncDomain.PROGRESSION.bit());
                            JobSkillsSyncService.sync(player);
                        })));
    }
}
