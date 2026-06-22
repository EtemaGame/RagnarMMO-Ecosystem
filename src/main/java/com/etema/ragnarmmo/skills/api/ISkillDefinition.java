package com.etema.ragnarmmo.skills.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public interface ISkillDefinition {
    ResourceLocation getId();

    String getDisplayName();

    String getCategory();

    String getTier();

    String getUsage();

    int getMaxLevel();

    int getUpgradeCost();

    boolean canUpgradeWithPoints();

    int getCooldownTicks();

    int getCastDelayTicks();

    int getBaseCost();

    int getCostPerLevel();

    Map<ResourceLocation, Integer> getRequirements();

    Map<Integer, Map<String, Double>> getLevelDataMap();

    Set<com.etema.ragnarmmo.common.api.jobs.JobType> getJobs();

    default boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(getUsage())
                || getCategory() != null && getCategory().toUpperCase(java.util.Locale.ROOT).contains("ACTIVE");
    }

    default int getLevelInt(String key, int level, int fallback) {
        return (int) Math.round(getLevelDouble(key, level, fallback));
    }

    default double getLevelDouble(String key, int level, double fallback) {
        Map<Integer, Map<String, Double>> levels = getLevelDataMap();
        if (levels == null || levels.isEmpty()) {
            return fallback;
        }
        Map<String, Double> values = levels.get(level);
        if (values == null) {
            values = levels.get(Math.max(1, Math.min(getMaxLevel(), level)));
        }
        if (values == null) {
            return fallback;
        }
        return values.getOrDefault(key, fallback);
    }

    default int resourceCost(int level) {
        return getLevelInt("sp_cost", level, getBaseCost() + Math.max(0, level - 1) * getCostPerLevel());
    }
}
