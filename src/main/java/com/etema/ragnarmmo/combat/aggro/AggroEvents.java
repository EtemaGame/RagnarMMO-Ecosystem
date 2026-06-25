package com.etema.ragnarmmo.combat.aggro;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID)
public final class AggroEvents {
    private AggroEvents() {
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long until = mob.getPersistentData().getLong(RoCombatStatusService.PROVOKE_UNTIL_TAG);
        if (until > 0 && mob.level().getGameTime() >= until) {
            RoCombatStatusService.clearProvoke(mob);
        }

        UUID targetUUID = AggroManager.getAggroTarget(mob);
        if (targetUUID == null) {
            return;
        }

        ServerPlayer aggroTarget = serverLevel.getServer().getPlayerList().getPlayer(targetUUID);
        if (aggroTarget == null || !aggroTarget.isAlive()
                || (RoCombatStatusService.hasHiding(aggroTarget) && !RoCombatStatusService.canDetectHiding(mob))) {
            AggroManager.clearAggro(mob);
            RoCombatStatusService.clearProvoke(mob);
            mob.setTarget(null);
            return;
        }

        LivingEntity currentTarget = mob.getTarget();
        if (currentTarget == null || !currentTarget.getUUID().equals(targetUUID)) {
            mob.setTarget(aggroTarget);
        }
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            AggroManager.clearAggro(mob);
            RoCombatStatusService.clearProvoke(mob);
        }
    }
}
