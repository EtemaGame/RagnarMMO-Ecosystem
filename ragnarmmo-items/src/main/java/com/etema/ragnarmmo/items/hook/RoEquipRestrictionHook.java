package com.etema.ragnarmmo.items.hook;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.items.data.RoItemRule;
import com.etema.ragnarmmo.items.runtime.RoAttributeApplier;
import com.etema.ragnarmmo.items.runtime.RoItemRuleResolver;
import com.etema.ragnarmmo.items.runtime.RoRequirementChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID)
public final class RoEquipRestrictionHook {
    private static final Map<UUID, Long> MESSAGE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000L;

    private RoEquipRestrictionHook() {
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        EquipmentSlot slot = event.getSlot();
        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        RoAttributeApplier.applySlotBonuses(player, slot, RoItemRule.EMPTY, false);
        if (to.isEmpty()) {
            return;
        }

        RoItemRule rule = RoItemRuleResolver.resolve(to);
        if (!rule.hasRequirements()) {
            RoAttributeApplier.applySlotBonuses(player, slot, rule, true);
        } else {
            RoRequirementChecker.CheckResult result = RoRequirementChecker.check(player, rule);
            if (result == RoRequirementChecker.CheckResult.OK || result == RoRequirementChecker.CheckResult.NO_STATS_DATA) {
                RoAttributeApplier.applySlotBonuses(player, slot, rule, true);
            } else if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                sendRateLimitedMessage(player, result, rule);
                revertEquipment(player, slot, from, to);
            }
        }

        RagnarCoreAPI.get(player).ifPresent(stats -> {
            if (stats instanceof com.etema.ragnarmmo.player.stats.capability.PlayerStats internal) {
                internal.markDirty(com.etema.ragnarmmo.common.api.player.RoPlayerSyncDomain.STATS);
            }
        });
    }

    @SubscribeEvent
    public static void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }

        RoItemRule rule = RoItemRuleResolver.resolve(stack);
        if (!rule.hasRequirements()) {
            return;
        }

        RoRequirementChecker.CheckResult result = RoRequirementChecker.check(player, rule);
        if (result != RoRequirementChecker.CheckResult.OK && result != RoRequirementChecker.CheckResult.NO_STATS_DATA) {
            event.setCanceled(true);
            sendRateLimitedMessage(player, result, rule);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        checkAndCancel(event, event.getEntity(), event.getItemStack());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        checkAndCancel(event, event.getEntity(), event.getItemStack());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        checkAndCancel(event, event.getEntity(), event.getItemStack());
    }

    private static void checkAndCancel(net.minecraftforge.eventbus.api.Event event,
            net.minecraft.world.entity.LivingEntity entity,
            ItemStack stack) {
        if (entity.level().isClientSide() || !(entity instanceof ServerPlayer player) || stack.isEmpty()) {
            return;
        }

        RoItemRule rule = RoItemRuleResolver.resolve(stack);
        if (!rule.hasRequirements()) {
            return;
        }

        RoRequirementChecker.CheckResult result = RoRequirementChecker.check(player, rule);
        if (result != RoRequirementChecker.CheckResult.OK && result != RoRequirementChecker.CheckResult.NO_STATS_DATA) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
            sendRateLimitedMessage(player, result, rule);
        }
    }

    private static void revertEquipment(ServerPlayer player, EquipmentSlot slot, ItemStack oldStack, ItemStack rejectedStack) {
        player.setItemSlot(slot, oldStack.copy());
        if (!rejectedStack.isEmpty() && !player.getInventory().add(rejectedStack)) {
            player.drop(rejectedStack, false);
        }
        player.inventoryMenu.broadcastChanges();
    }

    private static void sendRateLimitedMessage(ServerPlayer player, RoRequirementChecker.CheckResult result, RoItemRule rule) {
        long now = System.currentTimeMillis();
        Long lastTime = MESSAGE_COOLDOWNS.get(player.getUUID());
        if (lastTime != null && (now - lastTime) < COOLDOWN_MS) {
            return;
        }

        MESSAGE_COOLDOWNS.put(player.getUUID(), now);

        Component message = switch (result) {
            case LEVEL_TOO_LOW -> Component.translatable("message.ragnarmmo.roitems.level_required", rule.requiredBaseLevel());
            case WRONG_CLASS -> Component.translatable("message.ragnarmmo.roitems.class_required");
            default -> Component.translatable("message.ragnarmmo.roitems.cannot_equip");
        };
        player.displayClientMessage(message, true);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MESSAGE_COOLDOWNS.remove(player.getUUID());
        }
    }
}
