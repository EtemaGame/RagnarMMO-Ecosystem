package com.etema.ragnarmmo.client.combat;

import com.etema.ragnarmmo.combat.net.ServerboundRagnarBasicAttackPacket;
import com.etema.ragnarmmo.common.net.Network;

final class CombatPacketBridge {
    private CombatPacketBridge() {
    }

    static void sendBasicAttack(int sequenceId, int targetEntityId, boolean offHand) {
        Network.sendToServer(new ServerboundRagnarBasicAttackPacket(sequenceId, targetEntityId, offHand));
    }
}
