package com.etema.ragnarmmo.combat.event;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import com.etema.ragnarmmo.combat.api.BasicAttackSource;
import com.etema.ragnarmmo.combat.api.RagnarAttackRequest;
import com.etema.ragnarmmo.combat.engine.RagnarCombatEngine;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID)
public final class BasicAttackEventHandler {
    private BasicAttackEventHandler() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (RoCombatStatusService.blocksAction(player)) {
            event.setCanceled(true);
            return;
        }
        var outcome = RagnarCombatEngine.get().processBasicAttackRequest(player,
                RagnarAttackRequest.singleTarget(-1, event.getTarget().getId()),
                BasicAttackSource.SERVER_ATTACK_EVENT);
        if (outcome.shouldCancelVanilla()) {
            event.setCanceled(true);
        }
    }
}
