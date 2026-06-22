package com.etema.ragnarmmo.core.client;

import com.etema.ragnarmmo.core.RagnarMMOCore;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CoreClientCommands {
    private CoreClientCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        var root = dispatcher.register(Commands.literal("r")
                .then(Commands.literal("hud").executes(ctx -> openHud())));
        dispatcher.register(Commands.literal("hud").redirect(root.getChild("hud")));
    }

    private static int openHud() {
        Minecraft minecraft = Minecraft.getInstance();
        HudOverlayScreenRegistry.open(minecraft);
        return 1;
    }
}
