package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import com.etema.ragnarmmo.combat.api.CombatResolution;
import com.etema.ragnarmmo.combat.engine.RagnarDamageCalculator;
import com.etema.ragnarmmo.combat.engine.RagnarHitCalculator;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.CombatPropertyModifierService;
import com.etema.ragnarmmo.combat.formula.SwordmanSkillFormulaService;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

public final class CombatContract {
    private final RagnarHitCalculator hitCalculator;
    private final RagnarDamageCalculator damageCalculator;

    public CombatContract(RagnarHitCalculator hitCalculator, RagnarDamageCalculator damageCalculator) {
        this.hitCalculator = hitCalculator;
        this.damageCalculator = damageCalculator;
    }

    public CombatContractResult resolveBasicAttack(
            CombatantProfile attacker,
            CombatantProfile defender,
            ActionIntent.BasicAttackIntent intent,
            Random rng) {
        if (attacker == null || defender == null) {
            return new CombatContractResult(null, false, "missing_combatant_profile");
        }
        if (intent == null) {
            return new CombatContractResult(null, false, "missing_action_intent");
        }

        var randomSource = net.minecraft.util.RandomSource.create(rng.nextLong());
        if (hitCalculator.rollPerfectDodge(defender.defense().perfectDodge(), randomSource)) {
            return new CombatContractResult(
                    new CombatResolution(defender.entity().getId(), CombatHitResultType.DODGE, 0.0D, 0.0D, false, 0.0D),
                    attacker.fallback() || defender.fallback(),
                    null);
        }

        CombatProcService.DoubleAttackProc doubleAttack = CombatProcService.rollDoubleAttack(attacker, rng);
        CombatHitResultType hit = hitCalculator.rollHitWithCrit(
                attacker.physicalAttack().hit() + doubleAttack.hitBonus(),
                defender.defense().flee(),
                attacker.physicalAttack().critChance(),
                defender.defense().critShield(),
                randomSource);

        if (hit == CombatHitResultType.MISS) {
            return new CombatContractResult(
                    new CombatResolution(defender.entity().getId(), CombatHitResultType.MISS, 0.0D, 0.0D, false, 0.0D),
                    attacker.fallback() || defender.fallback(),
                    null);
        }

        double baseAttack = attacker.physicalAttack().averageAttack()
                * RoCombatStatusService.physicalAttackMultiplier(attacker.entity());
        double damage = damageCalculator.computePhysicalDamage(
                baseAttack,
                attacker.stats().dex(),
                attacker.stats().luk(),
                rng);

        boolean critical = hit == CombatHitResultType.CRIT;
        if (critical) {
            damage *= Math.max(1.0D, attacker.physicalAttack().critDamageMultiplier());
        }

        if (!critical) {
            damage = damageCalculator.applyPhysicalDefense(
                    damage,
                    defender.defense().vit(),
                    defender.defense().agi(),
                    defender.defense().level(),
                    adjustedHardDef(defender));
        }
        if (attacker.entity() instanceof ServerPlayer player) {
            damage += SwordmanSkillFormulaService.weaponMasteryBonus(player, attacker.physicalAttack().weapon());
            damage += AcolyteSkillFormulaService.demonBaneBonus(player, defender.entity());
        }
        damage = damageCalculator.applyElement(damage, defender.modifiers(), attacker.modifiers().element());
        if (attacker.entity() instanceof Player player) {
            damage *= CombatPropertyModifierService.outgoingDamageMultiplier(player,
                    defender.modifiers().race(),
                    defender.modifiers().element(),
                    defender.modifiers().size());
        }
        damage = PassiveCombatModifierService.applyOutgoingPhysicalDamage(attacker, defender, damage);
        damage = PassiveCombatModifierService.applyIncomingPhysicalDamage(attacker, defender, damage);
        damage *= doubleAttack.hitCount();

        double finalDamage = positiveDamageOrZero(damage);
        double hitRate = hitCalculator.hitRate(attacker.physicalAttack().hit(), defender.defense().flee());
        return new CombatContractResult(
                new CombatResolution(defender.entity().getId(),
                        critical ? CombatHitResultType.CRIT : CombatHitResultType.HIT,
                        damage,
                        finalDamage,
                        critical,
                        hitRate),
                attacker.fallback() || defender.fallback(),
                null);
    }

