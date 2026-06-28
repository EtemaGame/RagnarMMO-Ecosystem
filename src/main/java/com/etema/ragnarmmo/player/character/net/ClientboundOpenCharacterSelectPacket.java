package com.etema.ragnarmmo.player.character.net;

import com.etema.ragnarmmo.player.character.client.CharacterClientHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record ClientboundOpenCharacterSelectPacket(boolean required) {
    public static void encode(ClientboundOpenCharacterSelectPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.required);
    }

    public static ClientboundOpenCharacterSelectPacket decode(FriendlyByteBuf buf) {
        return new ClientboundOpenCharacterSelectPacket(buf.readBoolean());
    }

    public static void handle(ClientboundOpenCharacterSelectPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> CharacterClientHandler.openSelect(msg.required)));
        ctx.setPacketHandled(true);
    }
}
