package com.etema.ragnarmmo.mobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etema.ragnarmmo.common.api.mobs.data.RagnarAggroType;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarAiFlags;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarBlockProximityMode;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementConfig;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementProfile;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementSpeedClass;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarLootBehavior;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMetamorphosis;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarSpawnDefinition;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarSpawnReason;
import com.etema.ragnarmmo.common.api.mobs.data.load.MobDefinitionParser.MetamorphosisTargetValidator;
import com.etema.ragnarmmo.common.api.mobs.data.load.MobDefinitionParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class MobDefinitionParserTest {

    @Test
    void acceptsPassiveAndAggressiveAggroTypes() {
        RagnarAiFlags passive = MobDefinitionParser.parseAiFlags(id("ragnarmmo:poring"), json("""
                {"ai":{"aggroType":"PASSIVE"}}
                """));
        RagnarAiFlags aggressive = MobDefinitionParser.parseAiFlags(id("ragnarmmo:creamy_fear"), json("""
                {"ai":{"aggroType":"AGGRESSIVE"}}
                """));

        assertEquals(RagnarAggroType.PASSIVE, passive.aggroType());
        assertEquals(RagnarAggroType.AGGRESSIVE, aggressive.aggroType());
    }

    @Test
    void rejectsPassiveAliasAndAggroTypeTogether() {
        assertThrows(IllegalArgumentException.class, () -> MobDefinitionParser.parseAiFlags(
                id("ragnarmmo:poring"),
                json("""
                        {"ai":{"passive":true,"aggroType":"PASSIVE"}}
                        """)));
    }

    @Test
    void acceptsLegacyPassiveAlias() {
        RagnarAiFlags flags = MobDefinitionParser.parseAiFlags(id("ragnarmmo:poring"), json("""
                {"ai":{"passive":true}}
                """));
        assertEquals(RagnarAggroType.PASSIVE, flags.aggroType());
    }

    @Test
    void parsesLootBehaviorAndDefaultsForLooters() {
        RagnarLootBehavior behavior = MobDefinitionParser.parseLootBehavior(id("ragnarmmo:poring"), json("""
                {"ai":{"looter":true}}
                """));
        assertNotNull(behavior);
        assertEquals(6.0D, behavior.pickupRadius());
        assertTrue(behavior.dropLootedItemsOnDeath());
    }

    @Test
    void parsesMovementDefaultsAndButterflyFlight() {
        RagnarMovementConfig defaults = MobDefinitionParser.parseMovement(id("ragnarmmo:poring"), json("{}"));
        assertEquals(RagnarMovementProfile.GROUND_CRAWL, defaults.profile());
        assertEquals(RagnarMovementSpeedClass.SLOW_400, defaults.speedClass());

        RagnarMovementConfig flight = MobDefinitionParser.parseMovement(id("ragnarmmo:creamy"), json("""
                {"movement":{"profile":"BUTTERFLY_FLIGHT","speedClass":"VERY_FAST_150","hoverMin":0.5,"hoverMax":2.5,"wanderRadius":6,"leashRadius":12}}
                """));
        assertEquals(RagnarMovementProfile.BUTTERFLY_FLIGHT, flight.profile());
        assertEquals(RagnarMovementSpeedClass.VERY_FAST_150, flight.speedClass());
        assertEquals(0.5D, flight.hoverMin());
        assertEquals(2.5D, flight.hoverMax());
        assertEquals(6, flight.wanderRadius());
        assertEquals(12, flight.leashRadius());
    }

    @Test
    void parsesSpawnDefinitionAndRejectsMissingNaturalSpawnPath() {
        RagnarSpawnDefinition spawn = MobDefinitionParser.parseSpawn(id("ragnarmmo:creamy_fear"), json("""
                {"spawn":{"naturalSpawn":false,"structureTags":["ragnarmmo:guild_dungeons"],"weight":8,"minGroup":1,"maxGroup":1,"spawnReason":["STRUCTURE","EVENT"],"manualOnly":false}}
                """));
        assertNotNull(spawn);
        assertFalse(spawn.naturalSpawn());
        assertEquals(1, spawn.minGroup());
        assertEquals(Set.of(RagnarSpawnReason.STRUCTURE, RagnarSpawnReason.EVENT), spawn.spawnReasons());

        RagnarSpawnDefinition pupaSpawn = MobDefinitionParser.parseSpawn(id("ragnarmmo:pupa"), json("""
                {"spawn":{"naturalSpawn":true,"biomeTags":["ragnarmmo:forests"],"weight":14,"minGroup":1,"maxGroup":2,"nearBlocks":{"mode":"PREFER","radius":5,"values":["minecraft:oak_log","minecraft:birch_log"]}}}
                """));
        assertNotNull(pupaSpawn.nearBlocks());
        assertEquals(RagnarBlockProximityMode.PREFER, pupaSpawn.nearBlocks().mode());
        assertEquals(5, pupaSpawn.nearBlocks().radius());

        assertThrows(IllegalArgumentException.class, () -> MobDefinitionParser.parseSpawn(
                id("ragnarmmo:creamy_fear"),
                json("""
                        {"spawn":{"naturalSpawn":false,"weight":8,"minGroup":1,"maxGroup":1}}
                        """)));
    }

    @Test
    void rejectsCrossBlockContradictions() {
        assertThrows(IllegalArgumentException.class, () -> MobDefinitionParser.parseDefinition(
                id("ragnarmmo:creamy"),
                json("""
                        {
                          "entity":"ragnarmmo:creamy",
                          "ai":{"aggroType":"AGGRESSIVE","canAttack":false,"canMove":true,"immobile":false,"retaliates":false},
                          "movement":{"profile":"BUTTERFLY_FLIGHT","speedClass":"VERY_FAST_150","hoverMin":0.5,"hoverMax":2.5,"wanderRadius":6,"leashRadius":12}
                        }
                        """)));

        assertThrows(IllegalArgumentException.class, () -> MobDefinitionParser.parseDefinition(
                id("ragnarmmo:pupa"),
                json("""
                        {
                          "entity":"ragnarmmo:pupa",
                          "ai":{"aggroType":"PASSIVE","canMove":true,"canAttack":false,"immobile":true,"retaliates":false},
                          "movement":{"profile":"STATIONARY","speedClass":"IMMOBILE"}
                        }
                        """)));
    }

    @Test
    void parsesMetamorphosisAndValidatesTarget() {
        RagnarMetamorphosis metamorphosis = MobDefinitionParser.parseMetamorphosis(
                id("ragnarmmo:fabre"),
                json("""
                        {"metamorphosis":{"target":"minecraft:zombie","chancePerSecond":0.5}}
                        """),
                fakeValidator("minecraft:zombie"));
        assertNotNull(metamorphosis);
        assertEquals(id("minecraft:zombie"), metamorphosis.target());
        assertEquals(0.5D, metamorphosis.chancePerSecond());
    }

    @Test
    void rejectsMetamorphosisTargetingSelf() {
        assertThrows(IllegalArgumentException.class, () -> MobDefinitionParser.parseMetamorphosis(
                id("ragnarmmo:fabre"),
                json("""
                        {"entity":"ragnarmmo:fabre","metamorphosis":{"target":"ragnarmmo:fabre","chancePerSecond":0.5}}
                        """),
                fakeValidator("ragnarmmo:fabre")));
    }

    @Test
    void rejectsInvalidMetamorphosisChanceAndTarget() {
        assertThrows(IllegalArgumentException.class, () -> MobDefinitionParser.parseMetamorphosis(
                id("ragnarmmo:fabre"),
                json("""
                        {"metamorphosis":{"target":"minecraft:zombie","chancePerSecond":1.5}}
                        """),
                fakeValidator("minecraft:zombie")));
        assertThrows(IllegalArgumentException.class, () -> MobDefinitionParser.parseMetamorphosis(
                id("ragnarmmo:fabre"),
                json("""
                        {"metamorphosis":{"target":"ragnarmmo:not_real","chancePerSecond":0.5}}
                        """),
                fakeValidator("minecraft:zombie")));
    }

    @Test
    void parsesCreamyFearFutureFlagsWithoutChangingBehaviorYet() {
        RagnarAiFlags flags = MobDefinitionParser.parseAiFlags(id("ragnarmmo:creamy_fear"), json("""
                {"ai":{"aggroType":"AGGRESSIVE","detector":true,"changeTargetOnAttack":true,"changeTargetOnChase":true,"castSensorIdle":true,"castSensorChase":true}}
                """));

        assertEquals(RagnarAggroType.AGGRESSIVE, flags.aggroType());
        assertTrue(flags.detector());
        assertTrue(flags.changeTargetOnAttack());
        assertTrue(flags.changeTargetOnChase());
        assertTrue(flags.castSensorIdle());
        assertTrue(flags.castSensorChase());
        assertFalse(flags.looter());
    }

    @Test
    void creamyDefinitionIsPassiveAndMobile() throws IOException {
        JsonObject definition = json(Files.readString(Path.of("src/main/resources/data/ragnarmmo/mob_definitions/creamy.json")));
        RagnarAiFlags flags = MobDefinitionParser.parseAiFlags(id("ragnarmmo:creamy"), definition);
        RagnarMovementConfig movement = MobDefinitionParser.parseMovement(id("ragnarmmo:creamy"), definition);

        assertEquals(RagnarAggroType.PASSIVE, flags.aggroType());
        assertTrue(flags.canMove());
        assertTrue(flags.canAttack());
        assertTrue(flags.retaliates());
        assertEquals(RagnarMovementProfile.BUTTERFLY_FLIGHT, movement.profile());
    }

    private static JsonObject json(String raw) {
        return JsonParser.parseString(raw).getAsJsonObject();
    }

    private static ResourceLocation id(String raw) {
        ResourceLocation parsed = ResourceLocation.tryParse(raw);
        assertNotNull(parsed, raw);
        return parsed;
    }

    private static MetamorphosisTargetValidator fakeValidator(String existingId) {
        ResourceLocation existing = id(existingId);
        return new MetamorphosisTargetValidator() {
            @Override
            public boolean exists(ResourceLocation target) {
                return existing.equals(target);
            }

            @Override
            public boolean isCompatible(ResourceLocation target) {
                return exists(target);
            }
        };
    }
}
