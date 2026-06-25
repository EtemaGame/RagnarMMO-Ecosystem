package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class ArcherSkillFormulaService {
    private ArcherSkillFormulaService() {
    }

    public static int owlDexBonus(int skillLevel) {
        return Math.max(0, skillLevel);
    }

    public static int vultureHitBonus(int skillLevel) {
        return Math.max(0, skillLevel);
    }

    public static int vultureRangeBonus(int skillLevel) {
        return Math.max(0, skillLevel);
    }

    public static int vultureRangeBonus(ServerPlayer player) {
        return vultureRangeBonus(skillLevel(player, "vultures_eye"));
    }

    public static int improveConcentrationPercent(int skillLevel) {
        return Math.max(0, 2 + skillLevel);
    }

    public static int improveConcentrationBonus(int eligibleStat, int skillLevel) {
        return (int) Math.floor(Math.max(0, eligibleStat) * improveConcentrationPercent(skillLevel) / 100.0D);
    }

    public static int doubleStrafeTotalRatio(int skillLevel) {
        return 180 + 20 * Math.max(1, skillLevel);
    }

    public static int doubleStrafePerHitRatio(int skillLevel) {
        return doubleStrafeTotalRatio(skillLevel) / 2;
    }

    public static int arrowShowerRatio(int skillLevel) {
        return 75 + 5 * Math.max(1, skillLevel);
    }

    public static int statusStatModifier(LivingEntity entity, StatKeys key) {
        if (!(entity instanceof ServerPlayer player) || key == null) {
            return 0;
        }
        int modifier = 0;
        int owlLevel = skillLevel(player, "owls_eye");
        if (key == StatKeys.DEX) {
            modifier += owlDexBonus(owlLevel);
        }

        int concentrationLevel = RoCombatStatusService.improveConcentrationLevel(player);
        if (concentrationLevel > 0 && (key == StatKeys.DEX || key == StatKeys.AGI)) {
            int eligible = baseEligibleStat(player, key);
            if (key == StatKeys.DEX) {
                eligible += owlDexBonus(owlLevel);
            }
            modifier += improveConcentrationBonus(eligible, concentrationLevel);
        }
        return modifier;
    }

    private static int baseEligibleStat(ServerPlayer player, StatKeys key) {
        return Math.max(1, (int) Math.round(StatAttributes.getTotal(player, key)));
    }

    private static int skillLevel(ServerPlayer player, String path) {
        if (player == null) {
            return 0;
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("ragnarmmo", path);
        return PlayerJobSkillsProvider.get(player).map(skills -> skills.getSkillLevel(id)).orElse(0);
    }
}
