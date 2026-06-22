package com.etema.ragnarmmo.jobs.net;

import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.jobs.runtime.JobChangeService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundChangeJobPacket(String jobId) {
    public static void encode(ServerboundChangeJobPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.jobId == null ? "" : msg.jobId);
    }

    public static ServerboundChangeJobPacket decode(FriendlyByteBuf buf) {
        return new ServerboundChangeJobPacket(buf.readUtf());
    }

    public static void handle(ServerboundChangeJobPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ServerPlayer player = ctx.getSender();
        if (player == null) {
            ctx.setPacketHandled(true);
            return;
        }

        ctx.enqueueWork(() -> JobChangeService.changeToFirstClass(player, JobType.fromId(msg.jobId)));
        ctx.setPacketHandled(true);
    }
}
