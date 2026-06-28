package com.etema.ragnarmmo.player.character.net;

import com.etema.ragnarmmo.common.net.Network;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public final class CharacterNetwork {
    private static boolean registered;

    private CharacterNetwork() {
    }

    public static void register(SimpleChannel channel, AtomicInteger id) {
        if (registered) {
            return;
        }
        channel.messageBuilder(ClientboundCharacterListPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundCharacterListPacket::encode)
                .decoder(ClientboundCharacterListPacket::decode)
                .consumerMainThread(ClientboundCharacterListPacket::handle)
                .add();
        channel.messageBuilder(ClientboundOpenCharacterSelectPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundOpenCharacterSelectPacket::encode)
                .decoder(ClientboundOpenCharacterSelectPacket::decode)
                .consumerMainThread(ClientboundOpenCharacterSelectPacket::handle)
                .add();
        channel.messageBuilder(ClientboundCharacterActionResultPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundCharacterActionResultPacket::encode)
                .decoder(ClientboundCharacterActionResultPacket::decode)
                .consumerMainThread(ClientboundCharacterActionResultPacket::handle)
                .add();
        channel.messageBuilder(ServerboundCreateCharacterPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundCreateCharacterPacket::encode)
                .decoder(ServerboundCreateCharacterPacket::decode)
                .consumerMainThread(ServerboundCreateCharacterPacket::handle)
                .add();
        channel.messageBuilder(ServerboundSelectCharacterPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundSelectCharacterPacket::encode)
                .decoder(ServerboundSelectCharacterPacket::decode)
                .consumerMainThread(ServerboundSelectCharacterPacket::handle)
                .add();
        channel.messageBuilder(ServerboundDeleteCharacterPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundDeleteCharacterPacket::encode)
                .decoder(ServerboundDeleteCharacterPacket::decode)
                .consumerMainThread(ServerboundDeleteCharacterPacket::handle)
                .add();
        channel.messageBuilder(ServerboundReturnToCharacterSelectPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundReturnToCharacterSelectPacket::encode)
                .decoder(ServerboundReturnToCharacterSelectPacket::decode)
                .consumerMainThread(ServerboundReturnToCharacterSelectPacket::handle)
                .add();
        registered = true;
    }

    public static <T> void sendToPlayer(ServerPlayer player, T message) {
        Network.sendToPlayer(player, message);
    }
}
