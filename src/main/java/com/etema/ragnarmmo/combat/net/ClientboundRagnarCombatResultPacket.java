package com.etema.ragnarmmo.combat.net;

import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import com.etema.ragnarmmo.combat.client.render.CombatPopoffHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundRagnarCombatResultPacket(
        int attackerId,
        int targetEntityId,
        CombatHitResultType resultType,
        float damage,
        boolean critical) {

    public ClientboundRagnarCombatResultPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarInt(), buffer.readEnum(CombatHitResultType.class),
                buffer.readFloat(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(attackerId);
        buffer.writeVarInt(targetEntityId);
        buffer.writeEnum(resultType);
        buffer.writeFloat(damage);
        buffer.writeBoolean(critical);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().setPacketHandled(true);
        String text;
        int color;
        switch (resultType) {
            case HIT -> {
                text = String.valueOf(Math.max(1, Math.round(damage)));
                color = critical ? 0xFFFF5C5C : 0xFFE8EDF5;
            }
            case MISS, DODGE -> {
                text = resultType == CombatHitResultType.DODGE
                        ? Component.translatable("message.ragnarmmo.combat_result.dodge").getString()
                        : Component.translatable("message.ragnarmmo.combat_result.miss").getString();
                color = 0xFF9BA7B4;
            }
            case BLOCKED -> {
                text = Component.translatable("message.ragnarmmo.combat_result.blocked").getString();
                color = 0xFF8FD7FF;
            }
            default -> {
                return;
            }
        }
        CombatPopoffHandler.addPopoff(targetEntityId, text, color);
    }
}
