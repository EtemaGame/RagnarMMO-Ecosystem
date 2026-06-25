package com.etema.ragnarmmo.combat.engine;

import com.etema.ragnarmmo.combat.api.CombatRequestContext;
import com.etema.ragnarmmo.combat.api.CombatResolution;
import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import com.etema.ragnarmmo.combat.contract.*;
import com.etema.ragnarmmo.combat.profile.HandAttackProfileResolver;
import com.etema.ragnarmmo.combat.state.CombatActorState;
import com.etema.ragnarmmo.combat.util.CombatDebugLog;
import com.etema.ragnarmmo.skills.api.RagnarSkillDefinitionsAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RagnarSkillResolver {
    private final RagnarHitCalculator hitCalculator;
    private final RagnarDamageCalculator damageCalculator;
    private final CombatContract combatContract;

    public RagnarSkillResolver(RagnarHitCalculator hitCalculator, RagnarDamageCalculator damageCalculator, CombatContract combatContract) {
        this.hitCalculator = hitCalculator;
        this.damageCalculator = damageCalculator;
        this.combatContract = combatContract;
    }

    public List<CombatResolution> resolveSkill(CombatRequestContext ctx, CombatActorState actorState, long nowTick) {
        ServerPlayer player = ctx.actor();
        ResourceLocation skillId = parseSkillId(ctx.skillId());
        if (skillId == null) {
            return Collections.emptyList();
        }

        int level = ctx.metadata().get("level") instanceof Integer l ? l : 1;
        Optional<SkillCombatSpec> specOpt = SkillCombatSpecResolver.resolve(skillId, level);
        if (specOpt.isEmpty()) {
            return Collections.emptyList();
        }

        CombatantProfile attackerProfile = CombatantProfileResolver.resolvePlayer(player, HandAttackProfileResolver.resolve(player, ctx.offHand()).orElse(null)).orElse(null);
        if (attackerProfile == null) {
            return Collections.emptyList();
        }

        List<CombatResolution> results = new ArrayList<>();
        SkillCombatSpec spec = applyMetadataOverrides(specOpt.get(), ctx.metadata());
        for (var candidate : ctx.candidates()) {
            net.minecraft.world.entity.Entity entity = player.serverLevel().getEntity(candidate.entityId());
            if (!(entity instanceof LivingEntity target) || target == player) {
                continue;
            }
            CombatantProfile defenderProfile = resolveTargetProfile(target);
            if (defenderProfile == null) {
                continue;
            }
            var contractResult = combatContract.resolveSkill(
                    attackerProfile,
                    defenderProfile,
                    new ActionIntent.SkillIntent(skillId, level, target.getId()),
                    spec,
                    new java.util.Random(nowTick * 31L + player.getId() * 17L + target.getId()));
            if (contractResult.rejected() || contractResult.resolution() == null) {
                CombatDebugLog.logValidationReject(ctx, "SKILL_CONTRACT_REJECTED_" + contractResult.rejectReason());
                continue;
            }
            results.add(contractResult.resolution());
        }
        return results;
    }

    private static CombatantProfile resolveTargetProfile(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return CombatantProfileResolver.resolvePlayer(player, null).orElse(null);
        }
        if (target instanceof net.minecraft.world.entity.Mob mob) {
            return CombatantProfileResolver.resolveMob(mob, CombatStrictMode.current()).orElse(null);
        }
        return null;
    }

    private static ResourceLocation parseSkillId(String rawSkillId) {
        if (rawSkillId == null || rawSkillId.isBlank()) {
            return null;
        }
        return rawSkillId.contains(":")
                ? ResourceLocation.tryParse(rawSkillId)
                : ResourceLocation.fromNamespaceAndPath("ragnarmmo", rawSkillId);
    }

    private static SkillCombatSpec applyMetadataOverrides(SkillCombatSpec spec, Map<String, Object> metadata) {
        if (spec == null || metadata == null) {
            return spec;
        }
        Object hitCountRaw = metadata.get("_hit_count_override");
        if (!(hitCountRaw instanceof Number hitCountNumber)) {
            return spec;
        }
        int hitCount = Math.max(1, hitCountNumber.intValue());
        return new SkillCombatSpec(
                spec.damageType(),
                spec.element(),
                spec.hitPolicy(),
                spec.damagePercent(),
                hitCount,
                spec.aoeRadius(),
                spec.splashRatio(),
                spec.accuracyBonus(),
                spec.defenseBypassPercent(),
                spec.flatDamageBonus(),
                spec.undeadMultiplier(),
                spec.rangeType(),
                spec.elementPolicy(),
                spec.defensePolicy(),
                spec.multiHitPolicy());
    }
}
