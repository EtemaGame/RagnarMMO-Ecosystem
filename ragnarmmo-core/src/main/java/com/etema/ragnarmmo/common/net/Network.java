package com.etema.ragnarmmo.common.net;

import com.etema.ragnarmmo.core.RagnarMMOCore;
import com.etema.ragnarmmo.player.stats.network.StatsNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public final class Network {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CH = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(RagnarMMOCore.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);
    private static boolean corePacketsRegistered;

    private Network() {
    }

    public static SimpleChannel channel() {
        return CH;
    }

    public static int nextPacketId() {
        return NEXT_ID.getAndIncrement();
    }

    public static void registerCorePackets() {
        if (corePacketsRegistered) {
            return;
        }
        StatsNetwork.register(CH, NEXT_ID);
        corePacketsRegistered = true;
    }

    public static void registerPackets(BiConsumer<SimpleChannel, AtomicInteger> registrar) {
        if (registrar != null) {
            registrar.accept(CH, NEXT_ID);
        }
    }

    public static <T> void sendToPlayer(ServerPlayer player, T msg) {
        if (player == null || player.connection == null) {
            return;
        }
        CH.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static <T> void sendToServer(T msg) {
        CH.sendToServer(msg);
    }

    public static <T> void sendTrackingEntityAndSelf(Entity entity, T msg) {
        if (entity == null) {
            return;
        }
        CH.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }

    public static <T> void sendToTrackingEntity(Entity entity, T msg) {
        if (entity == null) {
            return;
        }
        CH.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }
}
