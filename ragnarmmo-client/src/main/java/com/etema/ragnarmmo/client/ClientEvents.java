package com.etema.ragnarmmo.client;

import com.etema.ragnarmmo.client.ui.AchievementsScreen;
import com.etema.ragnarmmo.client.ui.RagnarMainMenuScreen;
import com.etema.ragnarmmo.client.ui.SkillsScreen;
import com.etema.ragnarmmo.client.ui.StatsScreen;
import com.etema.ragnarmmo.client.ui.BestiaryScreen;
import com.etema.ragnarmmo.jobs.client.JobSkillsClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOClient.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
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

        while (Keybinds.OPEN_MENU.consumeClick()) {
            minecraft.setScreen(new RagnarMainMenuScreen());
        }
        while (Keybinds.OPEN_STATS.consumeClick()) {
            minecraft.setScreen(new StatsScreen());
        }
        while (Keybinds.OPEN_SKILLS.consumeClick()) {
            minecraft.setScreen(new SkillsScreen(null));
        }
        while (Keybinds.OPEN_ACHIEVEMENTS.consumeClick()) {
            minecraft.setScreen(new AchievementsScreen());
        }
        while (Keybinds.OPEN_BESTIARY.consumeClick()) {
            minecraft.setScreen(new BestiaryScreen());
        }
        while (Keybinds.TOGGLE_COMBAT_MODE.consumeClick()) {
            ClientCombatState.toggleCombatMode();
            minecraft.player.displayClientMessage(Component.translatable(
                    ClientCombatState.isCombatModeEnabled()
                            ? "message.ragnarmmo.combat_mode.on"
                            : "message.ragnarmmo.combat_mode.off"), true);
        }
        if (minecraft.screen == null && ClientCombatState.isCombatModeEnabled()) {
            for (int i = 0; i < Keybinds.HOTBAR_KEYS.length; i++) {
                while (Keybinds.HOTBAR_KEYS[i].consumeClick()) {
                    JobSkillsClientCache.requestUse(JobSkillsClientCache.getHotbarSlot(i));
                    if (i < minecraft.options.keyHotbarSlots.length) {
                        minecraft.options.keyHotbarSlots[i].consumeClick();
                    }
                }
            }
        }
    }
}
