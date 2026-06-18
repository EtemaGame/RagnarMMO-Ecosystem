package com.etema.ragnarmmo.combat.targeting;

import com.etema.ragnarmmo.combat.api.RagnarTargetCandidate;
import com.etema.ragnarmmo.combat.api.ResolvedTargetCandidate;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface RagnarTargetResolver {
    List<ResolvedTargetCandidate> resolveCandidates(ServerPlayer player, List<RagnarTargetCandidate> candidates);
}
