package com.etema.ragnarmmo.combat.targeting;

import com.etema.ragnarmmo.combat.api.RagnarTargetCandidate;
import com.etema.ragnarmmo.combat.api.ResolvedTargetCandidate;
import com.etema.ragnarmmo.combat.api.TargetRejectReason;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public final class ServerAuthoritativeTargetResolver implements RagnarTargetResolver {
    private static final double MAX_REACH_SQ = 64.0D;

    @Override
    public List<ResolvedTargetCandidate> resolveCandidates(ServerPlayer player, List<RagnarTargetCandidate> candidates) {
        List<ResolvedTargetCandidate> resolved = new ArrayList<>();
        if (player == null || candidates == null) {
            return resolved;
        }
        for (RagnarTargetCandidate candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            Entity entity = player.serverLevel().getEntity(candidate.entityId());
            if (entity == null) {
                resolved.add(ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_NOT_FOUND, candidate.distance()));
                continue;
            }
            if (!(entity instanceof LivingEntity target)) {
                resolved.add(ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_NOT_LIVING, candidate.distance()));
                continue;
            }
            if (!target.isAlive()) {
                resolved.add(ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_DEAD, candidate.distance()));
                continue;
            }
            if (player.distanceToSqr(target) > MAX_REACH_SQ) {
                resolved.add(ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.TARGET_OUT_OF_RANGE, candidate.distance()));
                continue;
            }
            if (target.getUUID().equals(player.getUUID())) {
                resolved.add(ResolvedTargetCandidate.rejected(candidate.entityId(), TargetRejectReason.SELF_TARGET, candidate.distance()));
                continue;
            }
            resolved.add(ResolvedTargetCandidate.accepted(target.getId(), candidate.distance()));
        }
        return resolved;
    }
}
