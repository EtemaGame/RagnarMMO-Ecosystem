package com.etema.ragnarmmo.player.character.net;

import com.etema.ragnarmmo.player.character.client.CharacterClientHandler;
import com.etema.ragnarmmo.player.character.data.CharacterSlot;
import com.etema.ragnarmmo.player.character.data.CharacterSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record ClientboundCharacterListPacket(List<CharacterSlot> slots, boolean selectionRequired) {
    public static void encode(ClientboundCharacterListPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.selectionRequired);
        buf.writeVarInt(msg.slots.size());
        for (CharacterSlot slot : msg.slots) {
            buf.writeVarInt(slot.slotIndex());
            buf.writeUUID(slot.characterId());
            buf.writeUtf(slot.name());
            buf.writeLong(slot.createdAt());
            buf.writeLong(slot.lastPlayedAt());
            CharacterSummary summary = slot.summary();
            buf.writeUtf(summary.name());
            buf.writeVarInt(summary.baseLevel());
            buf.writeUtf(summary.jobId());
            buf.writeUtf(summary.jobName());
            buf.writeVarInt(summary.jobLevel());
        }
    }

    public static ClientboundCharacterListPacket decode(FriendlyByteBuf buf) {
        boolean required = buf.readBoolean();
        int count = buf.readVarInt();
        List<CharacterSlot> slots = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int slotIndex = buf.readVarInt();
            UUID id = buf.readUUID();
            String name = buf.readUtf();
            long createdAt = buf.readLong();
            long lastPlayedAt = buf.readLong();
            CharacterSummary summary = new CharacterSummary(
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt());
            slots.add(new CharacterSlot(slotIndex, id, name, createdAt, lastPlayedAt, summary));
        }
        return new ClientboundCharacterListPacket(slots, required);
    }

    public static void handle(ClientboundCharacterListPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> CharacterClientHandler.handleList(msg)));
        ctx.setPacketHandled(true);
    }
}
