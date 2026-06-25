package com.etema.ragnarmmo.combat.event;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import com.etema.ragnarmmo.combat.element.CombatPropertyResolver;
import com.etema.ragnarmmo.combat.element.ElementProperty;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.combat.formula.AccuracyFormulaService;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.CombatPropertyModifierService;
import com.etema.ragnarmmo.combat.formula.DamageFormulaService;
import com.etema.ragnarmmo.combat.formula.DefenseFormulaService;
import com.etema.ragnarmmo.combat.formula.FleeMobbingPenaltyService;
import com.etema.ragnarmmo.combat.ground.GroundCellService;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileProvider;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileState;
import com.etema.ragnarmmo.common.api.mobs.profile.MobProfile;
import com.etema.ragnarmmo.common.api.mobs.runtime.MobProfileBootstrap;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID)
public final class MobPreRenewalDamageEventHandler {
    private MobPreRenewalDamageEventHandler() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ServerPlayer target)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker instanceof ServerPlayer) {
            return;
        }

        MobProfile profile = MobProfileBootstrap.ensureInitialized(attacker)
                .or(() -> MobProfileProvider.get(attacker).resolve()
                        .filter(MobProfileState::isInitialized)
                        .map(MobProfileState::profile))
                .orElse(null);
        if (profile == null) {
            return;
        }

        boolean rangedPhysical = isRangedPhysicalMobAttack(profile, event.getSource().getDirectEntity());
        if (rangedPhysical ? GroundCellService.blocksPhysicalRanged(target) : GroundCellService.consumePhysicalMeleeBlock(target)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }

        RagnarCoreAPI.get(target).ifPresent(stats -> {
            DerivedStats derived = DerivedStatsService.compute(target, stats).orElse(null);
            if (derived == null) {
                return;
            }

            double hitRate = AccuracyFormulaService.hitRate(
                    profile.hit()
                            * RoCombatStatusService.offensiveBlessingStatMultiplier(attacker)
                            * RoCombatStatusService.hitMultiplier(attacker),
                    FleeMobbingPenaltyService.applyMonsterMobbingPenalty(target, derived.flee));
            double perfectDodge = Math.max(0.0D, Math.min(0.95D, derived.perfectDodge));
            if (target.getRandom().nextDouble() < perfectDodge || target.getRandom().nextDouble() > hitRate) {
                event.setCanceled(true);
                return;
            }

            int atkMin = Math.max(0, profile.atkMin());
            int atkMax = Math.max(atkMin, profile.atkMax());
            double rawDamage = atkMin == atkMax
                    ? atkMin
                    : atkMin + target.getRandom().nextInt(atkMax - atkMin + 1);
            ElementProperty targetProperty = CombatPropertyResolver.getDefensiveElementProperty(target);
            ElementType attackElement = ElementType.NEUTRAL;
            double hardDefReduction = DefenseFormulaService.physicalDamageReduction(derived.hardDefense);
            double bonusReduction = Math.max(0.0D, derived.physicalDamageReduction - hardDefReduction)
                    + CombatPropertyModifierService.incomingDamageReduction(
                            target,
                            profile.race(),
                            attackElement,
                            CombatPropertyResolver.getEntitySize(attacker));
            double afterDefense = DefenseFormulaService.applyPhysicalDefense(
                    rawDamage,
                    derived.softDefense,
                    derived.hardDefense,
                    false);
            double afterElement = afterDefense
                    * DamageFormulaService.elementMultiplier(attackElement, targetProperty.type(), targetProperty.level());
            double reduced = afterElement * (1.0D - Math.max(0.0D, Math.min(0.95D, bonusReduction)));
            reduced -= AcolyteSkillFormulaService.divineProtectionReduction(target, attacker);
            double finalDamage = reduced > 0.0D ? Math.max(1.0D, reduced) : 0.0D;
            event.setAmount((float) finalDamage);
        });
    }

    private static boolean isRangedPhysicalMobAttack(MobProfile profile, Entity directEntity) {
        if (directEntity instanceof Projectile) {
            return true;
        }
        return profile.attackRange() > 3;
    }
}
