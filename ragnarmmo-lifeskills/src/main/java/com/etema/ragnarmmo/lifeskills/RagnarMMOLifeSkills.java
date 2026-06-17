package com.etema.ragnarmmo.lifeskills;

import com.etema.ragnarmmo.common.net.Network;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RagnarMMOLifeSkills.MOD_ID)
public final class RagnarMMOLifeSkills {
    public static final String MOD_ID = "ragnarmmo_lifeskills";
    public static final String LEGACY_NAMESPACE = "ragnarmmo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RagnarMMOLifeSkills() {
        Network.registerPackets(LifeSkillsNetwork::register);
    }
}
