package com.etema.ragnarmmo.jobs.runtime;

import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
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
            int sword = skills.getSkillLevel(id("sword_mastery"));
            int twoHand = skills.getSkillLevel(id("two_hand_mastery"));
            int hpRecovery = skills.getSkillLevel(id("increase_hp_recovery"));
            int owl = skills.getSkillLevel(id("owls_eye"));
            int vulture = skills.getSkillLevel(id("vultures_eye"));
            int dodge = skills.getSkillLevel(id("improve_dodge"));
            int doubleAttack = skills.getSkillLevel(id("double_attack"));
            int divine = skills.getSkillLevel(id("divine_protection"));
            int demon = skills.getSkillLevel(id("demon_bane"));
            int spRecovery = skills.getSkillLevel(id("increase_sp_recovery"));
            int weight = skills.getSkillLevel(id("enlarge_weight_limit"));
            int survival = skills.getSkillLevel(id("survival_instinct"));

            derived.physicalAttack += sword * 4.0D + twoHand * 4.0D + demon * 3.0D;
            derived.physicalAttackMin += sword * 2.0D + twoHand * 2.0D;
            derived.physicalAttackMax += sword * 4.0D + twoHand * 5.0D;
            derived.accuracy += owl;
            derived.flee += dodge * 3.0D;
            derived.perfectDodge += dodge * 0.15D;
            derived.projectileVelocityMult += vulture * 0.03D;
            derived.projectileGravityMult = Math.max(0.5D, derived.projectileGravityMult - vulture * 0.02D);
            derived.criticalChance += doubleAttack * 0.25D;
            derived.physicalDamageReduction += divine * 0.3D;
            derived.maxHealth += hpRecovery * 4.0D + survival * 6.0D;
            derived.healthRegenPerSecond += hpRecovery * 0.08D + survival * 0.04D;
            derived.maxMana += spRecovery * 3.0D;
            derived.manaRegenPerSecond += spRecovery * 0.08D;
            derived.maxSP += weight * 2.0D;
        });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("ragnarmmo", path);
    }
}
