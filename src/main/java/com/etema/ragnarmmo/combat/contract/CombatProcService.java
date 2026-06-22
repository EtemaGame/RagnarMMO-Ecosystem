package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.combat.formula.ThiefSkillFormulaService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;

import java.util.Random;

public final class CombatProcService {
    private static final ResourceLocation DOUBLE_ATTACK = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "double_attack");

    private CombatProcService() {
    }

    public static DoubleAttackProc rollDoubleAttack(CombatantProfile attacker, Random rng) {
        int level = doubleAttackLevel(attacker);
        if (level <= 0) {
            return DoubleAttackProc.inactive();
        }
        boolean active = rng != null && rng.nextDouble() < ThiefSkillFormulaService.doubleAttackChance(level);
        return new DoubleAttackProc(active, active ? ThiefSkillFormulaService.doubleAttackHitBonus(level) : 0,
                active ? 2 : 1);
    }

    private static int doubleAttackLevel(CombatantProfile attacker) {
        if (!(attacker.entity() instanceof ServerPlayer player)) {
            return 0;
        }
        if (!player.getMainHandItem().is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ragnarmmo", "daggers")))) {
            return 0;
        }
        return RagnarSkillsAPI.get(player).map(skills -> skills.getSkillLevel(DOUBLE_ATTACK)).orElse(0);
    }

    public record DoubleAttackProc(boolean active, int hitBonus, int hitCount) {
        public static DoubleAttackProc inactive() {
            return new DoubleAttackProc(false, 0, 1);
        }
    }
}
