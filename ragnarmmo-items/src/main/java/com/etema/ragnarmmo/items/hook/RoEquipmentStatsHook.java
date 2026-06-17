package com.etema.ragnarmmo.items.hook;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.items.runtime.RoAttributeApplier;
import com.etema.ragnarmmo.player.stats.compute.StatResolutionService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID)
public final class RoEquipmentStatsHook {
    private RoEquipmentStatsHook() {
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        RoAttributeApplier.refreshSlot(player, event.getSlot());
        resolve(player);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RoAttributeApplier.refreshAllSlots(player);
            resolve(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RoAttributeApplier.refreshAllSlots(player);
            resolve(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RoAttributeApplier.refreshAllSlots(player);
            resolve(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RoAttributeApplier.clearAllBonuses(player);
        }
    }

    private static void resolve(ServerPlayer player) {
        RagnarCoreAPI.get(player).ifPresent(stats -> StatResolutionService.resolve(player, stats));
    }
}
