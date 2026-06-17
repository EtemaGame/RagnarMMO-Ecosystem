package com.etema.ragnarmmo.jobs.net;

import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundSetHotbarSlotPacket(int slot, ResourceLocation skillId) {
    public static void encode(ServerboundSetHotbarSlotPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot);
        buf.writeBoolean(msg.skillId != null);
        if (msg.skillId != null) {
            buf.writeResourceLocation(msg.skillId);
        }
    }

    public static ServerboundSetHotbarSlotPacket decode(FriendlyByteBuf buf) {
        int slot = buf.readVarInt();
        ResourceLocation skillId = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new ServerboundSetHotbarSlotPacket(slot, skillId);
    }

    public static void handle(ServerboundSetHotbarSlotPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> PlayerJobSkillsProvider.get(player).ifPresent(skills -> {
                if (msg.skillId == null || (skills.getSkillLevel(msg.skillId) > 0
                        && SkillDefinitionRegistry.get(msg.skillId).map(def -> def.isActive()).orElse(false))) {
                    skills.setHotbarSlot(msg.slot, msg.skillId);
                    JobSkillsSyncService.sync(player);
                }
            }));
        }
        ctx.setPacketHandled(true);
    }
}
