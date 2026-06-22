package com.etema.ragnarmmo.combat;

import com.etema.ragnarmmo.RagnarMMO;
import com.etema.ragnarmmo.combat.net.CombatNetwork;
import com.etema.ragnarmmo.common.net.Network;

public final class RagnarMMOCombat {
    public static final String MOD_ID = RagnarMMO.MOD_ID;

    private RagnarMMOCombat() {
    }

    public static void init() {
        Network.registerPackets(CombatNetwork::register);
    }
}
