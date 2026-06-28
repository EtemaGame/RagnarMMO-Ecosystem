package com.etema.ragnarmmo.player.character.client;

import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.core.RagnarMMOCore;
import com.etema.ragnarmmo.player.character.net.ServerboundReturnToCharacterSelectPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCore.MOD_ID, value = Dist.CLIENT)
public final class CharacterClientEvents {
    private CharacterClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) {
            return;
        }
        int x = event.getScreen().width / 2 - 102;
        int y = event.getScreen().height / 4 + 144;
        event.addListener(Button.builder(Component.literal("Character Select"), button ->
                        Network.sendToServer(new ServerboundReturnToCharacterSelectPacket()))
                .bounds(x, y, 204, 20)
                .build());
    }
}
