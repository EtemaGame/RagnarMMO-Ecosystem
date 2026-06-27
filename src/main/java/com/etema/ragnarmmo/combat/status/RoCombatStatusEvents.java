package com.etema.ragnarmmo.combat.status;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import java.util.ArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

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
        bridgeVanillaEffects(entity);
        RoCombatStatusService.tickPoison(entity);
        RoCombatStatusService.tickBleeding(entity);
        RoCombatStatusService.tickHiding(entity);
        RoCombatStatusService.tickCloaking(entity);
        RoCombatStatusService.tickActionBlocked(entity);
        RoCombatStatusService.tickChaos(entity);
        if (entity instanceof Mob mob && mob.getTarget() != null
                && RoCombatStatusService.hasConcealment(mob.getTarget())
                && !RoCombatStatusService.canDetectHiding(mob)) {
            mob.setTarget(null);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        var attackerEntity = event.getSource().getEntity();
        if (attackerEntity instanceof LivingEntity attacker && RoCombatStatusService.blocksAction(attacker)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getAmount() > 0.0F && RoCombatStatusService.hasFrozen(event.getEntity())) {
            RoCombatStatusService.clearFrozen(event.getEntity());
        }
        if (event.getAmount() > 0.0F && RoCombatStatusService.hasStoneCurse(event.getEntity())) {
            RoCombatStatusService.clearStoneCurse(event.getEntity());
        }
        if (event.getAmount() > 0.0F && RoCombatStatusService.hasHiding(event.getEntity())) {
            RoCombatStatusService.revealHiding(event.getEntity());
        }
        if (event.getAmount() > 0.0F && RoCombatStatusService.hasCloaking(event.getEntity())) {
            RoCombatStatusService.revealHiding(event.getEntity());
        }
        if (event.getAmount() > 0.0F && RoCombatStatusService.hasSleep(event.getEntity())) {
            RoCombatStatusService.clearSleep(event.getEntity());
        }
        if (!(event.getSource().getEntity() instanceof Mob)) {
            return;
        }
        RoCombatStatusService.consumeEndureMonsterHit(event.getEntity());
    }

    private static void bridgeVanillaEffects(LivingEntity entity) {
        var blindness = entity.getEffect(MobEffects.BLINDNESS);
        if (blindness != null) {
            RoCombatStatusService.applyBlind(entity, Math.max(1, blindness.getDuration()));
            entity.removeEffect(MobEffects.BLINDNESS);
        }
        var confusion = entity.getEffect(MobEffects.CONFUSION);
        if (confusion != null) {
            RoCombatStatusService.applyChaos(entity, Math.max(1, confusion.getDuration()));
            entity.removeEffect(MobEffects.CONFUSION);
        }
        var poison = entity.getEffect(MobEffects.POISON);
        var wither = entity.getEffect(MobEffects.WITHER);
        var strongestPoisonBridge = poison != null ? poison : wither;
        if (strongestPoisonBridge != null) {
            if (!RoCombatStatusService.hasPoison(entity)) {
                RoCombatStatusService.applyPoison(entity, Math.max(1, strongestPoisonBridge.getDuration()));
            }
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
        }
        var invisibility = entity.getEffect(MobEffects.INVISIBILITY);
        if (invisibility != null) {
            RoCombatStatusService.applyHiding(entity, Math.max(1, invisibility.getDuration()));
        }
        var speed = entity.getEffect(MobEffects.MOVEMENT_SPEED);
        if (speed != null) {
            RoCombatStatusService.applyIncreaseAgi(entity, Math.max(1, speed.getDuration()),
                    Math.max(1, (speed.getAmplifier() + 1) * 3));
        }
        var slowness = entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (slowness != null) {
            RoCombatStatusService.applyDecreaseAgi(entity, Math.max(1, slowness.getDuration()),
                    Math.max(1, (slowness.getAmplifier() + 1) * 3));
        }
        entity.removeEffect(MobEffects.INVISIBILITY);
        entity.removeEffect(MobEffects.GLOWING);
        entity.removeEffect(MobEffects.MOVEMENT_SPEED);
        entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        entity.removeEffect(MobEffects.WEAKNESS);
        entity.removeEffect(MobEffects.DAMAGE_BOOST);
        entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        entity.removeEffect(MobEffects.DIG_SPEED);
        entity.removeEffect(MobEffects.REGENERATION);
        entity.removeEffect(MobEffects.ABSORPTION);
        entity.removeEffect(MobEffects.FIRE_RESISTANCE);
        purgeRemainingVanillaEffects(entity);
    }

    private static void purgeRemainingVanillaEffects(LivingEntity entity) {
        var effects = new ArrayList<>(entity.getActiveEffects());
        for (var effect : effects) {
            var key = ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect());
            if (key != null && "minecraft".equals(key.getNamespace())) {
                entity.removeEffect(effect.getEffect());
            }
        }
    }
}
