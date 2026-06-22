package com.etema.ragnarmmo.common.api.lifeskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LifeSkillTypeContractTest {
    @Test
    void lifeSkillIdsRemainStableAndIndependentFromJobs() {
        assertEquals("mining", LifeSkillType.MINING.getId());
        assertEquals("lifeskill.ragnarmmo.fishing", LifeSkillType.FISHING.getTranslationKey());
        assertEquals(LifeSkillType.WOODCUTTING, LifeSkillType.fromId("woodcutting"));
        assertNull(LifeSkillType.fromId("swordsman"));
    }
}
