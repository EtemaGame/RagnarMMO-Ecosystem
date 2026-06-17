package com.etema.ragnarmmo.combat.net;

import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundRagnarCombatResultPacket(
        int targetEntityId,
        CombatHitResultType resultType,
        float damage,
        boolean critical) {

    public ClientboundRagnarCombatResultPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readEnum(CombatHitResultType.class), buffer.readFloat(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(targetEntityId);
        buffer.writeEnum(resultType);
        buffer.writeFloat(damage);
        buffer.writeBoolean(critical);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().setPacketHandled(true);
    }
}
