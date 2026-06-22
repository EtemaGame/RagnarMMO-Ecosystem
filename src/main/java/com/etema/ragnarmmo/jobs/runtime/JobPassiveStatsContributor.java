package com.etema.ragnarmmo.jobs.runtime;

import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.combat.formula.SwordmanSkillFormulaService;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsContributor;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class JobPassiveStatsContributor implements DerivedStatsContributor {
    public static final JobPassiveStatsContributor INSTANCE = new JobPassiveStatsContributor();

    private JobPassiveStatsContributor() {
    }

    @Override
    public void contribute(ServerPlayer player, IPlayerStats stats, DerivedStats derived) {
        PlayerJobSkillsProvider.get(player).ifPresent(skills -> {
            int hpRecovery = skills.getSkillLevel(id("increase_hp_recovery"));
            int vulture = skills.getSkillLevel(id("vultures_eye"));
            int dodge = skills.getSkillLevel(id("improve_dodge"));
            int spRecovery = skills.getSkillLevel(id("increase_sp_recovery"));
            int weight = skills.getSkillLevel(id("enlarge_weight_limit"));
            int survival = skills.getSkillLevel(id("survival_instinct"));

            derived.accuracy += vulture;
            derived.flee += dodge * 3.0D;
            derived.perfectDodge += dodge * 0.15D;
            derived.maxHealth += survival * 6.0D;
            derived.healthRegenPerSecond += SwordmanSkillFormulaService.increaseHpRecoveryPerSecond(
                    hpRecovery,
                    derived.maxHealth)
                    + survival * 0.04D;
            derived.maxMana += spRecovery * 3.0D;
            derived.manaRegenPerSecond += spRecovery * 0.08D;
            derived.maxSP += weight * 2.0D;
        });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("ragnarmmo", path);
    }
}
