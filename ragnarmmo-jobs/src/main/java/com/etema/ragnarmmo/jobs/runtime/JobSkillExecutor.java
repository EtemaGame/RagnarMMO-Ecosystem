package com.etema.ragnarmmo.jobs.runtime;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.player.RoPlayerSyncDomain;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import com.etema.ragnarmmo.jobs.net.JobSkillsSyncService;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

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
            var context = new JobSkillContext(
                    player,
                    skillId,
                    definition,
                    level,
                    JobSkillTargeting.findEntityInSight(player, 16.0D));
            boolean executed = JobSkillEffectRegistry.get(skillId)
                    .map(effect -> effect.execute(context))
                    .orElseGet(() -> {
                        player.sendSystemMessage(Component.literal(
                                "Skill ready but no effect is ported yet: " + definition.displayName())
                                .withStyle(ChatFormatting.YELLOW));
                        return true;
                    });
            if (executed) {
                int cooldown = Math.max(
                        definition.getLevelInt("cooldown_ticks", level, definition.cooldownTicks()),
                        definition.getLevelInt("cast_delay_ticks", level, definition.castDelayTicks()));
                if (cooldown > 0) {
                    skills.setCooldown(skillId, now, cooldown);
                }
                if (resourceCost > 0) {
                    PlayerStatsSyncService.sync(player, statsOpt.get(), RoPlayerSyncDomain.RESOURCES.bit());
                }
                JobSkillsSyncService.sync(player);
            } else if (resourceCost > 0) {
                statsOpt.get().addSP(resourceCost);
            }
            return executed;
        }).orElse(false);
    }

    private static boolean reject(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
        return false;
    }
}
