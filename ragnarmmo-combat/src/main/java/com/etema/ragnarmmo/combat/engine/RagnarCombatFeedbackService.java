package com.etema.ragnarmmo.combat.engine;

import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import com.etema.ragnarmmo.combat.api.CombatResolution;
import com.etema.ragnarmmo.combat.net.ClientboundRagnarCombatResultPacket;
import com.etema.ragnarmmo.common.net.Network;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class RagnarCombatFeedbackService {
    public void sendBasicAttackFeedback(ServerPlayer attacker, LivingEntity target, CombatResolution resolution) {
        if (resolution == null) {
            return;
        }

        Entity trackingBasis = target != null ? target : attacker;
        if (trackingBasis != null) {
            Network.sendTrackingEntityAndSelf(trackingBasis, new ClientboundRagnarCombatResultPacket(
                    attacker != null ? attacker.getId() : -1,
                    resolution.targetEntityId(),
                    resolution.resultType(),
                    (float) resolution.finalDamage(),
                    resolution.critical()));
        }

        if (attacker != null
                && (resolution.resultType() == CombatHitResultType.MISS
                || resolution.resultType() == CombatHitResultType.DODGE)) {
            String label = resolution.resultType() == CombatHitResultType.DODGE ? "Dodge" : "Miss";
            attacker.sendSystemMessage(Component.literal("\u00A77" + label));
        }
    }
}
