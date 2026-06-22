package com.etema.ragnarmmo.social;

import com.etema.ragnarmmo.RagnarMMO;
import com.etema.ragnarmmo.achievements.network.AchievementNetwork;
import com.etema.ragnarmmo.bestiary.network.BestiaryNetwork;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.player.command.PartyCommands;
import com.etema.ragnarmmo.player.party.net.PartyNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

public final class RagnarMMOSocial {
    public static final String MOD_ID = RagnarMMO.MOD_ID;
    public static final String LEGACY_NAMESPACE = "ragnarmmo";
    public static final Logger LOGGER = LogUtils.getLogger();

    private RagnarMMOSocial() {
    }

    public static void init() {
        Network.registerPackets(AchievementNetwork::register);
        Network.registerPackets(BestiaryNetwork::register);
        Network.registerPackets(PartyNetwork::register);
        MinecraftForge.EVENT_BUS.addListener(RagnarMMOSocial::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(PartyCommands.createNode());
    }
}
