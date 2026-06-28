package com.etema.ragnarmmo.player.character.net;

import com.etema.ragnarmmo.player.character.client.CharacterClientHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record ClientboundCharacterActionResultPacket(boolean success, String message) {
    public static void encode(ClientboundCharacterActionResultPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.success);
        buf.writeUtf(msg.message == null ? "" : msg.message);
    }

    public static ClientboundCharacterActionResultPacket decode(FriendlyByteBuf buf) {
        return new ClientboundCharacterActionResultPacket(buf.readBoolean(), buf.readUtf());
    }

    public static void handle(ClientboundCharacterActionResultPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> CharacterClientHandler.handleResult(msg)));
        ctx.setPacketHandled(true);
    }
}
