package com.etema.ragnarmmo.jobs.net;

import com.etema.ragnarmmo.jobs.client.JobSkillsClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public record JobSkillsSyncPacket(Map<ResourceLocation, Integer> levels, Map<Integer, ResourceLocation> hotbar) {
    public static void encode(JobSkillsSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.levels.size());
        for (var entry : msg.levels.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
        buf.writeVarInt(msg.hotbar.size());
        for (var entry : msg.hotbar.entrySet()) {
            buf.writeVarInt(entry.getKey());
            buf.writeResourceLocation(entry.getValue());
        }
    }

    public static JobSkillsSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, Integer> levels = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            levels.put(buf.readResourceLocation(), buf.readVarInt());
        }
        int hotbarSize = buf.readVarInt();
        Map<Integer, ResourceLocation> hotbar = new LinkedHashMap<>();
        for (int i = 0; i < hotbarSize; i++) {
            hotbar.put(buf.readVarInt(), buf.readResourceLocation());
        }
        return new JobSkillsSyncPacket(levels, hotbar);
    }

    public static void handle(JobSkillsSyncPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> JobSkillsClientCache.replace(msg.levels, msg.hotbar)));
        ctx.setPacketHandled(true);
    }
}
