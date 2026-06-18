package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import com.etema.ragnarmmo.combat.api.CombatResolution;
import com.etema.ragnarmmo.combat.engine.RagnarDamageCalculator;
import com.etema.ragnarmmo.combat.engine.RagnarHitCalculator;

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

        CombatHitResultType hit = hitCalculator.rollHitWithCrit(
                attacker.physicalAttack().hit(),
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

        double baseAttack = attacker.physicalAttack().averageAttack();
        double damage = damageCalculator.computePhysicalDamage(
                baseAttack,
                attacker.stats().dex(),
                attacker.stats().luk(),
                rng);

        boolean critical = hit == CombatHitResultType.CRIT;
        if (critical) {
            damage *= Math.max(1.0D, attacker.physicalAttack().critDamageMultiplier());
        }

        damage = damageCalculator.applyModifiers(
                damage,
                attacker.physicalAttack().weapon(),
                defender.modifiers(),
                attacker.modifiers().element(),
                false);
        if (!critical) {
            damage = damageCalculator.applyPhysicalDefense(
                    damage,
                    defender.defense().vit(),
                    defender.defense().agi(),
                    attacker.stats().level(),
                    defender.defense().hardDef());
        }
        damage = PassiveCombatModifierService.applyOutgoingPhysicalDamage(attacker, defender, damage);
        damage = PassiveCombatModifierService.applyIncomingPhysicalDamage(attacker, defender, damage);
        damage = CombatProcService.applyBasicAttackDamageMultiplier(attacker, damage, rng);

        double finalDamage = Math.max(1.0D, damage);
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
                    attacker.physicalAttack().hit(),
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
            damage = damageCalculator.computePhysicalDamage(damage, attacker.stats().dex(), attacker.stats().luk(), rng);
            damage = damageCalculator.applyPhysicalDefense(damage, defender.defense().vit(), defender.defense().agi(),
                    attacker.stats().level(), defender.defense().hardDef());
        } else if (spec.damageType() == RagnarDamageType.MAGICAL) {
            damage = damageCalculator.applyMagicDefense(damage, defender.defense().intel(), defender.defense().vit(),
                    defender.defense().agi(), defender.defense().level(), defender.defense().hardMdef());
        }
        double finalDamage = Math.max(1.0D, damage);
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
}
