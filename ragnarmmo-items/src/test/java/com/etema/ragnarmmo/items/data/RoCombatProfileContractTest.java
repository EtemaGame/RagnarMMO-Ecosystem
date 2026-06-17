package com.etema.ragnarmmo.items.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class RoCombatProfileContractTest {
    @Test
    void combatProfileMergesOnlySpecifiedOverrides() {
        RoCombatProfile base = new RoCombatProfile(
                RoCombatProfile.WeaponMode.MELEE,
                10,
                0,
                150,
                2,
                0,
                0,
                Set.of("atk"),
                Set.of(),
                Set.of(),
                Set.of());
        RoCombatProfile override = new RoCombatProfile(
                RoCombatProfile.WeaponMode.RANGED,
                0,
                5,
                0,
                8,
                20,
                2.5F,
                Set.of(),
                Set.of("matk"),
                Set.of(),
                Set.of("range"));

        RoCombatProfile merged = RoCombatProfile.merge(base, override);

        assertTrue(merged.isRanged());
        assertEquals(10, merged.atk());
        assertEquals(5, merged.matk());
        assertEquals(150, merged.aspd());
        assertEquals(8, merged.range());
        assertFalse(merged.isEmpty());
    }
}
