package com.etema.ragnarmmo.combat;

import com.etema.ragnarmmo.combat.net.CombatNetwork;
import com.etema.ragnarmmo.common.net.Network;
import net.minecraftforge.fml.common.Mod;

@Mod(RagnarMMOCombat.MOD_ID)
public final class RagnarMMOCombat {
    public static final String MOD_ID = "ragnarmmo_combat";

    public RagnarMMOCombat() {
        Network.registerPackets(CombatNetwork::register);
    }
}
