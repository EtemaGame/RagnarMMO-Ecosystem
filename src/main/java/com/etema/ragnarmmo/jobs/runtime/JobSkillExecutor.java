package com.etema.ragnarmmo.jobs.runtime;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.player.RoPlayerSyncDomain;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import com.etema.ragnarmmo.jobs.data.SkillDefinition;
import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import com.etema.ragnarmmo.jobs.net.JobSkillsSyncService;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import com.etema.ragnarmmo.combat.formula.ArcherSkillFormulaService;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public final class JobSkillExecutor {
    private JobSkillExecutor() {
    }

    public static boolean use(ServerPlayer player, ResourceLocation skillId) {
        if (player == null || skillId == null) {
            return false;
        }

        var definitionOpt = SkillDefinitionRegistry.get(skillId);
        if (definitionOpt.isEmpty() || !definitionOpt.get().isActive()) {
            return reject(player, "Skill is not active: " + skillId);
        }
        if (RoCombatStatusService.blocksCast(player)) {
            return reject(player, "You are silenced.");
        }
        if (RoCombatStatusService.hasHiding(player)) {
            if ("ragnarmmo".equals(skillId.getNamespace()) && "hiding".equals(skillId.getPath())) {
                RoCombatStatusService.revealHiding(player);
                return true;
            }
            return reject(player, "You cannot use skills while hiding.");
        }

        var statsOpt = RagnarCoreAPI.get(player);
        if (statsOpt.isEmpty()) {
            return reject(player, "Missing core stats.");
        }
        JobType job = JobType.fromId(statsOpt.get().getJobId());
        if (!SkillDefinitionRegistry.isAllowedForJob(skillId, job)) {
            return reject(player, "Your job cannot use this skill.");
        }

        return PlayerJobSkillsProvider.get(player).map(skills -> {
            int level = skills.getSkillLevel(skillId);
            if (level <= 0) {
                return reject(player, "Skill is not learned.");
            }
            long now = player.level().getGameTime();
            if (skills.isOnCooldown(skillId, now)) {
                return reject(player, "Skill is on cooldown.");
            }

            var definition = definitionOpt.get();
            int resourceCost = definition.resourceCost(level);
            if (resourceCost > 0 && !statsOpt.get().consumeSP(resourceCost)) {
                return reject(player, "Not enough SP.");
            }
            double range = effectiveRange(player, skillId, definition, level);
            var context = new JobSkillContext(
                    player,
                    skillId,
                    definition,
                    level,
                    JobSkillTargeting.findEntityInSight(player, range),
                    JobSkillTargeting.findGroundInSight(player, range));
            boolean accepted = JobSkillCastService.startOrRun(context, resourceCost);
            if (accepted && resourceCost > 0) {
                PlayerStatsSyncService.sync(player, statsOpt.get(), RoPlayerSyncDomain.RESOURCES.bit());
            } else if (!accepted && resourceCost > 0) {
                statsOpt.get().addSP(resourceCost);
            }
            return accepted;
        }).orElse(false);
    }

    static boolean completeCast(ServerPlayer player, ResourceLocation skillId, int level, Integer targetEntityId,
            BlockPos groundTarget, int resourceCost, int cooldown, long now) {
        if (player == null || skillId == null) {
            return false;
        }
        Optional<SkillDefinition> definitionOpt = SkillDefinitionRegistry.get(skillId);
        if (definitionOpt.isEmpty()) {
            return false;
        }
        LivingEntity target = null;
        if (targetEntityId != null && player.level().getEntity(targetEntityId) instanceof LivingEntity living) {
            target = living;
        }
        JobSkillContext context = new JobSkillContext(
                player,
                skillId,
                definitionOpt.get(),
                level,
                Optional.ofNullable(target),
                Optional.ofNullable(groundTarget));
        return executeResolved(context, resourceCost, now, cooldown);
    }

    static boolean executeResolved(JobSkillContext context, int resourceCost, long now, int cooldown) {
        ServerPlayer player = context.player();
        boolean executed = JobSkillEffectRegistry.get(context.skillId())
                .map(effect -> effect.execute(context))
                .orElseGet(() -> {
                    player.sendSystemMessage(Component.literal(
                            "Skill ready but no effect is ported yet: " + context.definition().displayName())
                            .withStyle(ChatFormatting.YELLOW));
                    return true;
                });
        if (executed) {
            PlayerJobSkillsProvider.get(player).ifPresent(skills -> {
                if (cooldown > 0) {
                    skills.setCooldown(context.skillId(), now, cooldown);
                }
            });
            JobSkillsSyncService.sync(player);
        } else if (resourceCost > 0) {
            RagnarCoreAPI.get(player).ifPresent(stats -> {
                stats.addSP(resourceCost);
                PlayerStatsSyncService.sync(player, stats, RoPlayerSyncDomain.RESOURCES.bit());
            });
        }
        return executed;
    }

    private static boolean reject(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
        return false;
    }

    private static double effectiveRange(ServerPlayer player, ResourceLocation skillId, SkillDefinition definition,
            int level) {
        double range = definition.getLevelDouble("range", level, 16.0D);
        if (isArcherRangedSkill(skillId)) {
            range += ArcherSkillFormulaService.vultureRangeBonus(player);
        }
        return Math.max(1.0D, range);
    }

    private static boolean isArcherRangedSkill(ResourceLocation skillId) {
        if (skillId == null || !"ragnarmmo".equals(skillId.getNamespace())) {
            return false;
        }
        return switch (skillId.getPath()) {
            case "double_strafe", "arrow_shower" -> true;
            default -> false;
        };
    }
}
