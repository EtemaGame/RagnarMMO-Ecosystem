package com.etema.ragnarmmo.combat.status;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID)
public final class RoCombatStatusEvents {
    private RoCombatStatusEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        RoCombatStatusService.clearExpired(entity);
        RoCombatStatusService.tickPoison(entity);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Mob)) {
            return;
        }
        RoCombatStatusService.consumeEndureMonsterHit(event.getEntity());
    }
}
