package com.etema.ragnarmmo.combat.ground;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import com.etema.ragnarmmo.combat.api.CombatActionType;
import com.etema.ragnarmmo.combat.api.CombatRequestContext;
import com.etema.ragnarmmo.combat.api.CombatTargetCandidate;
import com.etema.ragnarmmo.combat.engine.RagnarCombatEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID)
public final class ThunderStormScheduler {
    private static final ResourceLocation THUNDER_STORM_ID = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "thunder_storm");
    private static final Map<ResourceKey<Level>, Map<UUID, PendingThunderStorm>> PENDING = new ConcurrentHashMap<>();

    private ThunderStormScheduler() {
    }

    public static void schedule(ServerLevel level, ServerPlayer owner, BlockPos center, int radius, int skillLevel,
            int hitCount, int spacingTicks) {
        if (level == null || owner == null || center == null) {
            return;
        }
        PendingThunderStorm storm = new PendingThunderStorm(
                owner.getUUID(),
                center.immutable(),
                Math.max(1, radius),
                Math.max(1, skillLevel),
                Math.max(1, hitCount),
                Math.max(1, spacingTicks),
                level.getGameTime());
        PENDING.computeIfAbsent(level.dimension(), ignored -> new ConcurrentHashMap<>()).put(UUID.randomUUID(), storm);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null || PENDING.isEmpty()) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            Map<UUID, PendingThunderStorm> storms = PENDING.get(level.dimension());
            if (storms == null || storms.isEmpty()) {
                continue;
            }
            long now = level.getGameTime();
            Iterator<Map.Entry<UUID, PendingThunderStorm>> iterator = storms.entrySet().iterator();
            while (iterator.hasNext()) {
                PendingThunderStorm storm = iterator.next().getValue();
                if (storm.remainingHits <= 0) {
                    iterator.remove();
                    continue;
                }
                if (now < storm.nextHitTick) {
                    continue;
                }
                ServerPlayer owner = level.getServer().getPlayerList().getPlayer(storm.owner);
                if (owner == null || !owner.isAlive()) {
                    iterator.remove();
                    continue;
                }
                strike(level, owner, storm);
                storm.remainingHits--;
                storm.nextHitTick = now + storm.spacingTicks;
                if (storm.remainingHits <= 0) {
                    iterator.remove();
                }
            }
            if (storms.isEmpty()) {
                PENDING.remove(level.dimension());
            }
        }
    }

    private static void strike(ServerLevel level, ServerPlayer owner, PendingThunderStorm storm) {
        List<LivingEntity> targets = GroundCellService.livingEntitiesInCells(level, storm.center, storm.radius, owner);
        for (LivingEntity target : targets) {
            RagnarCombatEngine.get().handleSkillUseRequest(new CombatRequestContext(
                    owner,
                    CombatActionType.SKILL,
                    0,
                    0,
                    false,
                    owner.getInventory().selected,
                    THUNDER_STORM_ID.toString(),
                    List.of(new CombatTargetCandidate(target.getId(), "thunder_storm_tick", 0.0D,
                            owner.hasLineOfSight(target))),
                    Map.of(
                            "level", storm.skillLevel,
                            "_cast_complete", true,
                            "_skip_timing", true,
                            "_hit_count_override", 1)));
        }
        double x = storm.center.getX() + 0.5D;
        double y = storm.center.getY() + 0.8D;
        double z = storm.center.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 18,
                storm.radius * 0.35D, 0.7D, storm.radius * 0.35D, 0.05D);
        level.playSound(null, storm.center, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.35F, 1.8F);
    }

    private static final class PendingThunderStorm {
        private final UUID owner;
        private final BlockPos center;
        private final int radius;
        private final int skillLevel;
        private final int spacingTicks;
        private int remainingHits;
        private long nextHitTick;

        private PendingThunderStorm(UUID owner, BlockPos center, int radius, int skillLevel, int remainingHits,
                int spacingTicks, long nextHitTick) {
            this.owner = owner;
            this.center = center;
            this.radius = radius;
            this.skillLevel = skillLevel;
            this.remainingHits = remainingHits;
            this.spacingTicks = spacingTicks;
            this.nextHitTick = nextHitTick;
        }
    }
}
