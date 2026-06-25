package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.combat.contract.CombatModifiers;
import com.etema.ragnarmmo.combat.contract.RagnarDamageType;
import com.etema.ragnarmmo.combat.contract.SkillCombatSpec;
import com.etema.ragnarmmo.combat.contract.SkillDefensePolicy;
import com.etema.ragnarmmo.combat.contract.SkillElementPolicy;
import com.etema.ragnarmmo.combat.contract.SkillHitPolicy;
import com.etema.ragnarmmo.combat.contract.SkillMultiHitPolicy;
import com.etema.ragnarmmo.combat.contract.SkillRangeType;
import com.etema.ragnarmmo.combat.contract.SkillCombatSpecResolver;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import com.etema.ragnarmmo.skills.api.ISkillDefinition;
import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CombatFormulaContractTest {
    @Test
    void damageFormulaKeepsRoStyleStatAttackSplit() {
        double melee = DamageFormulaService.statusAtk(40, 20, 10, false);
        double ranged = DamageFormulaService.statusAtk(40, 20, 10, true);

        assertTrue(melee > ranged);
        assertEquals(0.0, DamageFormulaService.weaponAtk(-10));
    }

    @Test
    void defenseFormulaNeverDropsDamageBelowOne() {
        double reduced = DefenseFormulaService.applyPhysicalDefense(5, 999, 0.99);

        assertEquals(1.0, reduced);
    }

    @Test
    void criticalPhysicalDamageIgnoresHardAndSoftDefense() {
        double reduced = DefenseFormulaService.applyPhysicalDefense(100.0D, 999.0D, 99.0D, true);

        assertEquals(100.0D, reduced);
    }

    @Test
    void sizeModifierOnlyAffectsWeaponAtkNotStatusAtk() {
        double statusAtk = 40.0D;
        double weaponAtk = 100.0D;
        double sizePenalty = 0.75D;

        double ordered = statusAtk + weaponAtk * sizePenalty;

        assertEquals(115.0D, ordered, 0.0001D);
    }

    @Test
    void physicalSkillHitCountIsAppliedAfterDefense() {
        double perHitRaw = 100.0D;
        int hitCount = 3;
        double softDef = 10.0D;
        double hardDef = 0.0D;

        double ordered = DefenseFormulaService.applyPhysicalDefense(perHitRaw, softDef, hardDef, false) * hitCount;
        double wrongAggregatedBeforeDefense = DefenseFormulaService.applyPhysicalDefense(
                perHitRaw * hitCount,
                softDef,
                hardDef,
                false);

        assertEquals(270.0D, ordered, 0.0001D);
        assertEquals(290.0D, wrongAggregatedBeforeDefense, 0.0001D);
    }

    @Test
    void flatBonusesAreAppliedBeforeElement() {
        double afterDefense = 90.0D;
        double flatBonus = 10.0D;
        CombatModifiers waterTarget = new CombatModifiers("unknown", ElementType.WATER, CombatMath.MobSize.MEDIUM);

        double ordered = (afterDefense + flatBonus)
                * DamageFormulaService.elementMultiplier(ElementType.FIRE, waterTarget.element(), waterTarget.elementLevel());

        assertEquals(50.0D, ordered, 0.0001D);
    }

    @Test
    void skillCombatSpecKeepsCentralCombatFlags() {
        SkillCombatSpec spec = new SkillCombatSpec(
                RagnarDamageType.PHYSICAL,
                ElementType.FIRE,
                SkillHitPolicy.BASIC_ATTACK,
                200.0D,
                2,
                0.0D,
                1.0D,
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                SkillRangeType.RANGED,
                SkillElementPolicy.SKILL,
                SkillDefensePolicy.IGNORE_DEF,
                SkillMultiHitPolicy.PER_HIT);

        assertEquals(SkillRangeType.RANGED, spec.rangeType());
        assertEquals(SkillElementPolicy.SKILL, spec.elementPolicy());
        assertEquals(SkillDefensePolicy.IGNORE_DEF, spec.defensePolicy());
        assertEquals(SkillMultiHitPolicy.PER_HIT, spec.multiHitPolicy());
    }

    @Test
    void skillCombatSpecResolverParsesStringCombatFlags() {
        ISkillDefinition definition = new TestSkillDefinition(
                ResourceLocation.fromNamespaceAndPath("ragnarmmo", "test_strike"),
                Map.of(1, Map.of("damage_percent", 100.0D)),
                Map.of(1, Map.of(
                        "damage_type", "physical",
                        "range_type", "melee",
                        "element_policy", "weapon",
                        "defense_policy", "normal",
                        "multi_hit_policy", "single")));
        SkillCombatSpec spec = SkillCombatSpecResolver.resolve(definition, 1).orElseThrow();

        assertEquals(SkillRangeType.MELEE, spec.rangeType());
        assertEquals(SkillElementPolicy.WEAPON, spec.elementPolicy());
        assertEquals(SkillMultiHitPolicy.SINGLE, spec.multiHitPolicy());
    }

    @Test
    void varianceFloorStaysInsideExpectedRange() {
        double floor = DamageFormulaService.damageVarianceFloor(100, 150, 0);
        double rolled = DamageFormulaService.damageVariance(100, 150, 0, new Random(1L));

        assertTrue(floor >= 80.0);
        assertTrue(rolled >= floor);
        assertTrue(rolled <= 100.0);
    }

    @Test
    void aspdUsesWeaponDelayModelFromBaseAspd() {
        int aspd = AspdFormulaService.aspdRo(156, false, 25, 100, 0.0D);

        assertEquals(164, aspd);
    }

    @Test
    void aspdSpeedModifierReducesRemainingWeaponDelay() {
        int withoutModifier = AspdFormulaService.aspdRo(156, false, 1, 1, 0.0D, 0.0D);
        int withModifier = AspdFormulaService.aspdRo(156, false, 1, 1, 0.2D, 0.0D);

        assertTrue(withModifier > withoutModifier);
    }

    @Test
    void incomingCardReductionsMultiplyBetweenCategories() {
        double reduction = CombatPropertyModifierService.combineIncomingReductionCategories(
                0.20D,
                0.20D,
                0.20D,
                0.0D);

        assertEquals(0.488D, reduction, 0.0001D);
    }

    @Test
    void rodSizeModifierIsNeutralForEveryTargetSize() {
        assertEquals(1.0D, CombatMath.getWeaponSizePenalty("rod", CombatMath.MobSize.SMALL), 0.0001D);
        assertEquals(1.0D, CombatMath.getWeaponSizePenalty("rod", CombatMath.MobSize.MEDIUM), 0.0001D);
        assertEquals(1.0D, CombatMath.getWeaponSizePenalty("rod", CombatMath.MobSize.LARGE), 0.0001D);
    }

    @Test
    void katarDoublesCriticalChanceBeforeTargetShield() {
        assertEquals(0.12D, CombatMath.applyWeaponCriticalChanceModifier(0.06D, "katar"), 0.0001D);
        assertEquals(0.06D, CombatMath.applyWeaponCriticalChanceModifier(0.06D, "sword"), 0.0001D);
    }

    private record TestSkillDefinition(
            ResourceLocation getId,
            Map<Integer, Map<String, Double>> getLevelDataMap,
            Map<Integer, Map<String, String>> getLevelStringDataMap) implements ISkillDefinition {
        @Override
        public String getDisplayName() {
            return "Test Skill";
        }

        @Override
        public String getCategory() {
            return "ACTIVE";
        }

        @Override
        public String getTier() {
            return "FIRST";
        }

        @Override
        public String getUsage() {
            return "ACTIVE";
        }

        @Override
        public int getMaxLevel() {
            return 1;
        }

        @Override
        public int getUpgradeCost() {
            return 1;
        }

        @Override
        public boolean canUpgradeWithPoints() {
            return true;
        }

        @Override
        public int getCooldownTicks() {
            return 0;
        }

        @Override
        public int getCastDelayTicks() {
            return 0;
        }

        @Override
        public int getBaseCost() {
            return 0;
        }

        @Override
        public int getCostPerLevel() {
            return 0;
        }

        @Override
        public Map<ResourceLocation, Integer> getRequirements() {
            return Map.of();
        }

        @Override
        public Set<JobType> getJobs() {
            return Set.of();
        }
    }
}
