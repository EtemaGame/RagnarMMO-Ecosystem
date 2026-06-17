package com.etema.ragnarmmo.jobs.runtime;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.player.RoPlayerSyncDomain;
import com.etema.ragnarmmo.common.api.stats.ChangeReason;
import com.etema.ragnarmmo.core.config.RagnarCoreConfigs;
import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import com.etema.ragnarmmo.jobs.net.JobSkillsSyncService;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class JobChangeService {
    private static final ResourceLocation BASIC_SKILL = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "basic_skill");

    private JobChangeService() {
    }

    public static boolean changeToFirstClass(ServerPlayer player, JobType requestedJob) {
        if (player == null || requestedJob == null || !JobType.FIRST_CLASSES.contains(requestedJob)) {
            return reject(player, "Invalid first class.");
        }

        var statsOpt = RagnarCoreAPI.get(player);
        if (statsOpt.isEmpty()) {
            return reject(player, "Missing core stats.");
        }

        return PlayerJobSkillsProvider.get(player).map(skills -> {
            var stats = statsOpt.get();
            JobType currentJob = JobType.fromId(stats.getJobId());
            if (!currentJob.canPromoteTo(requestedJob)) {
                return reject(player, "This job cannot change to " + requestedJob.getDisplayName() + ".");
            }

            int requiredJobLevel = RagnarCoreConfigs.SERVER.caps.noviceMaxJobLevel.get();
            if (currentJob == JobType.NOVICE && stats.getJobLevel() < requiredJobLevel) {
                return reject(player, "Novice Job Lv " + requiredJobLevel + " is required.");
            }

            if (currentJob == JobType.NOVICE && skills.getSkillLevel(BASIC_SKILL) < 9) {
                return reject(player, "Basic Skill Lv 9 is required.");
            }

            if (stats.getSkillPoints() > 0) {
                return reject(player, "Spend all Job Skill Points before changing class.");
            }

            clearNonNoviceSkills(skills);
            stats.setJobId(ResourceLocation.fromNamespaceAndPath("ragnarmmo", requestedJob.getId()).toString(),
                    ChangeReason.PLAYER_ACTION);
            stats.setJobLevel(1, ChangeReason.PLAYER_ACTION);
            stats.setJobExp(0);

            PlayerStatsSyncService.sync(player, stats, RoPlayerSyncDomain.PROGRESSION.bit());
            JobSkillsSyncService.sync(player);
            player.sendSystemMessage(Component.literal("Changed job to " + requestedJob.getDisplayName() + ".")
                    .withStyle(ChatFormatting.GREEN));
            return true;
        }).orElseGet(() -> reject(player, "Missing job skills data."));
    }

    private static void clearNonNoviceSkills(com.etema.ragnarmmo.jobs.player.PlayerJobSkills skills) {
        for (ResourceLocation skillId : skills.getSkillLevels().keySet()) {
            boolean novice = SkillDefinitionRegistry.get(skillId)
                    .map(definition -> "NOVICE".equalsIgnoreCase(definition.tier()))
                    .orElse(false);
            if (!novice) {
                skills.setSkillLevel(skillId, 0);
            }
        }
    }

    private static boolean reject(ServerPlayer player, String message) {
        if (player != null) {
            player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
        }
        return false;
    }
}
