package com.etema.ragnarmmo.lifeskills;

import com.etema.ragnarmmo.RagnarMMO;
import com.etema.ragnarmmo.common.net.Network;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class RagnarMMOLifeSkills {
    public static final String MOD_ID = RagnarMMO.MOD_ID;
    public static final String LEGACY_NAMESPACE = "ragnarmmo";
    public static final Logger LOGGER = LogUtils.getLogger();

    private RagnarMMOLifeSkills() {
    }

    public static void init() {
        Network.registerPackets(LifeSkillsNetwork::register);
    }
}
