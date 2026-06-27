package com.etema.ragnarmmo.jobs.runtime;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import com.etema.ragnarmmo.combat.net.ClientboundRagnarCastStatePacket;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import com.etema.ragnarmmo.combat.timing.CombatTimingCalculator;
import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import com.etema.ragnarmmo.jobs.data.SkillDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID)
public final class JobSkillCastService {
    private static final Map<UUID, PendingCast> PENDING = new ConcurrentHashMap<>();

    private JobSkillCastService() {
    }

    public static boolean hasPendingCast(ServerPlayer player) {
        return player != null && PENDING.containsKey(player.getUUID());
    }

    public static boolean startOrRun(JobSkillContext context, int resourceCost) {
        if (context == null || context.player() == null || context.definition() == null) {
            return false;
        }
        ServerPlayer player = context.player();
        long now = player.level().getGameTime();
        if (RoCombatStatusService.blocksCast(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You cannot cast in your current status."));
            return false;
        }
        if (hasPendingCast(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Already casting."));
            return false;
        }

        SkillDefinition definition = context.definition();
        int castTicks = definition.getLevelInt("cast_time_ticks", context.level(), 0);
        int fixedCastTicks = definition.getLevelInt("fixed_cast_time_ticks", context.level(), 0);
        int afterCastDelay = definition.getLevelInt("cast_delay_ticks", context.level(), definition.castDelayTicks());
        int cooldown = Math.max(
                definition.getLevelInt("cooldown_ticks", context.level(), definition.cooldownTicks()),
                afterCastDelay);
        var timing = CombatTimingCalculator.resolveSkillTiming(player, castTicks, fixedCastTicks,
                afterCastDelay, afterCastDelay, cooldown);
        if (timing.totalCastTicks() <= 0) {
            return JobSkillExecutor.executeResolved(context, resourceCost, now, cooldown);
        }

        PendingCast pending = new PendingCast(
                context.skillId(),
                context.level(),
                context.target().map(LivingEntity::getId).orElse(null),
                context.groundTarget().orElse(null),
                resourceCost,
                now + timing.totalCastTicks(),
                cooldown);
        PENDING.put(player.getUUID(), pending);
        Network.sendTrackingEntityAndSelf(player, new ClientboundRagnarCastStatePacket(
                player.getId(),
                context.skillId().toString(),
                ClientboundRagnarCastStatePacket.CastState.STARTED,
                timing.totalCastTicks()));
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null || PENDING.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, PendingCast>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingCast> entry = iterator.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) {
                iterator.remove();
                continue;
            }
            if (RoCombatStatusService.blocksCast(player)) {
                PendingCast pending = entry.getValue();
                iterator.remove();
                Network.sendTrackingEntityAndSelf(player, new ClientboundRagnarCastStatePacket(
                        player.getId(),
                        pending.skillId().toString(),
                        ClientboundRagnarCastStatePacket.CastState.INTERRUPTED,
                        0));
                continue;
            }
            long now = player.level().getGameTime();
            if (now < entry.getValue().endTick()) {
                continue;
            }
            PendingCast pending = entry.getValue();
            iterator.remove();
            JobSkillExecutor.completeCast(player, pending.skillId(), pending.level(), pending.targetEntityId(),
                    pending.groundTarget(), pending.resourceCost(), pending.cooldownTicks(), now);
            Network.sendTrackingEntityAndSelf(player, new ClientboundRagnarCastStatePacket(
                    player.getId(),
                    pending.skillId().toString(),
                    ClientboundRagnarCastStatePacket.CastState.COMPLETED,
                    0));
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PendingCast pending = PENDING.get(player.getUUID());
        if (pending == null) {
            return;
        }
        double resist = RagnarCoreAPI.get(player)
                .flatMap(stats -> DerivedStatsService.compute(player, stats))
                .map(stats -> Math.max(0.0D, stats.castInterruptResist))
                .orElse(0.0D);
        if (resist >= 1.0D) {
            return;
        }
        PENDING.remove(player.getUUID());
        Network.sendTrackingEntityAndSelf(player, new ClientboundRagnarCastStatePacket(
                player.getId(),
                pending.skillId().toString(),
                ClientboundRagnarCastStatePacket.CastState.INTERRUPTED,
                0));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Cast interrupted."));
    }

    public record PendingCast(
            ResourceLocation skillId,
            int level,
            Integer targetEntityId,
            BlockPos groundTarget,
            int resourceCost,
            long endTick,
            int cooldownTicks) {
    }
}
