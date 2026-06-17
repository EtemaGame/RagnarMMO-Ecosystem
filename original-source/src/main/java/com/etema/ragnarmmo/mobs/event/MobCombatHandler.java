package com.etema.ragnarmmo.mobs.event;

import com.etema.ragnarmmo.RagnarMMO;
import com.etema.ragnarmmo.entity.mob.AbstractRagnarMobEntity;
import com.etema.ragnarmmo.entity.mob.RagnarRetaliateMemory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMO.MODID)
public final class MobCombatHandler {

    private MobCombatHandler() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof AbstractRagnarMobEntity mob) || event.getEntity().level().isClientSide) {
            return;
        }

        LivingEntity attacker = resolveAttacker(event);
        if (attacker != null) {
            RagnarRetaliateMemory.mark(mob, attacker);
        }
    }

    private static LivingEntity resolveAttacker(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity living) {
            return living;
        }

        Entity direct = event.getSource().getDirectEntity();
        if (direct instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                return livingOwner;
            }
        }

        return null;
    }
}
