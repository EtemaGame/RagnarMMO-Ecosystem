package com.etema.ragnarmmo.combat.ground;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GroundCellService {
    private static final Map<ResourceKey<Level>, Map<BlockPos, GroundCell>> CELLS = new ConcurrentHashMap<>();
    private static final ResourceLocation FIRE_WALL_ID = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "fire_wall");
    private static final int FIRE_WALL_HIT_INTERVAL_TICKS = 10;

    private GroundCellService() {
    }

    public static AABB cellBounds(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 2.0D, pos.getZ() + 1.0D);
    }

    public static void placeArea(ServerLevel level, BlockPos center, int radius, GroundCellType type, ServerPlayer owner,
            int durationTicks, int maxHits, int skillLevel) {
        long expiresAt = level.getGameTime() + Math.max(1, durationTicks);
        Map<BlockPos, GroundCell> cells = CELLS.computeIfAbsent(level.dimension(), ignored -> new ConcurrentHashMap<>());
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos pos = center.offset(x, 0, z).immutable();
                putReplacing(cells, new GroundCell(type, pos, owner.getUUID(), expiresAt, maxHits, skillLevel));
            }
        }
    }

    public static void placeSingle(ServerLevel level, BlockPos pos, GroundCellType type, ServerPlayer owner,
            int durationTicks, int maxHits, int skillLevel) {
        long expiresAt = level.getGameTime() + Math.max(1, durationTicks);
        Map<BlockPos, GroundCell> cells = CELLS.computeIfAbsent(level.dimension(), ignored -> new ConcurrentHashMap<>());
        putReplacing(cells, new GroundCell(type, pos.immutable(), owner.getUUID(), expiresAt, maxHits, skillLevel));
    }

    public static void placeFireWall(ServerLevel level, ServerPlayer owner, BlockPos center, int segmentCount,
            int durationTicks, int maxHits, int skillLevel) {
        List<BlockPos> positions = fireWallPositions(owner, center, Math.max(1, segmentCount));
        int hitsPerCell = Math.max(1, maxHits);
        long expiresAt = level.getGameTime() + Math.max(1, durationTicks);
        Map<BlockPos, GroundCell> cells = CELLS.computeIfAbsent(level.dimension(), ignored -> new ConcurrentHashMap<>());
        for (BlockPos pos : positions) {
            putReplacing(cells, new GroundCell(GroundCellType.FIRE_WALL, pos.immutable(), owner.getUUID(),
                    expiresAt, hitsPerCell, skillLevel));
        }
    }

    private static void putReplacing(Map<BlockPos, GroundCell> cells, GroundCell cell) {
        // Current first-class scope uses one ground effect per block. Later systems can replace this with RO map flags.
        cells.put(cell.pos, cell);
    }

    public static List<LivingEntity> livingEntitiesInCells(ServerLevel level, BlockPos center, int radius, ServerPlayer owner) {
        AABB area = new AABB(
                center.getX() - radius,
                center.getY(),
                center.getZ() - radius,
                center.getX() + radius + 1.0D,
                center.getY() + 2.0D,
                center.getZ() + radius + 1.0D);
        return level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && entity != owner);
    }

    public static boolean blocksPhysicalRanged(LivingEntity target) {
        return firstMatching(target, GroundCellType.PNEUMA) != null;
    }

    public static boolean consumePhysicalMeleeBlock(LivingEntity target) {
        return consumePhysicalMeleeBlocks(target, 1) > 0;
    }

    public static int consumePhysicalMeleeBlocks(LivingEntity target, int hitCount) {
        int remaining = Math.max(1, hitCount);
        int blocked = 0;
        while (remaining > 0) {
            GroundCell cell = firstMatching(target, GroundCellType.SAFETY_WALL);
            if (cell == null) {
                break;
            }
            int consumed = cell.remainingHits < 0 ? remaining : Math.min(remaining, cell.remainingHits);
            blocked += consumed;
            remaining -= consumed;
            if (cell.remainingHits >= 0) {
                cell.remainingHits -= consumed;
                if (cell.remainingHits <= 0) {
                    remove(target.level(), cell.pos);
                }
            }
            if (consumed <= 0 || cell.remainingHits < 0) {
                break;
            }
        }
        return blocked;
    }

    public static void tick(ServerLevel level) {
        Map<BlockPos, GroundCell> cells = CELLS.get(level.dimension());
        if (cells == null || cells.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        List<BlockPos> expired = new ArrayList<>();
        for (GroundCell cell : cells.values()) {
            if (cell.expiresAt <= now || cell.remainingHits == 0) {
                expired.add(cell.pos);
                continue;
            }
            spawnCellParticle(level, cell);
            if (cell.type == GroundCellType.FIRE_WALL) {
                tickFireWall(level, cell, now);
            }
        }
        for (BlockPos pos : expired) {
            cells.remove(pos);
        }
        if (cells.isEmpty()) {
            CELLS.remove(level.dimension());
        }
    }

    private static void tickFireWall(ServerLevel level, GroundCell cell, long now) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(cell.owner);
        if (owner == null || !owner.isAlive()) {
            return;
        }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, cellBounds(cell.pos),
                entity -> entity.isAlive() && entity != owner)) {
            int entityId = target.getId();
            long nextHit = cell.nextHitByEntity.getOrDefault(entityId, 0L);
            if (nextHit > now) {
                continue;
            }
            cell.nextHitByEntity.put(entityId, now + FIRE_WALL_HIT_INTERVAL_TICKS);
            boolean hit = RagnarCombatEngine.get().handleSkillUseRequest(new CombatRequestContext(
                    owner,
                    CombatActionType.SKILL,
                    0,
                    0,
                    false,
                    owner.getInventory().selected,
                    FIRE_WALL_ID.toString(),
                    List.of(new CombatTargetCandidate(entityId, "ground_cell", 0.0D, owner.hasLineOfSight(target))),
                    Map.of(
                            "level", cell.skillLevel,
                            "_cast_complete", true,
                            "_skip_timing", true,
                            "_hit_count_override", 1))).stream()
                    .anyMatch(resolution -> resolution.targetEntityId() == entityId && resolution.dealsDamage());
            if (hit) {
                knockAwayFromCell(cell.pos, target, 2.0D);
                cell.remainingHits--;
                if (cell.remainingHits <= 0) {
                    remove(level, cell.pos);
                    return;
                }
            }
        }
    }

    private static GroundCell firstMatching(LivingEntity target, GroundCellType type) {
        Map<BlockPos, GroundCell> cells = CELLS.get(target.level().dimension());
        if (cells == null || cells.isEmpty()) {
            return null;
        }
        long now = target.level().getGameTime();
        AABB bounds = target.getBoundingBox();
        for (GroundCell cell : cells.values()) {
            if (cell.type == type && cell.expiresAt > now && cellBounds(cell.pos).intersects(bounds)) {
                return cell;
            }
        }
        return null;
    }

    private static void remove(Level level, BlockPos pos) {
        Map<BlockPos, GroundCell> cells = CELLS.get(level.dimension());
        if (cells != null) {
            cells.remove(pos);
        }
    }

    private static List<BlockPos> fireWallPositions(ServerPlayer owner, BlockPos center, int segmentCount) {
        Vec3 look = owner.getLookAngle();
        boolean wallAlongX = Math.abs(look.x) < Math.abs(look.z);
        int half = segmentCount / 2;
        List<BlockPos> positions = new ArrayList<>();
        for (int i = -half; i <= half; i++) {
            positions.add(wallAlongX ? center.offset(i, 0, 0) : center.offset(0, 0, i));
        }
        return positions;
    }

    private static void knockAwayFromCell(BlockPos pos, LivingEntity target, double cells) {
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 delta = target.position().subtract(center);
        if (delta.lengthSqr() < 1.0E-4D) {
            delta = new Vec3(0.0D, 0.0D, 1.0D);
        }
        Vec3 push = delta.normalize().scale(cells * 0.35D);
        target.push(push.x, 0.15D, push.z);
        target.hurtMarked = true;
    }

    private static void spawnCellParticle(ServerLevel level, GroundCell cell) {
        double x = cell.pos.getX() + 0.5D;
        double y = cell.pos.getY() + 0.15D;
        double z = cell.pos.getZ() + 0.5D;
        switch (cell.type) {
            case PNEUMA -> level.sendParticles(ParticleTypes.CLOUD, x, y + 0.6D, z, 1, 0.25D, 0.25D, 0.25D, 0.01D);
            case SAFETY_WALL -> level.sendParticles(ParticleTypes.ENCHANT, x, y + 0.8D, z, 1, 0.2D, 0.35D, 0.2D, 0.01D);
            case FIRE_WALL -> level.sendParticles(ParticleTypes.FLAME, x, y, z, 2, 0.18D, 0.1D, 0.18D, 0.02D);
        }
    }

    private static final class GroundCell {
        private final GroundCellType type;
        private final BlockPos pos;
        private final UUID owner;
        private final long expiresAt;
        private final int skillLevel;
        private final Map<Integer, Long> nextHitByEntity = new HashMap<>();
        private int remainingHits;

        private GroundCell(GroundCellType type, BlockPos pos, UUID owner, long expiresAt, int remainingHits,
                int skillLevel) {
            this.type = type;
            this.pos = pos;
            this.owner = owner;
            this.expiresAt = expiresAt;
            this.remainingHits = remainingHits;
            this.skillLevel = skillLevel;
        }
    }
}
