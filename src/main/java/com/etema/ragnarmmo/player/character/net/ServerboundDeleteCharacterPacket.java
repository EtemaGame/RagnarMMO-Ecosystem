package com.etema.ragnarmmo.player.character.net;

import com.etema.ragnarmmo.player.character.runtime.CharacterSelectionService;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record ServerboundDeleteCharacterPacket(UUID characterId, String typedName) {
    public static void encode(ServerboundDeleteCharacterPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.characterId);
        buf.writeUtf(msg.typedName == null ? "" : msg.typedName);
    }

    public static ServerboundDeleteCharacterPacket decode(FriendlyByteBuf buf) {
        return new ServerboundDeleteCharacterPacket(buf.readUUID(), buf.readUtf());
    }

    public static void handle(ServerboundDeleteCharacterPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                CharacterSelectionService.deleteCharacter(player, msg.characterId, msg.typedName);
            }
        });
        ctx.setPacketHandled(true);
    }
}
