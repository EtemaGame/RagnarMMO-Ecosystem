package com.etema.ragnarmmo.player.character.net;

import com.etema.ragnarmmo.player.character.runtime.CharacterSelectionService;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record ServerboundSelectCharacterPacket(UUID characterId) {
    public static void encode(ServerboundSelectCharacterPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.characterId);
    }

    public static ServerboundSelectCharacterPacket decode(FriendlyByteBuf buf) {
        return new ServerboundSelectCharacterPacket(buf.readUUID());
    }

    public static void handle(ServerboundSelectCharacterPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                CharacterSelectionService.selectCharacter(player, msg.characterId);
            }
        });
        ctx.setPacketHandled(true);
    }
}
