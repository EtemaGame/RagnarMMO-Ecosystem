package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.skills.api.ISkillDefinition;
import com.etema.ragnarmmo.skills.api.RagnarSkillDefinitionsAPI;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Optional;

public final class SkillCombatSpecResolver {
    private SkillCombatSpecResolver() {
    }

    public static Optional<SkillCombatSpec> resolve(ResourceLocation skillId, int level) {
        return RagnarSkillDefinitionsAPI.get(skillId).flatMap(def -> resolve(def, level));
    }

    public static Optional<SkillCombatSpec> resolve(ISkillDefinition definition, int level) {
        if (definition == null) {
            return Optional.empty();
        }
        ResourceLocation id = definition.getId();
        if (!isCombatSkill(definition, level)) {
            return Optional.empty();
        }

        String path = id.getPath();
        RagnarDamageType damageType = parseDamageType(definition.getLevelDataMap().getOrDefault(level, java.util.Map.of()).getOrDefault("damage_type", null),
                defaultDamageType(path));
        ElementType element = parseElement(definition.getLevelDataMap().getOrDefault(level, java.util.Map.of()).getOrDefault("element", null),
                defaultElement(path));
        SkillHitPolicy hitPolicy = parseHitPolicy(definition.getLevelDataMap().getOrDefault(level, java.util.Map.of()).getOrDefault("hit_policy", null),
                SkillHitPolicy.ALWAYS_HIT);
        double damagePercent = definition.getLevelDouble("damage_percent", level, defaultDamagePercent(path, level));
        int hitCount = definition.getLevelInt("hit_count", level, "cold_bolt".equals(path) ? Math.min(level, 10) : 1);
        double aoeRadius = definition.getLevelDouble("aoe_radius", level,
                definition.getLevelDouble("reveal_radius", level, defaultAoeRadius(path)));
        double splashRatio = definition.getLevelDouble("splash_ratio", level, 1.0D);
        return Optional.of(new SkillCombatSpec(damageType, element, hitPolicy, damagePercent, hitCount, aoeRadius, splashRatio));
    }

    public static boolean isMigratedCombatSkill(ResourceLocation id) {
        if (id == null || !"ragnarmmo".equals(id.getNamespace())) {
            return false;
        }
        return switch (id.getPath()) {
            case "arrow_shower", "bash", "blast_mine", "blitz_beat", "bowling_bash", "brandish_spear",
                    "cart_termination", "claymore_trap", "cold_bolt", "dark_illusion", "double_strafe", "envenom",
                    "fire_ball", "fire_pillar", "fire_wall", "frost_diver", "grimtooth",
                    "heavens_drive", "holy_light", "land_mine", "magnum_break", "magic_crasher",
                    "magnus_exorcismus", "mammonite", "meteor_storm", "napalm_beat", "pierce",
                    "ruwach", "shattering_strike", "sightrasher", "sonic_blow", "soul_strike", "spear_boomerang",
                    "spear_stab", "storm_gust", "thunder_storm", "turn_undead",
                    "venom_knife", "venom_splasher" -> true;
            default -> false;
        };
    }

    public static boolean shouldExecuteLegacyEffectAfterContract(ResourceLocation id) {
        if (id == null || !"ragnarmmo".equals(id.getNamespace())) {
            return false;
        }
        return switch (id.getPath()) {
            case "cold_bolt", "fire_ball", "frost_diver", "holy_light", "napalm_beat", "soul_strike" -> true;
            default -> false;
        };
    }

    private static boolean isCombatSkill(ISkillDefinition definition, int level) {
        return definition.getLevelDataMap().containsKey(level)
                || isMigratedCombatSkill(definition.getId());
    }

    private static RagnarDamageType parseDamageType(Double raw, RagnarDamageType fallback) {
        return fallback;
    }

    private static ElementType parseElement(Double raw, ElementType fallback) {
        return fallback;
    }

    private static SkillHitPolicy parseHitPolicy(Double raw, SkillHitPolicy fallback) {
        return fallback;
    }

    private static RagnarDamageType defaultDamageType(String skillPath) {
        return switch (skillPath) {
            case "cold_bolt", "fire_ball", "fire_pillar", "fire_wall", "frost_diver",
                    "heavens_drive", "holy_light", "lord_of_vermilion", "magic_crasher",
                    "magnus_exorcismus", "meteor_storm", "napalm_beat", "ruwach",
                    "sightrasher", "soul_strike", "storm_gust", "thunder_storm",
                    "turn_undead" -> RagnarDamageType.MAGICAL;
            default -> RagnarDamageType.PHYSICAL;
        };
    }

    private static ElementType defaultElement(String skillPath) {
        return switch (skillPath) {
            case "blast_mine", "claymore_trap", "fire_ball", "fire_pillar", "fire_wall", "magnum_break", "meteor_storm", "sightrasher" -> ElementType.FIRE;
            case "cold_bolt", "frost_diver", "storm_gust" -> ElementType.WATER;
            case "envenom", "venom_knife", "venom_splasher" -> ElementType.POISON;
            case "heavens_drive", "land_mine" -> ElementType.EARTH;
            case "holy_light", "magnus_exorcismus", "ruwach", "turn_undead" -> ElementType.HOLY;
            case "napalm_beat", "soul_strike" -> ElementType.GHOST;
            default -> ElementType.NEUTRAL;
        };
    }

    private static double defaultDamagePercent(String skillPath, int level) {
        return switch (skillPath) {
            case "blitz_beat" -> 100.0D;
            case "blast_mine" -> 100.0D + (20.0D * level);
            case "bowling_bash" -> 100.0D + (40.0D * level);
            case "brandish_spear" -> 100.0D + (40.0D * level);
            case "cart_termination" -> 250.0D + (50.0D * level);
            case "claymore_trap" -> 100.0D + (40.0D * level);
            case "dark_illusion" -> 100.0D + (50.0D * level);
            case "fire_pillar" -> 50.0D + (5.0D * level);
            case "grimtooth" -> 120.0D + (20.0D * level);
            case "heavens_drive" -> 100.0D + (40.0D * level);
            case "land_mine" -> 100.0D + (20.0D * level);
            case "magic_crasher" -> 100.0D + (20.0D * level);
            case "magnus_exorcismus" -> 100.0D;
            case "meteor_storm" -> 100.0D + (40.0D * level);
            case "pierce" -> 110.0D + (10.0D * level);
            case "sightrasher" -> 100.0D + (20.0D * level);
            case "shattering_strike" -> 100.0D + (30.0D * level);
            case "sonic_blow" -> 400.0D + (40.0D * level);
            case "spear_boomerang" -> 150.0D + (50.0D * level);
            case "spear_stab" -> 200.0D + (50.0D * level);
            case "storm_gust" -> 100.0D;
            case "turn_undead" -> 100.0D;
            case "venom_knife" -> 100.0D + (20.0D * level);
            case "venom_splasher" -> 100.0D + (40.0D * level);
            default -> 100.0D;
        };
    }

    private static double defaultAoeRadius(String skillPath) {
        return switch (skillPath) {
            case "brandish_spear" -> 3.0D;
            case "bowling_bash" -> 2.0D;
            case "fire_pillar" -> 2.0D;
            case "heavens_drive" -> 2.5D;
            case "magnus_exorcismus" -> 4.0D;
            case "meteor_storm" -> 4.0D;
            case "ruwach" -> 5.0D;
            case "sightrasher" -> 5.0D;
            case "storm_gust" -> 4.0D;
            default -> 0.0D;
        };
    }
}
