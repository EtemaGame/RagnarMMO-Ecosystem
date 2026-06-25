package com.etema.ragnarmmo.combat.targeting;

import com.etema.ragnarmmo.combat.api.RagnarTargetCandidate;
import com.etema.ragnarmmo.combat.api.ResolvedTargetCandidate;
import com.etema.ragnarmmo.combat.api.TargetRejectReason;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public final class ServerTargetResolver {
    private static final double BASIC_ATTACK_RANGE = 6.0D;

    private ServerTargetResolver() {
    }

    public static List<ResolvedTargetCandidate> resolve(ServerPlayer attacker, List<RagnarTargetCandidate> candidates) {
        return resolve(attacker, candidates, BASIC_ATTACK_RANGE);
    }

    public static List<ResolvedTargetCandidate> resolve(ServerPlayer attacker, List<RagnarTargetCandidate> candidates,
            double maxRange) {
        if (attacker == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        double safeRange = Math.max(0.0D, maxRange);
        return candidates.stream().map(candidate -> resolveOne(attacker, candidate, safeRange)).toList();
    }

    private static ResolvedTargetCandidate resolveOne(ServerPlayer attacker, RagnarTargetCandidate candidate,
            double maxRange) {
        if (candidate == null) {
            return ResolvedTargetCandidate.rejected(-1, TargetRejectReason.TARGET_NOT_FOUND, 0.0D);
        }
        Entity entity = attacker.level().getEntity(candidate.entityId());
        double distance = entity != null ? Math.sqrt(attacker.distanceToSqr(entity)) : candidate.distance();
        if (entity == null) {
            return ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_NOT_FOUND, distance);
        }
        if (entity == attacker) {
            return ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.SELF_TARGET, distance);
        }
        if (entity.isRemoved()) {
            return ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_REMOVED, distance);
        }
        if (!(entity instanceof LivingEntity living)) {
            return ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_NOT_LIVING, distance);
        }
        if (!living.isAlive()) {
            return ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_DEAD, distance);
        }
        if (RoCombatStatusService.hasHiding(living) && !RoCombatStatusService.canDetectHiding(attacker)) {
            return ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_HIDDEN, distance);
        }
        if (distance > maxRange) {
            return ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_OUT_OF_RANGE, distance);
        }
        if (living.isInvulnerable()) {
            return ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_INVULNERABLE, distance);
        }
        return ResolvedTargetCandidate.accepted(candidate.entityId(), distance);
    }
}
