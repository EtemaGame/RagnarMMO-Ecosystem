package com.etema.ragnarmmo.player.character.net;

import com.etema.ragnarmmo.player.character.runtime.CharacterSelectionService;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record ServerboundReturnToCharacterSelectPacket() {
    public static void encode(ServerboundReturnToCharacterSelectPacket msg, FriendlyByteBuf buf) {
    }

    public static ServerboundReturnToCharacterSelectPacket decode(FriendlyByteBuf buf) {
        return new ServerboundReturnToCharacterSelectPacket();
    }

    public static void handle(ServerboundReturnToCharacterSelectPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                CharacterSelectionService.returnToSelection(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
