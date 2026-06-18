package com.etema.ragnarmmo.combat.event;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import com.etema.ragnarmmo.combat.element.CombatPropertyResolver;
import com.etema.ragnarmmo.combat.element.ElementProperty;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.combat.formula.AccuracyFormulaService;
import com.etema.ragnarmmo.combat.formula.CombatPropertyModifierService;
import com.etema.ragnarmmo.combat.formula.DamageFormulaService;
import com.etema.ragnarmmo.combat.formula.DefenseFormulaService;
import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileProvider;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileState;
import com.etema.ragnarmmo.common.api.mobs.profile.MobProfile;
import com.etema.ragnarmmo.common.api.mobs.runtime.MobProfileBootstrap;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID)
public final class MobPreRenewalDamageEventHandler {
    private MobPreRenewalDamageEventHandler() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ServerPlayer target)) {
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

        RagnarCoreAPI.get(target).ifPresent(stats -> {
            DerivedStats derived = DerivedStatsService.compute(target, stats).orElse(null);
            if (derived == null) {
                return;
            }

            double hitRate = AccuracyFormulaService.hitRate(profile.hit(), derived.flee);
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
            boolean critical = target.getRandom().nextDouble() < Math.max(0.0D, profile.crit() / 100.0D);
            if (critical) {
                rawDamage *= 1.4D;
            }
            ElementProperty attackProperty = CombatPropertyResolver.getDefensiveElementProperty(attacker);
            ElementProperty targetProperty = CombatPropertyResolver.getDefensiveElementProperty(target);
            ElementType attackElement = attackProperty.type();
            rawDamage *= DamageFormulaService.elementMultiplier(attackElement, targetProperty.type(), targetProperty.level());

            double hardDefReduction = DefenseFormulaService.physicalDamageReduction(derived.hardDefense);
            double bonusReduction = Math.max(0.0D, derived.physicalDamageReduction - hardDefReduction)
                    + CombatPropertyModifierService.incomingElementReduction(target, attackElement);
            double finalDamage = DefenseFormulaService.applyPreRenewalMobPhysicalDefense(
                    rawDamage,
                    derived.hardDefense,
                    derived.softDefense,
                    bonusReduction);
            event.setAmount((float) finalDamage);
        });
    }
}
