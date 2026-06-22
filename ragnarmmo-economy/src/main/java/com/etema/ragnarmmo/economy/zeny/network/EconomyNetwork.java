package com.etema.ragnarmmo.economy.zeny.network;

import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public final class EconomyNetwork {
    private static boolean registered;

    private EconomyNetwork() {
    }

    public static synchronized void registerOnce() {
        if (registered) {
            return;
        }
        com.etema.ragnarmmo.common.net.Network.registerPackets(EconomyNetwork::register);
        registered = true;
    }

    public static void register(SimpleChannel channel, AtomicInteger idGen) {
        channel.messageBuilder(WalletSyncPacket.class, idGen.getAndIncrement())
                .encoder(WalletSyncPacket::encode)
                .decoder(WalletSyncPacket::decode)
                .consumerNetworkThread(WalletSyncPacket::handle)
                .add();

        channel.messageBuilder(ZenyBagActionPacket.class, idGen.getAndIncrement())
                .encoder(ZenyBagActionPacket::encode)
                .decoder(ZenyBagActionPacket::decode)
                .consumerNetworkThread(ZenyBagActionPacket::handle)
                .add();
    }
}
