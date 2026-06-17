package com.etema.ragnarmmo.combat.net;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public final class CombatNetwork {
    private static boolean registered;

    private CombatNetwork() {
    }

    public static void register(SimpleChannel channel, AtomicInteger nextId) {
        if (registered) {
            return;
        }

        channel.messageBuilder(ServerboundRagnarBasicAttackPacket.class, nextId.getAndIncrement(),
                        NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundRagnarBasicAttackPacket::encode)
                .decoder(ServerboundRagnarBasicAttackPacket::new)
                .consumerMainThread(ServerboundRagnarBasicAttackPacket::handle)
                .add();

        channel.messageBuilder(ClientboundRagnarCombatResultPacket.class, nextId.getAndIncrement(),
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundRagnarCombatResultPacket::encode)
                .decoder(ClientboundRagnarCombatResultPacket::new)
                .consumerMainThread(ClientboundRagnarCombatResultPacket::handle)
                .add();

        registered = true;
    }
}
