package com.etema.ragnarmmo.jobs.runtime;

import com.etema.ragnarmmo.combat.api.CombatActionType;
import com.etema.ragnarmmo.combat.api.CombatRequestContext;
import com.etema.ragnarmmo.combat.api.CombatTargetCandidate;
import com.etema.ragnarmmo.combat.engine.RagnarCombatEngine;
import com.etema.ragnarmmo.combat.aggro.AggroManager;
import com.etema.ragnarmmo.combat.element.CombatPropertyResolver;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.ThiefSkillFormulaService;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import com.etema.ragnarmmo.common.api.mobs.runtime.MobProfileBootstrap;
import com.etema.ragnarmmo.items.UtilityItems;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public final class JobSkillEffectRegistry {
    private static final Map<ResourceLocation, JobSkillEffect> EFFECTS = new ConcurrentHashMap<>();

    private JobSkillEffectRegistry() {
    }

    public static void bootstrapDefaults() {
        register(id("first_aid"), context -> {
            float amount = 5.0F;
            context.player().heal(amount);
            context.player().level().playSound(null, context.player(), SoundEvents.GENERIC_DRINK,
                    SoundSource.PLAYERS, 0.7F, 1.5F);
            particles(context.player(), ParticleTypes.HEART, 6, 0.3D, 0.2D, 0.3D);
            return true;
        });
        register(id("heal"), context -> {
            var aimedTarget = context.target().orElse(null);
            var target = aimedTarget instanceof ServerPlayer
                    || (aimedTarget != null && CombatPropertyResolver.getDefensiveElement(aimedTarget) == ElementType.UNDEAD)
                    ? aimedTarget
                    : context.player();
            int amount = AcolyteSkillFormulaService.healAmount(context.player(), context.level());
            int offensiveDamage = AcolyteSkillFormulaService.offensiveHealDamage(context.player(), target, context.level());
            if (offensiveDamage > 0) {
                target.invulnerableTime = 0;
                target.hurt(context.player().damageSources().magic(), offensiveDamage);
                target.invulnerableTime = 0;
                particles(target, ParticleTypes.SMOKE, 18, 0.25D, 0.45D, 0.25D);
            } else {
                target.heal(amount);
                particles(target, ParticleTypes.HEART, 8, 0.35D, 0.35D, 0.35D);
                particles(target, ParticleTypes.HAPPY_VILLAGER, 12, 0.45D, 0.6D, 0.45D);
            }
            target.level().playSound(null, target, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.8F, 1.7F);
            return true;
        });
        register(id("increase_agi"), context -> {
            int hpCost = context.definition().getLevelInt("hp_cost", context.level(), 15);
            if (context.player().getHealth() <= hpCost + 1.0F) {
                return false;
            }
            context.player().hurt(context.player().damageSources().generic(), hpCost);
            int duration = context.definition().getLevelInt(
                    "duration_ticks",
                    context.level(),
                    20 * (40 + context.level() * 20));
            LivingEntity aimedTarget = context.target().orElse(null);
            LivingEntity target = aimedTarget instanceof ServerPlayer ? aimedTarget : context.player();
            RoCombatStatusService.applyIncreaseAgi(target, duration,
                    context.definition().getLevelInt("agi_bonus", context.level(),
                            AcolyteSkillFormulaService.agiBonus(context.level())));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0, false, true, true));
            particles(context.player(), ParticleTypes.CLOUD, 12, 0.35D, 0.2D, 0.35D);
            return true;
        });
        register(id("bash"), context -> context.target().map(target -> {
            boolean hit = applyCombatSkillDamage(context, target);
            if (hit) {
                particles(target, ParticleTypes.CRIT, 10, 0.25D, 0.35D, 0.25D);
            }
            return hit;
        }).orElse(false));
        register(id("play_dead"), context -> {
            int duration = Math.max(60, context.definition().cooldownTicks() / 2);
            context.player().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0, false, false, true));
            context.player().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 6, false, false, true));
            context.player().addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false, true));
            AABB area = context.player().getBoundingBox().inflate(12.0D);
            for (Mob mob : context.player().level().getEntitiesOfClass(Mob.class, area, mob -> mob.isAlive())) {
                if (mob.getTarget() == context.player()) {
                    mob.setTarget(null);
                }
                mob.setLastHurtByMob(null);
                mob.setLastHurtMob(null);
            }
            context.player().level().playSound(null, context.player(), SoundEvents.ARMOR_EQUIP_CHAIN,
                    SoundSource.PLAYERS, 0.8F, 0.7F);
            particles(context.player(), ParticleTypes.SMOKE, 16, 0.4D, 0.15D, 0.4D);
            return true;
        });
        register(id("provoke"), context -> context.target().map(target -> {
            if (!(target instanceof Mob mob)
                    || CombatPropertyResolver.getDefensiveElement(target) == ElementType.UNDEAD
                    || MobProfileBootstrap.isBossLike(target)) {
                return false;
            }
            double successChance = context.definition().getLevelDouble(
                    "success_chance",
                    context.level(),
                    0.53D + (context.level() - 1) * 0.03D);
            if (context.player().getRandom().nextDouble() > successChance) {
                particles(target, ParticleTypes.SMOKE, 8, 0.2D, 0.2D, 0.2D);
                return false;
            }
            mob.setTarget(context.player());
            int duration = context.definition().getLevelInt("duration_ticks", context.level(), 600);
            RoCombatStatusService.applyProvoke(
                    mob,
                    duration,
                    context.definition().getLevelDouble("def_reduction_percent", context.level(),
                            5.0D + 5.0D * context.level()),
                    context.definition().getLevelDouble("attack_bonus_percent", context.level(),
                            2.0D + 3.0D * context.level()));
            AggroManager.applyAggro(mob, context.player(), duration);
            particles(target, ParticleTypes.ANGRY_VILLAGER, 15, 0.3D, 0.2D, 0.3D);
            target.level().playSound(null, target, SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.55F, 1.35F);
            return true;
        }).orElse(false));
        register(id("magnum_break"), context -> {
            float hpCost = context.definition().getLevelInt("hp_cost", context.level(), 20);
            if (context.player().getHealth() <= hpCost + 1.0F) {
                return false;
            }
            context.player().hurt(context.player().damageSources().generic(), hpCost);
            double radius = context.definition().getLevelDouble("aoe_radius", context.level(), 2.5D);
            int burnSeconds = context.definition().getLevelInt("burn_seconds", context.level(), 3);
            int hits = 0;
            for (LivingEntity target : context.player().level().getEntitiesOfClass(
                    LivingEntity.class,
                    context.player().getBoundingBox().inflate(radius),
                    entity -> entity.isAlive() && entity != context.player())) {
                if (applyCombatSkillDamage(context, target)) {
                    knockAway(context.player(), target,
                            context.definition().getLevelDouble("knockback_strength", context.level(), 0.6D));
                    hits++;
                }
            }
            RoCombatStatusService.applyMagnumBreakFireBonus(
                    context.player(),
                    context.definition().getLevelInt("buff_duration_ticks", context.level(), 200),
                    context.definition().getLevelDouble("fire_bonus_percent", context.level(), 20.0D) / 100.0D);
            if (context.player().level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.FLAME, context.player().getX(), context.player().getY() + 0.3D,
                        context.player().getZ(), 36, radius * 0.35D, 0.15D, radius * 0.35D, 0.08D);
                level.sendParticles(ParticleTypes.EXPLOSION, context.player().getX(), context.player().getY() + 0.4D,
                        context.player().getZ(), 1, 0, 0, 0, 0);
            }
            context.player().level().playSound(null, context.player(), SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS, 0.7F, 1.6F);
            return hits > 0;
        });
        register(id("blessing"), context -> {
            int duration = context.definition().getLevelInt("duration_ticks", context.level(), 1200 + context.level() * 400);
            LivingEntity aimedTarget = context.target().orElse(null);
            LivingEntity target;
            if (aimedTarget instanceof Mob mob) {
                if (!AcolyteSkillFormulaService.isUndeadOrDemon(mob) || MobProfileBootstrap.isBossLike(mob)) {
                    return false;
                }
                target = mob;
                RoCombatStatusService.applyOffensiveBlessing(target, duration, 0.5D);
            } else {
                target = aimedTarget instanceof ServerPlayer ? aimedTarget : context.player();
                RoCombatStatusService.applyBlessing(target, duration,
                        context.definition().getLevelInt("stat_bonus", context.level(),
                                AcolyteSkillFormulaService.blessingStatBonus(context.level())));
            }
            target.removeEffect(MobEffects.WITHER);
            particles(target, ParticleTypes.ENCHANT, 24, 0.45D, 0.8D, 0.45D);
            target.level().playSound(null, target, SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.PLAYERS, 0.8F, 1.5F);
            return true;
        });
        register(id("double_strafe"), context -> context.target().map(target -> {
            if (!requireBowAndArrow(context)) {
                return false;
            }
            boolean any = applyCombatSkillDamage(context, target);
            if (any) {
                particles(target, ParticleTypes.CRIT, 12, 0.25D, 0.35D, 0.25D);
                target.level().playSound(null, target, SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.7F, 1.35F);
            }
            return any;
        }).orElse(false));
        register(id("arrow_shower"), context -> context.target().map(center -> {
            if (!requireBowAndArrow(context)) {
                return false;
            }
            double radius = context.definition().getLevelDouble("aoe_radius", context.level(), 2.5D);
            int hits = 0;
            for (LivingEntity target : center.level().getEntitiesOfClass(
                    LivingEntity.class,
                    center.getBoundingBox().inflate(radius),
                    entity -> entity.isAlive() && entity != context.player())) {
                if (applyCombatSkillDamage(context, target)) {
                    knockAway(context.player(), target,
                            context.definition().getLevelDouble("knockback_strength", context.level(), 0.25D));
                    hits++;
                }
            }
            particles(center, ParticleTypes.CRIT, 24, radius * 0.3D, 0.5D, radius * 0.3D);
            center.level().playSound(null, center, SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 0.8F, 1.2F);
            return hits > 0;
        }).orElse(false));
        registerBolt("fire_bolt", ParticleTypes.FLAME, true);
        registerBolt("cold_bolt", ParticleTypes.SNOWFLAKE, false);
        registerBolt("lightning_bolt", ParticleTypes.ELECTRIC_SPARK, false);
        register(id("envenom"), context -> context.target().map(target -> {
            boolean hit = applyCombatSkillDamage(context, target);
            boolean poisoned = false;
            double chance = context.definition().getLevelDouble("status_chance", context.level(),
                    ThiefSkillFormulaService.envenomPoisonChance(context.level()));
            if (context.player().getRandom().nextDouble() < chance
                    && CombatPropertyResolver.getDefensiveElement(target) != ElementType.UNDEAD
                    && !MobProfileBootstrap.isBossLike(target)) {
                RoCombatStatusService.applyPoison(target,
                        context.definition().getLevelInt("poison_duration_ticks", context.level(), 1200));
                poisoned = true;
            }
            if (hit || poisoned) {
                particles(target, ParticleTypes.ITEM_SLIME, 12, 0.25D, 0.35D, 0.25D);
            }
            return hit || poisoned;
        }).orElse(false));
        register(id("hiding"), context -> {
            int duration = context.definition().getLevelInt("duration_ticks", context.level(), 600);
            int slowness = context.definition().getLevelInt("slowness_amplifier", context.level(), 2);
            context.player().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0, false, false, true));
            if (slowness >= 0) {
                context.player().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slowness, false, false, true));
            }
            AABB area = context.player().getBoundingBox().inflate(10.0D);
            for (Mob mob : context.player().level().getEntitiesOfClass(Mob.class, area, mob -> mob.getTarget() == context.player())) {
                mob.setTarget(null);
            }
            particles(context.player(), ParticleTypes.SMOKE, 18, 0.35D, 0.2D, 0.35D);
            context.player().level().playSound(null, context.player(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.7F, 0.8F);
            return true;
        });
        register(id("detoxify"), context -> {
            LivingEntity target = context.target().orElse(context.player());
            RoCombatStatusService.clearPoison(target);
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WITHER);
            particles(target, ParticleTypes.HAPPY_VILLAGER, 10, 0.35D, 0.4D, 0.35D);
            return true;
        });
        register(id("cure"), context -> {
            LivingEntity target = context.target().orElse(context.player());
            clearNegativeEffects(target);
            particles(target, ParticleTypes.HAPPY_VILLAGER, 14, 0.35D, 0.45D, 0.35D);
            target.level().playSound(null, target, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5F, 1.8F);
            return true;
        });
        register(id("decrease_agi"), context -> context.target().map(target -> {
            if (MobProfileBootstrap.isBossLike(target)) {
                return false;
            }
            double chance = AcolyteSkillFormulaService.decreaseAgiSuccessChance(context.player(), target, context.level());
            if (context.player().getRandom().nextDouble() > chance) {
                particles(target, ParticleTypes.SMOKE, 8, 0.25D, 0.25D, 0.25D);
                return false;
            }
            int duration = context.definition().getLevelInt("duration_ticks", context.level(), 600 + context.level() * 200);
            int reduction = context.definition().getLevelInt("agi_reduction", context.level(),
                    AcolyteSkillFormulaService.agiBonus(context.level()));
            RoCombatStatusService.applyDecreaseAgi(target, duration, reduction);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0, false, true, true));
            particles(target, ParticleTypes.ASH, 16, 0.3D, 0.5D, 0.3D);
            return true;
        }).orElse(false));
        register(id("fire_ball"), context -> context.target().map(center -> {
            double radius = context.definition().getLevelDouble("splash_radius", context.level(), 2.5D);
            int burn = context.definition().getLevelInt("burn_seconds", context.level(), 3);
            int hits = 0;
            for (LivingEntity target : center.level().getEntitiesOfClass(
                    LivingEntity.class,
                    center.getBoundingBox().inflate(radius),
                    entity -> entity.isAlive() && entity != context.player())) {
                if (applyCombatSkillDamage(context, target)) {
                    target.setSecondsOnFire(burn);
                    hits++;
                }
            }
            particles(center, ParticleTypes.FLAME, 30, radius * 0.25D, 0.5D, radius * 0.25D);
            center.level().playSound(null, center, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
            return hits > 0;
        }).orElse(false));
        register(id("angelus"), context -> {
            int duration = context.definition().getLevelInt("duration_ticks", context.level(), 600 * context.level());
            RoCombatStatusService.applyAngelus(context.player(), duration,
                    context.definition().getLevelDouble("soft_def_multiplier", context.level(),
                            AcolyteSkillFormulaService.angelusSoftDefenseMultiplier(context.level())));
            particles(context.player(), ParticleTypes.ENCHANT, 18, 0.4D, 0.5D, 0.4D);
            return true;
        });
        register(id("aqua_benedicta"), context -> {
            if (!context.player().level().getFluidState(context.player().blockPosition()).is(FluidTags.WATER)) {
                context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Aqua Benedicta requires water."));
                return false;
            }
            if (context.player().getInventory().countItem(Items.GLASS_BOTTLE) <= 0) {
                context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Aqua Benedicta requires an Empty Bottle."));
                return false;
            }
            context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Aqua Benedicta ready; Holy Water item is not registered yet."));
            context.player().level().playSound(null, context.player(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.8F, 1.3F);
            particles(context.player(), ParticleTypes.SPLASH, 18, 0.35D, 0.25D, 0.35D);
            return true;
        });
        register(id("back_slide"), context -> {
            Vec3 back = context.player().getLookAngle().normalize().scale(-3.0D);
            context.player().teleportTo(context.player().getX() + back.x, context.player().getY(), context.player().getZ() + back.z);
            particles(context.player(), ParticleTypes.CLOUD, 12, 0.25D, 0.15D, 0.25D);
            return true;
        });
        register(id("endure"), context -> {
            int duration = context.definition().getLevelInt("duration_ticks", context.level(), 120 + context.level() * 20);
            RoCombatStatusService.applyEndure(
                    context.player(),
                    duration,
                    context.definition().getLevelInt("mdef_bonus", context.level(), context.level()),
                    context.definition().getLevelInt("max_hits", context.level(), 7));
            particles(context.player(), ParticleTypes.CRIT, 14, 0.3D, 0.5D, 0.3D);
            return true;
        });
        register(id("fire_wall"), context -> context.target().map(center -> {
            double radius = 2.0D;
            int duration = context.definition().getLevelInt("duration_ticks", context.level(), 80 + context.level() * 20);
            int hits = 0;
            for (LivingEntity target : center.level().getEntitiesOfClass(LivingEntity.class,
                    center.getBoundingBox().inflate(radius), entity -> entity.isAlive() && entity != context.player())) {
                if (applyCombatSkillDamage(context, target)) {
                    target.setSecondsOnFire(Math.max(2, duration / 40));
                    hits++;
                }
            }
            particles(center, ParticleTypes.FLAME, 36, radius * 0.35D, 0.25D, radius * 0.35D);
            return hits > 0;
        }).orElse(false));
        register(id("frost_diver"), context -> context.target().map(target -> {
            boolean hit = applyCombatSkillDamage(context, target);
            if (hit) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                        context.definition().getLevelInt("duration_ticks", context.level(), 80 + context.level() * 20),
                        6, false, true, true));
                particles(target, ParticleTypes.SNOWFLAKE, 20, 0.3D, 0.55D, 0.3D);
            }
            return hit;
        }).orElse(false));
        register(id("improve_concentration"), context -> {
            int duration = context.definition().getLevelInt("duration_ticks", context.level(), 1200 + context.level() * 200);
            RoCombatStatusService.applyImproveConcentration(context.player(), duration, context.level());
            for (LivingEntity target : context.player().level().getEntitiesOfClass(LivingEntity.class,
                    context.player().getBoundingBox().inflate(
                            context.definition().getLevelDouble("reveal_radius", context.level(), 3.0D)),
                    entity -> entity.isAlive() && entity != context.player())) {
                target.removeEffect(MobEffects.INVISIBILITY);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, true, true));
            }
            particles(context.player(), ParticleTypes.CRIT, 16, 0.35D, 0.5D, 0.35D);
            return true;
        });
        register(id("napalm_beat"), context -> context.target().map(center -> magicSplash(context, center, 1.8D, 70.0D + context.level() * 8.0D, ParticleTypes.WITCH)).orElse(false));
        register(id("pneuma"), context -> {
            context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Pneuma needs ground-effect combat cells."));
            return false;
        });
        register(id("ruwach"), context -> {
            int revealed = 0;
            for (LivingEntity target : context.player().level().getEntitiesOfClass(LivingEntity.class,
                    context.player().getBoundingBox().inflate(
                            context.definition().getLevelDouble("reveal_radius", context.level(), 2.0D)),
                    entity -> entity.isAlive() && entity != context.player())) {
                boolean hidden = target.hasEffect(MobEffects.INVISIBILITY);
                target.removeEffect(MobEffects.INVISIBILITY);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, true, true));
                if (hidden) {
                    applyCombatSkillDamage(context, target);
                    revealed++;
                }
            }
            particles(context.player(), ParticleTypes.END_ROD, 28, 1.0D, 0.5D, 1.0D);
            return true;
        });
        register(id("safety_wall"), context -> selfBuff(context, MobEffects.DAMAGE_RESISTANCE, "duration_ticks", 160, 2, ParticleTypes.ENCHANTED_HIT));
        register(id("sight"), context -> {
            int revealed = 0;
            for (LivingEntity target : context.player().level().getEntitiesOfClass(LivingEntity.class,
                    context.player().getBoundingBox().inflate(
                            context.definition().getLevelDouble("aoe_radius", context.level(), 15.0D)),
                    entity -> entity.isAlive() && entity != context.player())) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0, false, true, true));
                revealed++;
            }
            particles(context.player(), ParticleTypes.FLAME, 24, 0.8D, 0.3D, 0.8D);
            return revealed >= 0;
        });
        register(id("signum_crucis"), context -> {
            int affected = 0;
            for (LivingEntity target : context.player().level().getEntitiesOfClass(LivingEntity.class,
                    context.player().getBoundingBox().inflate(8.0D), entity -> entity.isAlive() && entity != context.player())) {
                if (target instanceof ServerPlayer || !AcolyteSkillFormulaService.isUndeadOrDemon(target)) {
                    continue;
                }
                double chance = (23.0D + 4.0D * context.level()
                        + AcolyteSkillFormulaService.baseLevel(context.player())
                        - targetBaseLevel(target)) / 100.0D;
                if (context.player().getRandom().nextDouble() <= Math.max(0.0D, Math.min(0.95D, chance))) {
                    RoCombatStatusService.applySignumCrucis(target,
                            context.definition().getLevelInt("duration_ticks", context.level(), 300),
                            AcolyteSkillFormulaService.signumHardDefenseMultiplier(context.level()));
                    affected++;
                }
            }
            particles(context.player(), ParticleTypes.END_ROD, 32, 1.0D, 0.6D, 1.0D);
            return affected > 0;
        });
        register(id("soul_strike"), context -> context.target().map(target -> {
            boolean any = applyCombatSkillDamage(context, target);
            int hits = Math.max(1, context.definition().getLevelInt("hit_count", context.level(), Math.max(1, context.level() / 2)));
            particles(target, ParticleTypes.SOUL_FIRE_FLAME, 12 + hits * 2, 0.3D, 0.5D, 0.3D);
            return any;
        }).orElse(false));
        register(id("steal"), context -> context.target().map(target -> {
            particles(target, ParticleTypes.POOF, 8, 0.2D, 0.25D, 0.2D);
            context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Steal attempt complete."));
            return true;
        }).orElse(false));
        register(id("stone_curse"), context -> context.target().map(target -> {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    context.definition().getLevelInt("duration_ticks", context.level(), 100 + context.level() * 20),
                    8, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100 + context.level() * 20, 1, false, true, true));
            particles(target, ParticleTypes.ASH, 18, 0.25D, 0.45D, 0.25D);
            return true;
        }).orElse(false));
        register(id("teleportation"), context -> {
            if (context.level() >= 2) {
                context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Teleport Lv2 needs Save Point support."));
                return false;
            }
            double distance = 12.0D + context.level() * 4.0D;
            Vec3 offset = new Vec3(
                    (context.player().getRandom().nextDouble() - 0.5D) * distance,
                    0,
                    (context.player().getRandom().nextDouble() - 0.5D) * distance);
            context.player().teleportTo(context.player().getX() + offset.x, context.player().getY(), context.player().getZ() + offset.z);
            particles(context.player(), ParticleTypes.PORTAL, 32, 0.4D, 0.8D, 0.4D);
            return true;
        });
        register(id("thunder_storm"), context -> context.target().map(center -> magicSplash(context, center, 3.0D, 80.0D + context.level() * 10.0D, ParticleTypes.ELECTRIC_SPARK)).orElse(false));
        register(id("warp_portal"), context -> {
            if (UtilityItems.countItem(context.player(), UtilityItems.BLUE_GEMSTONE.get()) <= 0) {
                context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Warp Portal requires Blue Gemstone."));
                return false;
            }
            context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Warp Portal needs memo/save destination support."));
            return false;
        });
        register(id("identify"), context -> {
            context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Identify inspected the held item."));
            particles(context.player(), ParticleTypes.ENCHANT, 10, 0.25D, 0.35D, 0.25D);
            return true;
        });
        register(id("pushcart"), context -> selfBuff(context, MobEffects.MOVEMENT_SLOWDOWN, "duration_ticks", 1200, 0, ParticleTypes.HAPPY_VILLAGER));
        register(id("mammonite"), context -> context.target().map(target -> {
            boolean hit = applyCombatSkillDamage(context, target);
            if (hit) {
                particles(target, ParticleTypes.CRIT, 18, 0.25D, 0.35D, 0.25D);
            }
            return hit;
        }).orElse(false));
        register(id("buying_store"), context -> merchantPlaceholder(context, "Buying Store requires custom merchant menus."));
        register(id("vending"), context -> merchantPlaceholder(context, "Vending requires custom merchant menus."));
    }

    public static void register(ResourceLocation skillId, JobSkillEffect effect) {
        if (skillId != null && effect != null) {
            EFFECTS.put(skillId, effect);
        }
    }

    public static Optional<JobSkillEffect> get(ResourceLocation skillId) {
        return Optional.ofNullable(EFFECTS.get(skillId));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("ragnarmmo", path);
    }

    private static void registerBolt(String path, net.minecraft.core.particles.ParticleOptions particle, boolean ignite) {
        register(id(path), context -> context.target().map(target -> {
            int hits = context.definition().getLevelInt("hit_count", context.level(), context.level());
            boolean any = applyCombatSkillDamage(context, target);
            if (ignite) {
                target.setSecondsOnFire(Math.max(1, context.level() / 2));
            }
            particles(target, particle, 10 + hits * 2, 0.25D, 0.55D, 0.25D);
            target.level().playSound(null, target, ignite ? SoundEvents.BLAZE_SHOOT : SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.PLAYERS, 0.75F, ignite ? 1.5F : 1.1F);
            return any;
        }).orElse(false));
    }

    private static void clearNegativeEffects(LivingEntity target) {
        target.removeEffect(MobEffects.POISON);
        target.removeEffect(MobEffects.WITHER);
        target.removeEffect(MobEffects.BLINDNESS);
        target.removeEffect(MobEffects.CONFUSION);
    }

    private static void particles(LivingEntity entity, net.minecraft.core.particles.ParticleOptions particle,
            int count, double dx, double dy, double dz) {
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(particle, entity.getX(), entity.getY() + entity.getBbHeight() * 0.6D, entity.getZ(),
                    count, dx, dy, dz, 0.04D);
        }
    }

    private static void knockAway(LivingEntity source, LivingEntity target, double strength) {
        Vec3 delta = target.position().subtract(source.position());
        if (delta.lengthSqr() < 1.0E-4D) {
            delta = source.getLookAngle();
        }
        Vec3 push = delta.normalize().scale(strength);
        target.push(push.x, 0.18D, push.z);
        target.hurtMarked = true;
    }

    private static boolean selfBuff(JobSkillContext context, net.minecraft.world.effect.MobEffect effect,
            String durationKey, int fallbackDuration, int amplifier,
            net.minecraft.core.particles.ParticleOptions particle) {
        int duration = context.definition().getLevelInt(durationKey, context.level(), fallbackDuration);
        context.player().addEffect(new MobEffectInstance(effect, duration, Math.max(0, amplifier), false, true, true));
        particles(context.player(), particle, 18, 0.4D, 0.5D, 0.4D);
        return true;
    }

    private static boolean magicSplash(JobSkillContext context, LivingEntity center, double radius, double percent,
            net.minecraft.core.particles.ParticleOptions particle) {
        int hits = 0;
        for (LivingEntity target : center.level().getEntitiesOfClass(LivingEntity.class,
                center.getBoundingBox().inflate(radius),
                entity -> entity.isAlive() && entity != context.player())) {
            if (applyCombatSkillDamage(context, target)) {
                hits++;
            }
        }
        particles(center, particle, 24, radius * 0.25D, 0.55D, radius * 0.25D);
        return hits > 0;
    }

    private static boolean applyCombatSkillDamage(JobSkillContext context, LivingEntity target) {
        if (context == null || target == null || !target.isAlive()) {
            return false;
        }
        var resolutions = RagnarCombatEngine.get().handleSkillUseRequest(new CombatRequestContext(
                context.player(),
                CombatActionType.SKILL,
                0,
                0,
                false,
                context.player().getInventory().selected,
                context.skillId().toString(),
                List.of(new CombatTargetCandidate(target.getId(), "job_hotbar", 0.0D,
                        context.player().hasLineOfSight(target))),
                Map.of("level", context.level())));
        return resolutions.stream()
                .anyMatch(resolution -> resolution.targetEntityId() == target.getId()
                        && resolution.dealsDamage()
                        && resolution.finalDamage() > 0.0D);
    }

    private static boolean merchantPlaceholder(JobSkillContext context, String message) {
        context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
        particles(context.player(), ParticleTypes.HAPPY_VILLAGER, 8, 0.25D, 0.3D, 0.25D);
        return true;
    }

    private static boolean requireBowAndArrow(JobSkillContext context) {
        if (!(context.player().getMainHandItem().getItem() instanceof BowItem)) {
            context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Requires Bow."));
            return false;
        }
        if (context.player().isCreative()) {
            return true;
        }
        if (context.player().getInventory().countItem(Items.ARROW) <= 0) {
            context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal("Requires Arrow."));
            return false;
        }
        context.player().getInventory().clearOrCountMatchingItems(stack -> stack.is(Items.ARROW), 1,
                context.player().inventoryMenu.getCraftSlots());
        context.player().inventoryMenu.broadcastChanges();
        return true;
    }

    private static int targetBaseLevel(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return AcolyteSkillFormulaService.baseLevel(player);
        }
        return MobProfileBootstrap.ensureInitialized(target)
                .map(profile -> Math.max(1, profile.level()))
                .orElse(1);
    }
}
