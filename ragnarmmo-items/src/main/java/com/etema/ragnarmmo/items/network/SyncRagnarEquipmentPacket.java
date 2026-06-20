package com.etema.ragnarmmo.items.network;

import com.etema.ragnarmmo.items.client.RagnarEquipmentClientSyncHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SyncRagnarEquipmentPacket {
    private final CompoundTag equipment;

    public SyncRagnarEquipmentPacket(CompoundTag equipment) {
        this.equipment = equipment == null ? new CompoundTag() : equipment;
    }

    public CompoundTag equipment() {
        return equipment;
    }

    public static void encode(SyncRagnarEquipmentPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.equipment);
    }

    public static SyncRagnarEquipmentPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new SyncRagnarEquipmentPacket(tag == null ? new CompoundTag() : tag);
    }

    public static void handle(SyncRagnarEquipmentPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        var ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> RagnarEquipmentClientSyncHandler.handle(msg)));
        ctx.setPacketHandled(true);
    }
}