    public CombatContractResult resolveSkill(
            CombatantProfile attacker,
            CombatantProfile defender,
            ActionIntent.SkillIntent intent,
            SkillCombatSpec spec,
            Random rng) {
        if (attacker == null || defender == null) {
            return new CombatContractResult(null, false, "missing_combatant_profile");
        }
        if (intent == null) {
            return new CombatContractResult(null, false, "missing_action_intent");
        }
        if (spec == null) {
            return new CombatContractResult(null, false, "missing_skill_combat_spec");
        }

        if (spec.hitPolicy() == SkillHitPolicy.BASIC_ATTACK) {
            var randomSource = net.minecraft.util.RandomSource.create(rng.nextLong());
            CombatHitResultType hit = hitCalculator.rollHitWithCrit(
                    attacker.physicalAttack().hit() + spec.accuracyBonus(),
                    defender.defense().flee(),
                    attacker.physicalAttack().critChance(),
                    defender.defense().critShield(),
                    randomSource);
            if (hit == CombatHitResultType.MISS) {
                return new CombatContractResult(
                        new CombatResolution(defender.entity().getId(), CombatHitResultType.MISS, 0.0D, 0.0D, false, 0.0D),
                        attacker.fallback() || defender.fallback(),
                        null);
            }
        }

        double baseDamage = switch (spec.damageType()) {
            case MAGICAL, TRUE -> attacker.magicAttack().averageMagicAttack();
            case PHYSICAL -> attacker.physicalAttack().averageAttack();
        };
        double damage = baseDamage * (spec.damagePercent() / 100.0D) * spec.hitCount();
        if (spec.damageType() == RagnarDamageType.PHYSICAL) {
            double defenseBypassDamage = 0.0D;
            if (spec.defenseBypassPercent() > 0.0D && spec.damagePercent() > 0.0D) {
                defenseBypassDamage = baseDamage * (spec.defenseBypassPercent() / 100.0D) * spec.hitCount();
                damage = baseDamage * ((spec.damagePercent() - spec.defenseBypassPercent()) / 100.0D) * spec.hitCount();
            }
            damage = damageCalculator.computePhysicalDamage(damage, attacker.stats().dex(), attacker.stats().luk(), rng);
            damage *= RoCombatStatusService.physicalAttackMultiplier(attacker.entity());
            damage = damageCalculator.applyPhysicalDefense(damage, defender.defense().vit(), defender.defense().agi(),
                    defender.defense().level(), adjustedHardDef(defender));
            damage += defenseBypassDamage;
            damage += spec.flatDamageBonus();
            if (attacker.entity() instanceof ServerPlayer player) {
                damage += SwordmanSkillFormulaService.weaponMasteryBonus(player, attacker.physicalAttack().weapon());
                damage += AcolyteSkillFormulaService.demonBaneBonus(player, defender.entity());
            }
            damage = damageCalculator.applyElement(damage, defender.modifiers(), spec.element());
            if (attacker.entity() instanceof Player player) {
                damage *= CombatPropertyModifierService.outgoingDamageMultiplier(player,
                        defender.modifiers().race(),
                        defender.modifiers().element(),
                        defender.modifiers().size());
            }
        } else if (spec.damageType() == RagnarDamageType.MAGICAL) {
            damage = damageCalculator.applyModifiers(
                    damage,
                    attacker.physicalAttack().weapon(),
                    defender.modifiers(),
                    spec.element(),
                    true);
            damage = damageCalculator.applyMagicDefense(damage, defender.defense().intel(), defender.defense().vit(),
                    defender.defense().agi(), defender.defense().level(), defender.defense().hardMdef());
        }
        double finalDamage = positiveDamageOrZero(damage);
        return new CombatContractResult(
                new CombatResolution(defender.entity().getId(),
                        damage > 0.0D ? CombatHitResultType.HIT : CombatHitResultType.MISS,
                        damage,
                        finalDamage,
                        false,
                        1.0D),
                attacker.fallback() || defender.fallback(),
                null);
    }

    private static double positiveDamageOrZero(double damage) {
        return damage > 0.0D ? Math.max(1.0D, damage) : 0.0D;
    }

    private static double adjustedHardDef(CombatantProfile defender) {
        if (defender == null || defender.entity() instanceof ServerPlayer) {
            return defender == null ? 0.0D : defender.defense().hardDef();
        }
        return defender.defense().hardDef() * RoCombatStatusService.physicalDefenseMultiplier(defender.entity());
    }
}
