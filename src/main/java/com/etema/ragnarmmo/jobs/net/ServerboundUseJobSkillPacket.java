package com.etema.ragnarmmo.jobs.net;

import com.etema.ragnarmmo.jobs.runtime.JobSkillExecutor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundUseJobSkillPacket(ResourceLocation skillId) {
    public static void encode(ServerboundUseJobSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.skillId);
    }

    public static ServerboundUseJobSkillPacket decode(FriendlyByteBuf buf) {
        return new ServerboundUseJobSkillPacket(buf.readResourceLocation());
    }

    public static void handle(ServerboundUseJobSkillPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> JobSkillExecutor.use(player, msg.skillId));
        }
        ctx.setPacketHandled(true);
    }
}
