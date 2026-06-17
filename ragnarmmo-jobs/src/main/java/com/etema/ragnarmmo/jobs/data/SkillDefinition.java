package com.etema.ragnarmmo.jobs.data;

import com.etema.ragnarmmo.common.api.jobs.JobType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public record SkillDefinition(
        ResourceLocation id,
        String displayName,
        String category,
        String tier,
        String usage,
        int maxLevel,
        int upgradeCost,
        boolean canUpgradeWithPoints,
        int cooldownTicks,
        int castDelayTicks,
        int baseCost,
        int costPerLevel,
        Map<ResourceLocation, Integer> requirements,
        Map<Integer, Map<String, Double>> levelData,
        Set<JobType> jobs) {
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(usage) || category.toUpperCase(java.util.Locale.ROOT).contains("ACTIVE");
    }

    public int resourceCost(int level) {
        return getLevelInt("sp_cost", level, baseCost + Math.max(0, level - 1) * costPerLevel);
    }

    public int getLevelInt(String key, int level, int fallback) {
        return (int) Math.round(getLevelDouble(key, level, fallback));
    }

    public double getLevelDouble(String key, int level, double fallback) {
        Map<String, Double> values = levelData.get(level);
        if (values == null) {
            values = levelData.get(Math.max(1, Math.min(maxLevel, level)));
        }
        if (values == null) {
            return fallback;
        }
        return values.getOrDefault(key, fallback);
    }
}
