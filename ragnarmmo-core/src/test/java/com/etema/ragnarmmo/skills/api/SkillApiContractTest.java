package com.etema.ragnarmmo.skills.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SkillApiContractTest {
    @Test
    void skillMetadataUsesStableIds() {
        assertEquals("swordsman", SkillCategory.SWORDSMAN.getId());
        assertEquals(SkillCategory.MERCHANT, SkillCategory.fromId("merchant"));
        assertNull(SkillCategory.fromId("not_a_job"));

        assertEquals("second", SkillTier.SECOND.getId());
        assertEquals(SkillTier.LIFE, SkillTier.fromId("life"));
    }

    @Test
    void resourceTypeDistinguishesRealResourceCostsFromCooldownOnly() {
        assertTrue(ResourceType.SP.isResource());
        assertTrue(ResourceType.MANA.isResource());
        assertFalse(ResourceType.COOLDOWN_ONLY.isResource());
    }
}
