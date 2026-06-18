package com.etema.ragnarmmo.combat.net;

import com.etema.ragnarmmo.combat.api.CombatActionType;
import com.etema.ragnarmmo.combat.api.CombatRequestContext;
import com.etema.ragnarmmo.combat.api.CombatTargetCandidate;
import com.etema.ragnarmmo.combat.engine.RagnarCombatEngine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record ServerboundRagnarSkillUsePacket(int sequenceId, String skillId, int skillLevel, int targetEntityId) {
    public ServerboundRagnarSkillUsePacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readUtf(), buffer.readVarInt(), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(sequenceId);
        buffer.writeUtf(skillId == null ? "" : skillId);
        buffer.writeVarInt(skillLevel);
        buffer.writeVarInt(targetEntityId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            RagnarCombatEngine.get().handleSkillUseRequest(new CombatRequestContext(
                    player,
                    CombatActionType.SKILL,
                    sequenceId,
                    0,
                    false,
                    player.getInventory().selected,
                    skillId,
                    List.of(new CombatTargetCandidate(targetEntityId, "packet", 0.0D, false)),
                    java.util.Map.of("level", skillLevel)));
        });
        context.setPacketHandled(true);
    }
}
