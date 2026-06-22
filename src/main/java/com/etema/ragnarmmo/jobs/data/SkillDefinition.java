package com.etema.ragnarmmo.jobs.data;

import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.skills.api.ISkillDefinition;
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
        String texture,
        Map<ResourceLocation, Integer> requirements,
        Map<Integer, Map<String, Double>> levelData,
        Set<JobType> jobs) implements ISkillDefinition {
    @Override public ResourceLocation getId() { return id; }
    @Override public String getDisplayName() { return displayName; }
    @Override public String getCategory() { return category; }
    @Override public String getTier() { return tier; }
    @Override public String getUsage() { return usage; }
    @Override public int getMaxLevel() { return maxLevel; }
    @Override public int getUpgradeCost() { return upgradeCost; }
    @Override public boolean canUpgradeWithPoints() { return canUpgradeWithPoints; }
    @Override public int getCooldownTicks() { return cooldownTicks; }
    @Override public int getCastDelayTicks() { return castDelayTicks; }
    @Override public int getBaseCost() { return baseCost; }
    @Override public int getCostPerLevel() { return costPerLevel; }
    public String texture() { return texture; }
    @Override public Map<ResourceLocation, Integer> getRequirements() { return requirements; }
    @Override public Map<Integer, Map<String, Double>> getLevelDataMap() { return levelData; }
    @Override public Set<JobType> getJobs() { return jobs; }
}
