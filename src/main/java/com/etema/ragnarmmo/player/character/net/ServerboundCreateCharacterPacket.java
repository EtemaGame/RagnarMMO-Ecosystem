package com.etema.ragnarmmo.player.character.net;

import com.etema.ragnarmmo.player.character.runtime.CharacterSelectionService;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record ServerboundCreateCharacterPacket(int slotIndex, String name) {
    public static void encode(ServerboundCreateCharacterPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slotIndex);
        buf.writeUtf(msg.name == null ? "" : msg.name);
    }

    public static ServerboundCreateCharacterPacket decode(FriendlyByteBuf buf) {
        return new ServerboundCreateCharacterPacket(buf.readVarInt(), buf.readUtf());
    }

    public static void handle(ServerboundCreateCharacterPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                CharacterSelectionService.createCharacter(player, msg.slotIndex, msg.name);
            }
        });
        ctx.setPacketHandled(true);
    }
}
