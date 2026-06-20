package com.etema.ragnarmmo.items.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public final class RoItemsNetwork {
    private RoItemsNetwork() {
    }

    public static void register(SimpleChannel ch, AtomicInteger id) {
        ch.messageBuilder(SyncRoItemRulesPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncRoItemRulesPacket::encode)
                .decoder(SyncRoItemRulesPacket::decode)
                .consumerMainThread(SyncRoItemRulesPacket::handle)
                .add();

        ch.messageBuilder(SyncRagnarEquipmentPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncRagnarEquipmentPacket::encode)
                .decoder(SyncRagnarEquipmentPacket::decode)
                .consumerMainThread(SyncRagnarEquipmentPacket::handle)
                .add();

        ch.messageBuilder(CardCompoundPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(CardCompoundPacket::encode)
                .decoder(CardCompoundPacket::decode)
                .consumerMainThread(CardCompoundPacket::handle)
                .add();
    }
}
