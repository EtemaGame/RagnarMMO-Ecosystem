package com.etema.ragnarmmo.combat.api;

public record RagnarTargetCandidate(int entityId, double distance, boolean clientLineOfSight) {
}
