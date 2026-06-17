package com.etema.ragnarmmo.social;

import com.etema.ragnarmmo.achievements.network.AchievementNetwork;
import com.etema.ragnarmmo.bestiary.network.BestiaryNetwork;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.player.command.PartyCommands;
import com.etema.ragnarmmo.player.party.net.PartyNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RagnarMMOSocial.MOD_ID)
public final class RagnarMMOSocial {
    public static final String MOD_ID = "ragnarmmo_social";
    public static final String LEGACY_NAMESPACE = "ragnarmmo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RagnarMMOSocial() {
        Network.registerPackets(AchievementNetwork::register);
        Network.registerPackets(BestiaryNetwork::register);
        Network.registerPackets(PartyNetwork::register);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(PartyCommands.createNode());
    }
}
