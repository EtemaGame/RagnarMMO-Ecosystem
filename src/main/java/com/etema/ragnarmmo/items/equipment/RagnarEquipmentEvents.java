package com.etema.ragnarmmo.items.equipment;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.player.character.runtime.CharacterSelectionService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID)
public final class RagnarEquipmentEvents {
    private RagnarEquipmentEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (CharacterSelectionService.isSelectionRequired(player)) {
                return;
            }
            RagnarEquipmentSync.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RagnarEquipmentSync.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RagnarEquipmentSync.sync(player);
        }
    }
}
