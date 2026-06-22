package com.etema.ragnarmmo.combat.util;

import com.etema.ragnarmmo.combat.api.BasicAttackOutcome;
import com.etema.ragnarmmo.combat.api.BasicAttackSource;
import com.etema.ragnarmmo.combat.api.CombatRequestContext;
import com.etema.ragnarmmo.combat.api.CombatResolution;
import com.etema.ragnarmmo.combat.api.CombatRejectReason;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CombatDebugLog {
    private static final Logger LOGGER = LoggerFactory.getLogger("RagnarMMO-Combat");

    private CombatDebugLog() {
    }

    public static void logAttackRequest(CombatRequestContext ctx) {
        if (ctx != null) {
            LOGGER.debug("combat request action={} seq={} actor={}", ctx.actionType(), ctx.sequenceId(),
                    ctx.actor() != null ? ctx.actor().getGameProfile().getName() : "null");
        }
    }

    public static void logBasicAttackOutcome(BasicAttackOutcome outcome) {
        if (outcome != null) {
            LOGGER.debug("basic attack outcome accepted={} reject={} failure={} resolutions={}",
                    outcome.accepted(), outcome.rejectReason(), outcome.failureReason(), outcome.resolutions().size());
        }
    }

    public static void logValidationReject(CombatRequestContext ctx, String reason) {
        LOGGER.debug("combat validation reject {} ctx={}", reason, ctx != null ? ctx.sequenceId() : -1);
    }

    public static void logInfrastructureFailure(BasicAttackSource source, CombatRejectReason reason, Throwable ex) {
        LOGGER.warn("combat infrastructure failure source={} reason={}", source, reason, ex);
    }

    public static void logBasicAttackDedupe(String phase, BasicAttackSource source, ServerPlayer player, int sequenceId,
            int targetId, long nowTick) {
        LOGGER.debug("combat dedupe phase={} source={} player={} seq={} target={} tick={}",
                phase, source, player != null ? player.getId() : -1, sequenceId, targetId, nowTick);
    }

    public static void logHitResolution(CombatResolution resolution) {
        LOGGER.debug("combat resolution target={} type={} base={} final={}",
                resolution.targetEntityId(), resolution.resultType(), resolution.rawDamage(), resolution.finalDamage());
    }
}
