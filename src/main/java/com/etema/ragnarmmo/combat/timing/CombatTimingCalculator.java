package com.etema.ragnarmmo.combat.timing;

import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.ArcherSkillFormulaService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class CombatTimingCalculator {
    private CombatTimingCalculator() {
    }

    public static TimingProfile resolveSkillTiming(ServerPlayer player, int variableCastTicks, int fixedCastTicks,
            int afterCastDelayTicks, int globalDelayTicks, int cooldownTicks) {
        int adjustedVariable = adjustVariableCastTicks(player, variableCastTicks);
        return new TimingProfile(adjustedVariable, Math.max(0, fixedCastTicks), Math.max(0, afterCastDelayTicks),
                Math.max(0, globalDelayTicks), Math.max(0, cooldownTicks));
    }

    public static int adjustVariableCastTicks(ServerPlayer player, int variableCastTicks) {
        if (player == null || variableCastTicks <= 0) {
            return 0;
        }
        int dex = Math.max(1, (int) Math.round(StatAttributes.getTotal(player, StatKeys.DEX))
                + AcolyteSkillFormulaService.statusStatModifier(player, StatKeys.DEX)
                + ArcherSkillFormulaService.statusStatModifier(player, StatKeys.DEX));
        int intel = Math.max(1, (int) Math.round(StatAttributes.getTotal(player, StatKeys.INT))
                + AcolyteSkillFormulaService.statusStatModifier(player, StatKeys.INT)
                + ArcherSkillFormulaService.statusStatModifier(player, StatKeys.INT));
        double seconds = variableCastTicks / 20.0D;
        return Math.max(0, (int) Math.round(CombatMath.computeCastTime(seconds, dex, intel, false) * 20.0D));
    }

    public static int computeGlobalDelayTicks(Player player, int baseDelayTicks) {
        return Math.max(0, baseDelayTicks);
    }

    public record TimingProfile(
            int variableCastTicks,
            int fixedCastTicks,
            int afterCastDelayTicks,
            int globalDelayTicks,
            int cooldownTicks) {
        public int totalCastTicks() {
            return Math.max(0, variableCastTicks) + Math.max(0, fixedCastTicks);
        }
    }
}
