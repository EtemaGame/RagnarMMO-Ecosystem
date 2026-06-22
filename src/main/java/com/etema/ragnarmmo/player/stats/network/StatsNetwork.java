package com.etema.ragnarmmo.player.stats.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public final class StatsNetwork {
    private StatsNetwork() {
    }

    public static void register(SimpleChannel channel, AtomicInteger id) {
        channel.messageBuilder(PlayerStatsSyncPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayerStatsSyncPacket::encode)
                .decoder(PlayerStatsSyncPacket::decode)
                .consumerMainThread(PlayerStatsSyncPacket::handle)
                .add();

        channel.messageBuilder(DerivedStatsSyncPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DerivedStatsSyncPacket::encode)
                .decoder(DerivedStatsSyncPacket::decode)
                .consumerMainThread(DerivedStatsSyncPacket::handle)
                .add();

        channel.messageBuilder(AllocateStatPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(AllocateStatPacket::encode)
                .decoder(AllocateStatPacket::decode)
                .consumerMainThread(AllocateStatPacket::handle)
                .add();

        channel.messageBuilder(DeallocateStatPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeallocateStatPacket::encode)
                .decoder(DeallocateStatPacket::decode)
                .consumerMainThread(DeallocateStatPacket::handle)
                .add();
    }
}
