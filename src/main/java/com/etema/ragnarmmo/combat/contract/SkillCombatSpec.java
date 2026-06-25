package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.combat.element.ElementType;

public record SkillCombatSpec(
        RagnarDamageType damageType,
        ElementType element,
        SkillHitPolicy hitPolicy,
        double damagePercent,
        int hitCount,
        double aoeRadius,
        double splashRatio,
        double accuracyBonus,
        double defenseBypassPercent,
        double flatDamageBonus,
        double undeadMultiplier,
        SkillRangeType rangeType,
        SkillElementPolicy elementPolicy,
        SkillDefensePolicy defensePolicy,
        SkillMultiHitPolicy multiHitPolicy) {
    public SkillCombatSpec {
        damageType = damageType == null ? RagnarDamageType.PHYSICAL : damageType;
        element = element == null ? ElementType.NEUTRAL : element;
        hitPolicy = hitPolicy == null ? SkillHitPolicy.BASIC_ATTACK : hitPolicy;
        damagePercent = Math.max(0.0D, damagePercent);
        hitCount = Math.max(1, hitCount);
        aoeRadius = Math.max(0.0D, aoeRadius);
        splashRatio = Math.max(0.0D, splashRatio);
        accuracyBonus = Math.max(0.0D, accuracyBonus);
        defenseBypassPercent = Math.max(0.0D, defenseBypassPercent);
        flatDamageBonus = Math.max(0.0D, flatDamageBonus);
        undeadMultiplier = Math.max(1.0D, undeadMultiplier);
        rangeType = rangeType == null ? defaultRangeType(damageType) : rangeType;
        elementPolicy = elementPolicy == null ? defaultElementPolicy(damageType) : elementPolicy;
        defensePolicy = defensePolicy == null ? SkillDefensePolicy.NORMAL : defensePolicy;
        multiHitPolicy = multiHitPolicy == null ? (hitCount > 1 ? SkillMultiHitPolicy.PER_HIT : SkillMultiHitPolicy.SINGLE) : multiHitPolicy;
    }

    private static SkillRangeType defaultRangeType(RagnarDamageType damageType) {
        return damageType == RagnarDamageType.MAGICAL ? SkillRangeType.MAGIC : SkillRangeType.MELEE;
    }

    private static SkillElementPolicy defaultElementPolicy(RagnarDamageType damageType) {
        return damageType == RagnarDamageType.MAGICAL ? SkillElementPolicy.SKILL : SkillElementPolicy.WEAPON;
    }
}
