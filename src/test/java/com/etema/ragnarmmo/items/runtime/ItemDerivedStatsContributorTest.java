package com.etema.ragnarmmo.items.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

class ItemDerivedStatsContributorTest {
    @Test
    void additiveArmorAttributeContributesHardDefense() {
        AttributeModifier armor = new AttributeModifier(
                UUID.randomUUID(),
                "Test armor",
                6.0D,
                AttributeModifier.Operation.ADDITION);

        double hardDefense = ItemDerivedStatsContributor.armorHardDefenseFromModifiers(List.of(armor));

        assertEquals(6.0D, hardDefense, 0.0001D);
    }

    @Test
    void nonAdditiveArmorAttributeDoesNotBecomeHardDefense() {
        AttributeModifier multiplier = new AttributeModifier(
                UUID.randomUUID(),
                "Test armor multiplier",
                0.5D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        double hardDefense = ItemDerivedStatsContributor.armorHardDefenseFromModifiers(List.of(multiplier));

        assertEquals(0.0D, hardDefense, 0.0001D);
    }
}
