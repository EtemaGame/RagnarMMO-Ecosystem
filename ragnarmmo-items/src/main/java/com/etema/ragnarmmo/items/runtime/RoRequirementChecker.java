package com.etema.ragnarmmo.items.runtime;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.items.data.RoItemRule;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class RoRequirementChecker {
    private RoRequirementChecker() {
    }

    public enum CheckResult {
        OK,
        LEVEL_TOO_LOW,
        WRONG_CLASS,
        NO_STATS_DATA
    }

    public static CheckResult check(Player player, RoItemRule rule) {
        if (player == null) {
            return CheckResult.NO_STATS_DATA;
        }
        if (rule == null || !rule.hasRequirements()) {
            return CheckResult.OK;
        }

        Optional<IPlayerStats> statsOpt = RagnarCoreAPI.get(player);
        if (statsOpt.isEmpty()) {
            return CheckResult.NO_STATS_DATA;
        }

        IPlayerStats stats = statsOpt.get();
        if (rule.requiredBaseLevel() > 0 && stats.getLevel() < rule.requiredBaseLevel()) {
            return CheckResult.LEVEL_TOO_LOW;
        }

        if (!rule.allowedJobs().isEmpty()) {
            JobType playerJob = JobType.fromId(stats.getJobId());
            boolean jobAllowed = rule.allowedJobs().stream().anyMatch(playerJob::matchesExactOrAncestor);
            if (!jobAllowed) {
                return CheckResult.WRONG_CLASS;
            }
        }

        return CheckResult.OK;
    }

    public static boolean meetsRequirements(Player player, RoItemRule rule) {
        CheckResult result = check(player, rule);
        return result == CheckResult.OK || result == CheckResult.NO_STATS_DATA;
    }

    public static String getMessageKey(CheckResult result) {
        return switch (result) {
            case LEVEL_TOO_LOW -> "message.ragnarmmo.roitems.level_required";
            case WRONG_CLASS -> "message.ragnarmmo.roitems.class_required";
            case NO_STATS_DATA -> "message.ragnarmmo.roitems.cannot_equip";
            default -> "";
        };
    }
}
