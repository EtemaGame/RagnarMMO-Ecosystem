package com.etema.ragnarmmo.combat.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.etema.ragnarmmo.combat.contract.CombatStats;
import com.etema.ragnarmmo.combat.contract.MagicAttackProfile;
import com.etema.ragnarmmo.combat.element.ElementType;
import org.junit.jupiter.api.Test;

class CombatApiContractTest {
    @Test
    void combatContractsExposeStableDomainValues() {
        assertEquals(CombatHitResultType.CRIT, CombatHitResultType.valueOf("CRIT"));
        assertEquals(TargetRejectReason.TARGET_OUT_OF_RANGE, TargetRejectReason.valueOf("TARGET_OUT_OF_RANGE"));
        assertEquals(ElementType.UNDEAD, ElementType.valueOf("UNDEAD"));
    }

    @Test
    void pureProfilesRemainSimpleValueObjects() {
        CombatStats stats = new CombatStats(1, 2, 3, 4, 5, 6, 7);
        MagicAttackProfile magic = new MagicAttackProfile(10, 30);

        assertEquals(7, stats.level());
        assertEquals(20.0, magic.averageMagicAttack());
    }
}
