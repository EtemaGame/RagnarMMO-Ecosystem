package com.etema.ragnarmmo.economy.zeny.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WalletSyncPacket {
    public final long zeny;

    public WalletSyncPacket(long zeny) {
        this.zeny = zeny;
    }

    public static void encode(WalletSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.zeny);
    }

    public static WalletSyncPacket decode(FriendlyByteBuf buf) {
        return new WalletSyncPacket(buf.readLong());
    }

    public static void handle(WalletSyncPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.etema.ragnarmmo.economy.client.EconomyClientPacketHandler.handleWalletSync(msg)));
        ctx.setPacketHandled(true);
    }
}
