package com.etema.ragnarmmo.items.cards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.util.RandomSource;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CardContractTest {
    @Test
    void cardEquipTypeParsingFallsBackToAny() {
        assertEquals(CardEquipType.WEAPON, CardEquipType.fromString("weapon"));
        assertEquals(CardEquipType.ANY, CardEquipType.fromString("unknown"));
        assertEquals("Headgear", CardEquipType.HEADGEAR.displayName());
    }

    @Test
    void cardDefinitionKeepsDataPackFields() {
        CardDefinition card = new CardDefinition(
                "poring_card",
                "Poring Card",
                "ragnarmmo:poring",
                Map.of("ragnarmmo:luk", 1.0),
                0.01,
                CardEquipType.ARMOR,
                "card.ragnarmmo.poring",
                1001);

        assertEquals("poring_card", card.id());
        assertEquals(CardEquipType.ARMOR, card.equipType());
    }

    @Test
    void cardRegistryIndexesByIdAndMob() {
        CardDefinition card = new CardDefinition(
                "lunatic_card",
                "Lunatic Card",
                "ragnarmmo:lunatic",
                Map.of("ragnarmmo:luk", 1.0),
                1.0,
                CardEquipType.HEADGEAR,
                "card.ragnarmmo.lunatic",
                1002);

        CardRegistry registry = CardRegistry.getInstance();
        registry.clear();
        registry.register(card);

        assertEquals(1, registry.size());
        assertSame(card, registry.get("lunatic_card"));
        assertEquals(1, registry.getForMob("ragnarmmo:lunatic").size());
        assertSame(card, registry.rollDrop("ragnarmmo:lunatic", 0, RandomSource.create(1L)));

        registry.clear();
    }
}
