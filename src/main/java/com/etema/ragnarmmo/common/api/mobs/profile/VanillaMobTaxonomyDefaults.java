package com.etema.ragnarmmo.common.api.mobs.profile;

import com.etema.ragnarmmo.common.api.mobs.MobRank;
import com.etema.ragnarmmo.common.api.mobs.data.ResolvedMobDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public final class VanillaMobTaxonomyDefaults {
    private static final Map<ResourceLocation, Taxonomy> DEFAULTS = Map.ofEntries(
            entry("allay", "angel", "holy", 1, 0),
            entry("axolotl", "fish", "water", 1, 0),
            entry("bat", "brute", "wind", 1, 0),
            entry("bee", "insect", "poison", 1),
            entry("blaze", "formless", "fire", 3),
            entry("camel", "brute", "neutral", 1, 0),
            entry("cat", "brute", "neutral", 1, 0),
            entry("cave_spider", "insect", "poison", 2),
            entry("chicken", "brute", "neutral", 1, 0),
            entry("cod", "fish", "water", 1, 0),
            entry("cow", "brute", "neutral", 1, 0),
            entry("creeper", "formless", "neutral", 3, 4),
            entry("dolphin", "fish", "water", 2, 0),
            entry("donkey", "brute", "neutral", 1, 0),
            entry("drowned", "undead", "undead", 2),
            entry("elder_guardian", "fish", "water", 4, 15),
            entry("ender_dragon", "dragon", "shadow", 4, 64, MobRank.BOSS),
            entry("enderman", "demon", "shadow", 3),
            entry("endermite", "insect", "shadow", 1),
            entry("evoker", "demihuman", "shadow", 2, 8),
            entry("fox", "brute", "neutral", 1, 0),
            entry("frog", "brute", "water", 1, 0),
            entry("ghast", "demon", "ghost", 3, 32),
            entry("glow_squid", "fish", "water", 2, 0),
            entry("goat", "brute", "earth", 1, 0),
            entry("guardian", "fish", "water", 3, 15),
            entry("hoglin", "brute", "fire", 1),
            entry("horse", "brute", "neutral", 1, 0),
            entry("husk", "undead", "undead", 2),
            entry("iron_golem", "formless", "earth", 2, 4),
            entry("llama", "brute", "neutral", 1, 0),
            entry("magma_cube", "formless", "fire", 3),
            entry("mooshroom", "brute", "earth", 1, 0),
            entry("mule", "brute", "neutral", 1, 0),
            entry("ocelot", "brute", "neutral", 1, 0),
            entry("panda", "brute", "neutral", 1, 0),
            entry("parrot", "brute", "wind", 1, 0),
            entry("phantom", "undead", "undead", 2),
            entry("pig", "brute", "neutral", 1, 0),
            entry("piglin", "demihuman", "fire", 1),
            entry("piglin_brute", "demihuman", "fire", 2),
            entry("pillager", "demihuman", "neutral", 1, 12),
            entry("polar_bear", "brute", "water", 1),
            entry("pufferfish", "fish", "poison", 1, 4),
            entry("rabbit", "brute", "neutral", 1, 0),
            entry("ravager", "brute", "earth", 2),
            entry("salmon", "fish", "water", 1, 0),
            entry("sheep", "brute", "neutral", 1, 0),
            entry("shulker", "formless", "ghost", 2, 12),
            entry("silverfish", "insect", "earth", 1),
            entry("skeleton", "undead", "undead", 2, 12),
            entry("skeleton_horse", "undead", "undead", 1, 0),
            entry("slime", "formless", "water", 1),
            entry("sniffer", "brute", "earth", 1, 0),
            entry("snow_golem", "formless", "water", 2, 12),
            entry("spider", "insect", "poison", 1),
            entry("squid", "fish", "water", 1, 0),
            entry("stray", "undead", "undead", 2, 12),
            entry("strider", "brute", "fire", 2),
            entry("tadpole", "fish", "water", 1, 0),
            entry("trader_llama", "brute", "neutral", 1, 0),
            entry("tropical_fish", "fish", "water", 1, 0),
            entry("turtle", "fish", "water", 2, 0),
            entry("vex", "demon", "ghost", 3),
            entry("villager", "demihuman", "neutral", 1, 0),
            entry("vindicator", "demihuman", "neutral", 1),
            entry("wandering_trader", "demihuman", "neutral", 1, 0),
            entry("warden", "demon", "shadow", 4, 4, MobRank.BOSS),
            entry("witch", "demihuman", "poison", 2, 8),
            entry("wither", "undead", "undead", 4, 32, MobRank.BOSS),
            entry("wither_skeleton", "undead", "undead", 3),
            entry("wolf", "brute", "neutral", 1),
            entry("zoglin", "undead", "undead", 2),
            entry("zombie", "undead", "undead", 2),
            entry("zombie_villager", "undead", "undead", 2),
            entry("zombified_piglin", "undead", "undead", 2));

    private VanillaMobTaxonomyDefaults() {
    }

    public static Optional<ResolvedMobDefinition> resolve(ResourceLocation entityTypeId) {
        if (entityTypeId == null) {
            return Optional.empty();
        }
        Taxonomy taxonomy = DEFAULTS.get(entityTypeId);
        if (taxonomy == null) {
            return Optional.empty();
        }
        return Optional.of(new ResolvedMobDefinition(
                entityTypeId,
                taxonomy.rank(),
                null,
                null,
                null,
                null,
                null,
                taxonomy.race(),
                taxonomy.element(),
                taxonomy.elementLevel(),
                null,
                taxonomy.attackRange(),
                null,
                null,
                null,
                null,
                null));
    }

    public static boolean isBossLike(ResourceLocation entityTypeId) {
        if (entityTypeId == null) {
            return false;
        }
        Taxonomy taxonomy = DEFAULTS.get(entityTypeId);
        return taxonomy != null && taxonomy.rank() == MobRank.BOSS;
    }

    private static Map.Entry<ResourceLocation, Taxonomy> entry(String path, String race, String element, int elementLevel) {
        return entry(path, race, element, elementLevel, 2, null);
    }

    private static Map.Entry<ResourceLocation, Taxonomy> entry(String path, String race, String element, int elementLevel,
            int attackRange) {
        return entry(path, race, element, elementLevel, attackRange, null);
    }

    private static Map.Entry<ResourceLocation, Taxonomy> entry(String path, String race, String element, int elementLevel,
            MobRank rank) {
        return entry(path, race, element, elementLevel, 2, rank);
    }

    private static Map.Entry<ResourceLocation, Taxonomy> entry(String path, String race, String element, int elementLevel,
            int attackRange, MobRank rank) {
        return Map.entry(
                ResourceLocation.fromNamespaceAndPath("minecraft", path),
                new Taxonomy(race, element, elementLevel, Math.max(0, attackRange), rank));
    }

    private record Taxonomy(String race, String element, int elementLevel, int attackRange, MobRank rank) {
    }
}
