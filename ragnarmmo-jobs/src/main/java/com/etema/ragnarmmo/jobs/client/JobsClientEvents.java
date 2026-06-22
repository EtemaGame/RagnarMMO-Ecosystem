package com.etema.ragnarmmo.jobs.client;

import com.etema.ragnarmmo.combat.client.ClientCombatState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.jobs.RagnarMMOJobs.MOD_ID, value = Dist.CLIENT)
public final class JobsClientEvents {
    private JobsClientEvents() {
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

        if (minecraft.screen == null && ClientCombatState.isCombatModeEnabled()) {
            for (int i = 0; i < JobsKeybinds.HOTBAR_KEYS.length; i++) {
                while (JobsKeybinds.HOTBAR_KEYS[i].consumeClick()) {
                    JobSkillsClientCache.requestUse(JobSkillsClientCache.getHotbarSlot(i));
                    if (i < minecraft.options.keyHotbarSlots.length) {
                        minecraft.options.keyHotbarSlots[i].consumeClick();
                    }
                }
            }
        }
    }
}
