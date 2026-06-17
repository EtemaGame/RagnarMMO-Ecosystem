package com.etema.ragnarmmo.combat.net;

import com.etema.ragnarmmo.combat.api.BasicAttackSource;
import com.etema.ragnarmmo.combat.api.RagnarAttackRequest;
import com.etema.ragnarmmo.combat.engine.RagnarCombatEngine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundRagnarBasicAttackPacket(int sequenceId, int targetEntityId, boolean offHand) {
    public ServerboundRagnarBasicAttackPacket(int sequenceId, int targetEntityId) {
        this(sequenceId, targetEntityId, false);
    }

    public ServerboundRagnarBasicAttackPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(sequenceId);
        buffer.writeVarInt(targetEntityId);
        buffer.writeBoolean(offHand);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                RagnarCombatEngine.get().processBasicAttackRequest(player,
                        RagnarAttackRequest.singleTarget(sequenceId, targetEntityId, offHand),
                        BasicAttackSource.CLIENT_PACKET);
            }
        });
        context.setPacketHandled(true);
    }
}
