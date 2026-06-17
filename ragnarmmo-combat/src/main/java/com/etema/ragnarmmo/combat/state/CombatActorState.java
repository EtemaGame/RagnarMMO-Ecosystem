package com.etema.ragnarmmo.combat.state;

public final class CombatActorState {
    private int lastAcceptedSequence = -1;
    private long nextBasicAttackTick;

    public boolean isStale(int sequenceId) {
        return sequenceId >= 0 && sequenceId <= lastAcceptedSequence;
    }

    public void acceptSequence(int sequenceId) {
        if (sequenceId >= 0) {
            lastAcceptedSequence = Math.max(lastAcceptedSequence, sequenceId);
        }
    }

    public boolean basicAttackReady(long now) {
        return now >= nextBasicAttackTick;
    }

    public void setBasicAttackCooldown(long now, int ticks) {
        nextBasicAttackTick = now + Math.max(1, ticks);
    }
}
