package com.etema.ragnarmmo.combat.api;

import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record CombatRequestContext(
        ServerPlayer actor,
        CombatActionType actionType,
        int sequenceId,
        int comboIndex,
        boolean offHand,
        int selectedSlot,
        String skillId,
        List<CombatTargetCandidate> candidates,
        Map<String, Object> metadata) {
    public CombatRequestContext {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        metadata = metadata == null ? Collections.emptyMap() : Map.copyOf(metadata);
    }

    public CombatRequestContext(ServerPlayer actor, CombatActionType actionType, int sequenceId, int comboIndex,
            boolean offHand, int selectedSlot, String skillId, List<CombatTargetCandidate> candidates) {
        this(actor, actionType, sequenceId, comboIndex, offHand, selectedSlot, skillId, candidates, Collections.emptyMap());
    }
}
