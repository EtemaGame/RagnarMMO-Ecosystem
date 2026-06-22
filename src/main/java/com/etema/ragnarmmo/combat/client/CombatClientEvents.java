package com.etema.ragnarmmo.combat.client;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID, value = Dist.CLIENT)
public final class CombatClientEvents {
    private CombatClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        while (CombatKeybinds.TOGGLE_COMBAT_MODE.consumeClick()) {
            ClientCombatState.toggleCombatMode();
            minecraft.player.displayClientMessage(Component.translatable(
                    ClientCombatState.isCombatModeEnabled()
                            ? "message.ragnarmmo.combat_mode.on"
                            : "message.ragnarmmo.combat_mode.off"), true);
        }
    }
}
