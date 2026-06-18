package com.etema.ragnarmmo.items.hook;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.items.data.RoItemRule;
import com.etema.ragnarmmo.items.runtime.RoAttributeApplier;
import com.etema.ragnarmmo.items.runtime.RoItemRuleResolver;
import com.etema.ragnarmmo.items.runtime.RoRequirementChecker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID)
public final class RoHotbarTickHook {
    private static final int CHECK_INTERVAL_TICKS = 5;
    private static final Map<UUID, HandCache> PLAYER_HAND_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> TICK_COUNTERS = new ConcurrentHashMap<>();

    private RoHotbarTickHook() {
    }

    private record HandCache(ItemStack mainhandItem, ItemStack offhandItem) {
        static HandCache from(ServerPlayer player) {
            return new HandCache(player.getMainHandItem().copy(), player.getOffhandItem().copy());
        }

        boolean mainhandChanged(HandCache other) {
            return other == null || !ItemStack.isSameItemSameTags(mainhandItem, other.mainhandItem);
        }

        boolean offhandChanged(HandCache other) {
            return other == null || !ItemStack.isSameItemSameTags(offhandItem, other.offhandItem);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        UUID playerId = player.getUUID();
        int tickCounter = TICK_COUNTERS.getOrDefault(playerId, 0) + 1;
        if (tickCounter < CHECK_INTERVAL_TICKS) {
            TICK_COUNTERS.put(playerId, tickCounter);
            return;
        }
        TICK_COUNTERS.put(playerId, 0);

        HandCache current = HandCache.from(player);
        HandCache cached = PLAYER_HAND_CACHE.put(playerId, current);
        if (current.mainhandChanged(cached)) {
            processSlotChange(player, EquipmentSlot.MAINHAND);
        }
        if (current.offhandChanged(cached)) {
            processSlotChange(player, EquipmentSlot.OFFHAND);
        }
    }

    private static void processSlotChange(ServerPlayer player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            RoAttributeApplier.applySlotBonuses(player, slot, RoItemRule.EMPTY, false);
            return;
        }

        RoItemRule rule = RoItemRuleResolver.resolve(stack);
        RoAttributeApplier.applySlotBonuses(player, slot, rule, RoRequirementChecker.meetsRequirements(player, rule));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID id = player.getUUID();
            PLAYER_HAND_CACHE.remove(id);
            TICK_COUNTERS.remove(id);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PLAYER_HAND_CACHE.put(player.getUUID(), HandCache.from(player));
            TICK_COUNTERS.put(player.getUUID(), 0);
        }
    }
}
