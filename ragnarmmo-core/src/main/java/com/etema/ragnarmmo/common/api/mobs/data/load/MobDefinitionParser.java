package com.etema.ragnarmmo.common.api.mobs.data.load;

import com.etema.ragnarmmo.common.api.mobs.MobRank;
import com.etema.ragnarmmo.common.api.mobs.data.MobDefinition;
import com.etema.ragnarmmo.common.api.mobs.data.MobDirectStatsBlock;
import com.etema.ragnarmmo.common.api.mobs.data.MobRoStatsBlock;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarAggroType;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarAiFlags;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarBlockProximityMode;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarBlockProximityRule;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementConfig;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementProfile;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementSpeedClass;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarLootBehavior;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMetamorphosis;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarSpawnDefinition;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarSpawnReason;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class MobDefinitionParser {

    static final Set<String> DEFINITION_FIELDS = Set.of(
            "entity",
            "template",
            "rank",
            "level",
            "base_exp",
            "job_exp",
            "ro_stats",
            "direct_stats",
            "race",
            "element",
            "element_level",
            "size",
            "ai",
            "movement",
            "loot_behavior",
            "metamorphosis",
            "spawn");

    private static final Set<String> AI_FIELDS = Set.of(
            "passive",
            "aggroType",
            "looter",
            "canMove",
            "canAttack",
            "immobile",
            "retaliates",
            "detector",
            "changeTargetOnAttack",
            "changeTargetOnChase",
            "castSensorIdle",
            "castSensorChase");
    private static final Set<String> MOVEMENT_FIELDS = Set.of(
            "profile",
            "speedClass",
            "hoverMin",
            "hoverMax",
            "wanderRadius",
            "leashRadius");
    private static final Set<String> LOOT_FIELDS = Set.of("pickupRadius", "dropLootedItemsOnDeath");
    private static final Set<String> METAMORPHOSIS_FIELDS = Set.of("target", "chancePerSecond");
    private static final Set<String> SPAWN_FIELDS = Set.of(
            "naturalSpawn",
            "biomeTags",
            "structureTags",
            "weight",
            "minGroup",
            "maxGroup",
            "minY",
            "maxY",
            "lightMin",
            "lightMax",
            "surfaceOnly",
            "requiresSky",
            "temperatureMax",
            "nearBlocks",
            "spawnReason",
            "manualOnly");
    private static final Set<String> NEAR_BLOCKS_FIELDS = Set.of("mode", "radius", "values");
    private static final Set<String> RO_STATS_FIELDS = Set.of("str", "agi", "vit", "int", "dex", "luk");
    private static final Set<String> DIRECT_STATS_FIELDS = Set.of(
            "max_hp", "atk_min", "atk_max", "matk_min", "matk_max", "def", "mdef", "hit", "flee", "crit", "aspd", "move_speed");

    private MobDefinitionParser() {
    }

    public static MobDefinition parseDefinition(ResourceLocation sourceId, JsonObject json) {
        validateAllowedFields(sourceId, json, DEFINITION_FIELDS, "definition");

        ResourceLocation entity = parseRequiredResourceLocation(sourceId, json, "entity");
        ResourceLocation template = parseOptionalResourceLocation(sourceId, json, "template");
        MobDefinition definition = new MobDefinition(
                entity,
                template,
                parseOptionalRank(sourceId, json, "rank"),
                parseOptionalInteger(sourceId, json, "level"),
                parseOptionalInteger(sourceId, json, "base_exp"),
                parseOptionalInteger(sourceId, json, "job_exp"),
                parseRoStatsBlock(sourceId, json),
                parseDirectStatsBlock(sourceId, json),
                parseOptionalString(sourceId, json, "race"),
                parseOptionalString(sourceId, json, "element"),
                parseOptionalInteger(sourceId, json, "element_level"),
                parseOptionalString(sourceId, json, "size"),
                parseAiFlags(sourceId, json),
                parseMovement(sourceId, json),
                parseLootBehavior(sourceId, json),
                parseMetamorphosis(sourceId, json),
                parseSpawn(sourceId, json));
        return validateCrossBlockConstraints(sourceId, definition);
    }

    public static @Nullable RagnarAiFlags parseAiFlags(ResourceLocation sourceId, JsonObject json) {
        if (!json.has("ai")) {
            return new RagnarAiFlags(
                    RagnarAggroType.PASSIVE,
                    false,
                    true,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false);
        }
        JsonElement element = json.get("ai");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("ai must be a JSON object in " + sourceId);
        }

        JsonObject block = element.getAsJsonObject();
        validateAllowedFields(sourceId, block, AI_FIELDS, "ai");

        boolean hasPassive = block.has("passive");
        boolean hasAggroType = block.has("aggroType");
        if (hasPassive && hasAggroType) {
            throw new IllegalArgumentException("passive and aggroType cannot both be present in " + sourceId);
        }

        RagnarAggroType aggroType;
        if (hasAggroType) {
            aggroType = parseAggroType(sourceId, block);
        } else if (hasPassive) {
            aggroType = parseBoolean(sourceId, block, "passive")
                    ? RagnarAggroType.PASSIVE
                    : RagnarAggroType.AGGRESSIVE;
        } else {
            aggroType = RagnarAggroType.PASSIVE;
        }

        boolean looter = parseOptionalBoolean(sourceId, block, "looter", false);
        boolean canMove = parseOptionalBoolean(sourceId, block, "canMove", true);
        boolean canAttack = parseOptionalBoolean(sourceId, block, "canAttack", true);
        boolean immobile = parseOptionalBoolean(sourceId, block, "immobile", false);
        boolean retaliates = parseOptionalBoolean(sourceId, block, "retaliates", false);
        boolean detector = parseOptionalBoolean(sourceId, block, "detector", false);
        boolean changeTargetOnAttack = parseOptionalBoolean(sourceId, block, "changeTargetOnAttack", false);
        boolean changeTargetOnChase = parseOptionalBoolean(sourceId, block, "changeTargetOnChase", false);
        boolean castSensorIdle = parseOptionalBoolean(sourceId, block, "castSensorIdle", false);
        boolean castSensorChase = parseOptionalBoolean(sourceId, block, "castSensorChase", false);

        return new RagnarAiFlags(
                aggroType,
                looter,
                canMove,
                canAttack,
                immobile,
                retaliates,
                detector,
                changeTargetOnAttack,
                changeTargetOnChase,
                castSensorIdle,
                castSensorChase);
    }

    public static @Nullable RagnarMovementConfig parseMovement(ResourceLocation sourceId, JsonObject json) {
        if (!json.has("movement")) {
            return RagnarMovementConfig.defaults();
        }
        JsonElement element = json.get("movement");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("movement must be a JSON object in " + sourceId);
        }

        JsonObject block = element.getAsJsonObject();
        validateAllowedFields(sourceId, block, MOVEMENT_FIELDS, "movement");
        RagnarMovementProfile profile = parseMovementProfile(sourceId, block);
        RagnarMovementSpeedClass speedClass = parseMovementSpeedClass(sourceId, block, profile);
        Double hoverMinValue = parseOptionalDouble(sourceId, block, "hoverMin");
        Double hoverMaxValue = parseOptionalDouble(sourceId, block, "hoverMax");
        Integer wanderRadiusValue = parseOptionalInteger(sourceId, block, "wanderRadius");
        Integer leashRadiusValue = parseOptionalInteger(sourceId, block, "leashRadius");
        double hoverMin = hoverMinValue != null ? hoverMinValue : defaultHoverMin(profile);
        double hoverMax = hoverMaxValue != null ? hoverMaxValue : defaultHoverMax(profile);
        int wanderRadius = wanderRadiusValue != null ? wanderRadiusValue : defaultWanderRadius(profile);
        int leashRadius = leashRadiusValue != null ? leashRadiusValue : defaultLeashRadius(profile);
        return new RagnarMovementConfig(profile, speedClass, hoverMin, hoverMax, wanderRadius, leashRadius);
    }

    public static @Nullable RagnarLootBehavior parseLootBehavior(ResourceLocation sourceId, JsonObject json) {
        if (!json.has("loot_behavior")) {
            RagnarAiFlags ai = parseAiFlags(sourceId, json);
            if (ai != null && ai.looter()) {
                return RagnarLootBehavior.DEFAULT;
            }
            return null;
        }
        JsonElement element = json.get("loot_behavior");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("loot_behavior must be a JSON object in " + sourceId);
        }

        JsonObject block = element.getAsJsonObject();
        validateAllowedFields(sourceId, block, LOOT_FIELDS, "loot_behavior");
        Double pickupRadiusValue = parseOptionalDouble(sourceId, block, "pickupRadius");
        double pickupRadius = pickupRadiusValue == null ? 6.0D : pickupRadiusValue;
        boolean dropLootedItemsOnDeath = parseOptionalBoolean(sourceId, block, "dropLootedItemsOnDeath", true);
        return new RagnarLootBehavior(pickupRadius, dropLootedItemsOnDeath);
    }

    public static @Nullable RagnarMetamorphosis parseMetamorphosis(ResourceLocation sourceId, JsonObject json) {
        return parseMetamorphosis(sourceId, json, RuntimeMetamorphosisTargetValidator.INSTANCE);
    }

    public static @Nullable RagnarMetamorphosis parseMetamorphosis(
            ResourceLocation sourceId,
            JsonObject json,
            MetamorphosisTargetValidator validator) {
        if (!json.has("metamorphosis")) {
            return null;
        }
        JsonElement element = json.get("metamorphosis");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("metamorphosis must be a JSON object in " + sourceId);
        }

        JsonObject block = element.getAsJsonObject();
        validateAllowedFields(sourceId, block, METAMORPHOSIS_FIELDS, "metamorphosis");
        ResourceLocation target = parseRequiredResourceLocation(sourceId, block, "target");
        ResourceLocation entity = parseOptionalResourceLocation(sourceId, json, "entity");
        if (entity != null && entity.equals(target)) {
            throw new IllegalArgumentException("metamorphosis.target cannot point to the same entity in " + sourceId);
        }
        if (validator != null && !validator.exists(target)) {
            throw new IllegalArgumentException("metamorphosis.target does not exist in entity registry: " + target + " in " + sourceId);
        }
        if (validator != null && !validator.isCompatible(target)) {
            throw new IllegalArgumentException("metamorphosis.target must point to a compatible mob entity in " + sourceId + ": " + target);
        }
        double chancePerSecond = parseRequiredDouble(sourceId, block, "chancePerSecond");
        return new RagnarMetamorphosis(target, chancePerSecond);
    }

    public static @Nullable RagnarSpawnDefinition parseSpawn(ResourceLocation sourceId, JsonObject json) {
        if (!json.has("spawn")) {
            return null;
        }
        JsonElement element = json.get("spawn");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("spawn must be a JSON object in " + sourceId);
        }

        JsonObject block = element.getAsJsonObject();
        validateAllowedFields(sourceId, block, SPAWN_FIELDS, "spawn");

        boolean naturalSpawn = parseOptionalBoolean(sourceId, block, "naturalSpawn", true);
        List<ResourceLocation> biomeTags = parseResourceLocationList(sourceId, block, "biomeTags");
        List<ResourceLocation> structureTags = parseResourceLocationList(sourceId, block, "structureTags");
        Integer weightValue = parseOptionalInteger(sourceId, block, "weight");
        Integer minGroupValue = parseOptionalInteger(sourceId, block, "minGroup");
        Integer maxGroupValue = parseOptionalInteger(sourceId, block, "maxGroup");
        int weight = weightValue != null ? weightValue : 1;
        int minGroup = minGroupValue != null ? minGroupValue : 1;
        int maxGroup = maxGroupValue != null ? maxGroupValue : minGroup;
        Integer minY = parseOptionalInteger(sourceId, block, "minY");
        Integer maxY = parseOptionalInteger(sourceId, block, "maxY");
        Integer lightMin = parseOptionalInteger(sourceId, block, "lightMin");
        Integer lightMax = parseOptionalInteger(sourceId, block, "lightMax");
        boolean surfaceOnly = parseOptionalBoolean(sourceId, block, "surfaceOnly", false);
        boolean requiresSky = parseOptionalBoolean(sourceId, block, "requiresSky", false);
        Double temperatureMax = parseOptionalDouble(sourceId, block, "temperatureMax");
        RagnarBlockProximityRule nearBlocks = parseNearBlocks(sourceId, block);
        Set<RagnarSpawnReason> spawnReasons = parseSpawnReasons(sourceId, block);
        boolean manualOnly = parseOptionalBoolean(sourceId, block, "manualOnly", false);
        return new RagnarSpawnDefinition(
                naturalSpawn,
                biomeTags,
                structureTags,
                weight,
                minGroup,
                maxGroup,
                minY,
                maxY,
                lightMin,
                lightMax,
                surfaceOnly,
                requiresSky,
                temperatureMax,
                nearBlocks,
                spawnReasons,
                manualOnly);
    }

    public interface MetamorphosisTargetValidator {
        boolean exists(ResourceLocation target);

        boolean isCompatible(ResourceLocation target);
    }

    private enum RuntimeMetamorphosisTargetValidator implements MetamorphosisTargetValidator {
        INSTANCE;

        @Override
        public boolean exists(ResourceLocation target) {
            return ForgeRegistries.ENTITY_TYPES.containsKey(target);
        }

        @Override
        public boolean isCompatible(ResourceLocation target) {
            var type = ForgeRegistries.ENTITY_TYPES.getValue(target);
            return type != null && type.getCategory() != MobCategory.MISC;
        }
    }

    private static @Nullable MobRoStatsBlock parseRoStatsBlock(ResourceLocation sourceId, JsonObject json) {
        if (!json.has("ro_stats")) {
            return null;
        }
        JsonElement element = json.get("ro_stats");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("ro_stats must be a JSON object in " + sourceId);
        }

        JsonObject block = element.getAsJsonObject();
        validateAllowedFields(sourceId, block, RO_STATS_FIELDS, "ro_stats");
        return new MobRoStatsBlock(
                parseOptionalInteger(sourceId, block, "str"),
                parseOptionalInteger(sourceId, block, "agi"),
                parseOptionalInteger(sourceId, block, "vit"),
                parseOptionalInteger(sourceId, block, "int"),
                parseOptionalInteger(sourceId, block, "dex"),
                parseOptionalInteger(sourceId, block, "luk"));
    }

    private static @Nullable MobDirectStatsBlock parseDirectStatsBlock(ResourceLocation sourceId, JsonObject json) {
        if (!json.has("direct_stats")) {
            return null;
        }
        JsonElement element = json.get("direct_stats");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("direct_stats must be a JSON object in " + sourceId);
        }

        JsonObject block = element.getAsJsonObject();
        validateAllowedFields(sourceId, block, DIRECT_STATS_FIELDS, "direct_stats");
        return new MobDirectStatsBlock(
                parseOptionalInteger(sourceId, block, "max_hp"),
                parseOptionalInteger(sourceId, block, "atk_min"),
                parseOptionalInteger(sourceId, block, "atk_max"),
                parseOptionalInteger(sourceId, block, "matk_min"),
                parseOptionalInteger(sourceId, block, "matk_max"),
                parseOptionalInteger(sourceId, block, "def"),
                parseOptionalInteger(sourceId, block, "mdef"),
                parseOptionalInteger(sourceId, block, "hit"),
                parseOptionalInteger(sourceId, block, "flee"),
                parseOptionalInteger(sourceId, block, "crit"),
                parseOptionalInteger(sourceId, block, "aspd"),
                parseOptionalDouble(sourceId, block, "move_speed"));
    }

    private static RagnarMovementProfile parseMovementProfile(ResourceLocation sourceId, JsonObject json) {
        String raw = parseOptionalString(sourceId, json, "profile");
        if (raw == null) {
            return RagnarMovementProfile.GROUND_CRAWL;
        }
        try {
            return RagnarMovementProfile.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("movement.profile must be one of SLIME_HOP, GROUND_CRAWL, RABBIT_HOP, STATIONARY, BUTTERFLY_FLIGHT in " + sourceId);
        }
    }

    private static RagnarMovementSpeedClass parseMovementSpeedClass(
            ResourceLocation sourceId,
            JsonObject json,
            RagnarMovementProfile profile) {
        String raw = parseOptionalString(sourceId, json, "speedClass");
        if (raw == null) {
            return defaultSpeedClass(profile);
        }
        try {
            RagnarMovementSpeedClass speedClass = RagnarMovementSpeedClass.valueOf(raw);
            if (!speedClassCompatible(profile, speedClass)) {
                throw new IllegalArgumentException("movement.speedClass is incompatible with movement.profile in " + sourceId);
            }
            return speedClass;
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("incompatible")) {
                throw ex;
            }
            throw new IllegalArgumentException("movement.speedClass must be one of SLOW_400, MEDIUM_300, FAST_200, VERY_FAST_150, VERY_FAST_155, IMMOBILE in " + sourceId);
        }
    }

    private static boolean speedClassCompatible(RagnarMovementProfile profile, RagnarMovementSpeedClass speedClass) {
        return switch (profile) {
            case STATIONARY -> speedClass == RagnarMovementSpeedClass.IMMOBILE;
            case BUTTERFLY_FLIGHT -> speedClass == RagnarMovementSpeedClass.VERY_FAST_150
                    || speedClass == RagnarMovementSpeedClass.VERY_FAST_155;
            case RABBIT_HOP -> speedClass == RagnarMovementSpeedClass.MEDIUM_300
                    || speedClass == RagnarMovementSpeedClass.FAST_200
                    || speedClass == RagnarMovementSpeedClass.VERY_FAST_150
                    || speedClass == RagnarMovementSpeedClass.SLOW_400;
            case SLIME_HOP, GROUND_CRAWL -> !speedClass.isImmobile();
        };
    }

    private static double defaultHoverMin(RagnarMovementProfile profile) {
        return profile == RagnarMovementProfile.BUTTERFLY_FLIGHT ? 0.5D : 0.0D;
    }

    private static double defaultHoverMax(RagnarMovementProfile profile) {
        return profile == RagnarMovementProfile.BUTTERFLY_FLIGHT ? 2.5D : 0.0D;
    }

    private static int defaultWanderRadius(RagnarMovementProfile profile) {
        return profile == RagnarMovementProfile.STATIONARY ? 0 : 8;
    }

    private static int defaultLeashRadius(RagnarMovementProfile profile) {
        return profile == RagnarMovementProfile.STATIONARY ? 0 : 16;
    }

    private static RagnarMovementSpeedClass defaultSpeedClass(RagnarMovementProfile profile) {
        return switch (profile) {
            case STATIONARY -> RagnarMovementSpeedClass.IMMOBILE;
            case BUTTERFLY_FLIGHT -> RagnarMovementSpeedClass.VERY_FAST_150;
            case RABBIT_HOP -> RagnarMovementSpeedClass.FAST_200;
            case SLIME_HOP, GROUND_CRAWL -> RagnarMovementSpeedClass.SLOW_400;
        };
    }

    private static RagnarBlockProximityRule parseNearBlocks(ResourceLocation sourceId, JsonObject json) {
        if (!json.has("nearBlocks")) {
            return null;
        }
        JsonElement element = json.get("nearBlocks");
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("nearBlocks must be a JSON object in " + sourceId);
        }
        JsonObject block = element.getAsJsonObject();
        validateAllowedFields(sourceId, block, NEAR_BLOCKS_FIELDS, "nearBlocks");
        String rawMode = parseOptionalString(sourceId, block, "mode");
        RagnarBlockProximityMode mode = rawMode == null
                ? RagnarBlockProximityMode.PREFER
                : parseEnum(sourceId, rawMode, RagnarBlockProximityMode.class, "nearBlocks.mode");
        int radius = parseOptionalInteger(sourceId, block, "radius") != null ? parseOptionalInteger(sourceId, block, "radius") : 6;
        List<ResourceLocation> values = parseResourceLocationList(sourceId, block, "values");
        if (mode != RagnarBlockProximityMode.DISABLED && values.isEmpty()) {
            throw new IllegalArgumentException("nearBlocks.values must not be empty when nearBlocks.mode is enabled in " + sourceId);
        }
        return new RagnarBlockProximityRule(mode, radius, values);
    }

    private static Set<RagnarSpawnReason> parseSpawnReasons(ResourceLocation sourceId, JsonObject json) {
        if (!json.has("spawnReason")) {
            return Set.of();
        }
        JsonElement element = json.get("spawnReason");
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("spawnReason must be a JSON array in " + sourceId);
        }
        EnumSet<RagnarSpawnReason> reasons = EnumSet.noneOf(RagnarSpawnReason.class);
        for (JsonElement child : element.getAsJsonArray()) {
            if (!child.isJsonPrimitive() || !child.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("spawnReason values must be strings in " + sourceId);
            }
            reasons.add(parseEnum(sourceId, child.getAsString(), RagnarSpawnReason.class, "spawnReason"));
        }
        return reasons;
    }

    private static List<ResourceLocation> parseResourceLocationList(ResourceLocation sourceId, JsonObject json, String field) {
        if (!json.has(field)) {
            return List.of();
        }
        JsonElement element = json.get(field);
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException(field + " must be a JSON array in " + sourceId);
        }
        List<ResourceLocation> values = new ArrayList<>();
        JsonArray array = element.getAsJsonArray();
        for (JsonElement child : array) {
            if (!child.isJsonPrimitive() || !child.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException(field + " values must be strings in " + sourceId);
            }
            ResourceLocation parsed = ResourceLocation.tryParse(child.getAsString());
            if (parsed == null) {
                throw new IllegalArgumentException(field + " contains an invalid resource location in " + sourceId);
            }
            values.add(parsed);
        }
        return values;
    }

    private static <E extends Enum<E>> E parseEnum(ResourceLocation sourceId, String raw, Class<E> type, String field) {
        try {
            return Enum.valueOf(type, raw);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(field + " must be one of " + java.util.Arrays.toString(type.getEnumConstants()) + " in " + sourceId);
        }
    }

    private static boolean parseOptionalBoolean(
            ResourceLocation sourceId,
            JsonObject json,
            String field,
            boolean defaultValue) {
        if (!json.has(field)) {
            return defaultValue;
        }
        return parseBoolean(sourceId, json, field);
    }

    public static MobDefinition validateCrossBlockConstraints(ResourceLocation sourceId, MobDefinition definition) {
        RagnarAiFlags ai = definition.ai();
        RagnarMovementConfig movement = definition.movement();
        RagnarLootBehavior loot = definition.lootBehavior();
        RagnarSpawnDefinition spawn = definition.spawn();

        if (ai != null && movement != null) {
            boolean stationary = movement.profile() == RagnarMovementProfile.STATIONARY;
            if (stationary != (ai.immobile() && !ai.canMove())) {
                throw new IllegalArgumentException("movement.profile and ai.canMove/immobile contradict each other in " + sourceId);
            }
            if (!stationary && (!ai.canMove() || ai.immobile())) {
                throw new IllegalArgumentException("movement.profile requires ai.canMove=true and ai.immobile=false in " + sourceId);
            }
            if (!ai.canAttack() && ai.aggroType() == RagnarAggroType.AGGRESSIVE) {
                throw new IllegalArgumentException("canAttack=false cannot coexist with AGGRESSIVE aggroType in " + sourceId);
            }
            if (!ai.canAttack() && ai.retaliates()) {
                throw new IllegalArgumentException("canAttack=false cannot coexist with retaliates=true in " + sourceId);
            }
            if (ai.looter() && (!ai.canMove() || ai.immobile())) {
                throw new IllegalArgumentException("looter=true requires canMove=true and immobile=false in " + sourceId);
            }
        }
        if (movement != null && movement.profile() == RagnarMovementProfile.STATIONARY && (movement.speedClass() != RagnarMovementSpeedClass.IMMOBILE)) {
            throw new IllegalArgumentException("STATIONARY movement requires IMMOBILE speedClass in " + sourceId);
        }
        if (spawn != null && !spawn.naturalSpawn() && spawn.spawnReasons().isEmpty() && !spawn.manualOnly() && spawn.structureTags().isEmpty()) {
            throw new IllegalArgumentException("spawn.naturalSpawn=false requires a non-natural spawn path in " + sourceId);
        }
        if (loot != null && loot.pickupRadius() < 0.0D) {
            throw new IllegalArgumentException("loot_behavior.pickupRadius must be >= 0 in " + sourceId);
        }
        return definition;
    }

    private static RagnarAggroType parseAggroType(ResourceLocation sourceId, JsonObject json) {
        String raw = parseRequiredString(sourceId, json, "aggroType");
        try {
            return RagnarAggroType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("aggroType must be PASSIVE or AGGRESSIVE in " + sourceId);
        }
    }

    private static void validateAllowedFields(
            ResourceLocation sourceId,
            JsonObject json,
            Set<String> allowedFields,
            String context) {
        for (String field : json.keySet()) {
            if (!allowedFields.contains(field)) {
                throw new IllegalArgumentException(
                        "unknown field '" + field + "' in " + context + " for " + sourceId);
            }
        }
    }

    private static @Nullable String parseOptionalString(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        if (!json.has(field)) {
            return null;
        }
        JsonElement element = json.get(field);
        if (element.isJsonNull() || !element.isJsonPrimitive()) {
            throw new IllegalArgumentException(field + " must be a string in " + sourceId);
        }
        return element.getAsString();
    }

    private static String parseRequiredString(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        String value = parseOptionalString(sourceId, json, field);
        if (value == null) {
            throw new IllegalArgumentException(field + " is required in " + sourceId);
        }
        return value;
    }

    private static @Nullable Integer parseOptionalInteger(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        if (!json.has(field)) {
            return null;
        }
        JsonElement element = json.get(field);
        if (element.isJsonNull() || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(field + " must be a number in " + sourceId);
        }
        return element.getAsInt();
    }

    private static @Nullable Double parseOptionalDouble(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        if (!json.has(field)) {
            return null;
        }
        JsonElement element = json.get(field);
        if (element.isJsonNull() || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(field + " must be a number in " + sourceId);
        }
        return element.getAsDouble();
    }

    private static boolean parseBoolean(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        if (!json.has(field)) {
            throw new IllegalArgumentException(field + " is required in " + sourceId);
        }
        JsonElement element = json.get(field);
        if (element.isJsonNull() || !element.isJsonPrimitive()) {
            throw new IllegalArgumentException(field + " must be a boolean in " + sourceId);
        }
        return element.getAsBoolean();
    }

    private static double parseRequiredDouble(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        Double value = parseOptionalDouble(sourceId, json, field);
        if (value == null) {
            throw new IllegalArgumentException(field + " is required in " + sourceId);
        }
        if (value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(field + " must be between 0 and 1 in " + sourceId);
        }
        return value;
    }

    private static @Nullable ResourceLocation parseOptionalResourceLocation(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        String raw = parseOptionalString(sourceId, json, field);
        if (raw == null) {
            return null;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(raw);
        if (parsed == null) {
            throw new IllegalArgumentException(field + " must be a valid resource location in " + sourceId);
        }
        return parsed;
    }

    private static ResourceLocation parseRequiredResourceLocation(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        ResourceLocation parsed = parseOptionalResourceLocation(sourceId, json, field);
        if (parsed == null) {
            throw new IllegalArgumentException(field + " is required in " + sourceId);
        }
        return parsed;
    }

    private static @Nullable MobRank parseOptionalRank(
            ResourceLocation sourceId,
            JsonObject json,
            String field) {
        String raw = parseOptionalString(sourceId, json, field);
        if (raw == null) {
            return null;
        }
        try {
            return MobRank.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(field + " must be one of NORMAL, ELITE, BOSS in " + sourceId);
        }
    }

}
