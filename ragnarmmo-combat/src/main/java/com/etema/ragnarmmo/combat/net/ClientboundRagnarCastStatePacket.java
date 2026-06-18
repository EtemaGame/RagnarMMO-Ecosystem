package com.etema.ragnarmmo.combat.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundRagnarCastStatePacket(int casterId, String skillId, CastState state, int durationTicks) {
    public enum CastState {
        STARTED,
        INTERRUPTED,
        COMPLETED,
        FAILED
    }

    public ClientboundRagnarCastStatePacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readUtf(), buffer.readEnum(CastState.class), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(casterId);
        buffer.writeUtf(skillId == null ? "" : skillId);
        buffer.writeEnum(state);
        buffer.writeVarInt(durationTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().setPacketHandled(true);
    }
}
