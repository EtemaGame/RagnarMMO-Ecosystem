package com.etema.ragnarmmo.items.hook;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.items.data.RoItemRule;
import com.etema.ragnarmmo.items.runtime.RoItemRuleResolver;
import com.etema.ragnarmmo.items.runtime.RoRequirementChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID)
public final class RoCombatRestrictionHook {
    private static final Map<UUID, Long> COMBAT_MSG_COOLDOWNS = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000L;

    private RoCombatRestrictionHook() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack weapon = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (weapon.isEmpty()) {
            return;
        }

        RoItemRule rule = RoItemRuleResolver.resolve(weapon);
        if (!rule.hasRequirements()) {
            return;
        }

        RoRequirementChecker.CheckResult result = RoRequirementChecker.check(player, rule);
        if (result != RoRequirementChecker.CheckResult.OK && result != RoRequirementChecker.CheckResult.NO_STATS_DATA) {
            sendCombatWarning(player, result, rule);
        }
    }

    private static void sendCombatWarning(ServerPlayer player, RoRequirementChecker.CheckResult result, RoItemRule rule) {
        long now = System.currentTimeMillis();
        Long lastTime = COMBAT_MSG_COOLDOWNS.get(player.getUUID());
        if (lastTime != null && (now - lastTime) < COOLDOWN_MS) {
            return;
        }
        COMBAT_MSG_COOLDOWNS.put(player.getUUID(), now);

        Component message = switch (result) {
            case LEVEL_TOO_LOW -> Component.translatable("message.ragnarmmo.roitems.level_required", rule.requiredBaseLevel());
            case WRONG_CLASS -> Component.translatable("message.ragnarmmo.roitems.class_required");
            default -> Component.translatable("message.ragnarmmo.roitems.weapon_ineffective");
        };
        player.displayClientMessage(message, true);
    }
}
