package com.etema.ragnarmmo.combat.contract;

import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.combat.formula.ThiefSkillFormulaService;
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
        java.util.Map<String, Double> numericData = definition.getLevelDataMap().getOrDefault(level, java.util.Map.of());
        RagnarDamageType damageType = parseDamageType(
                definition.getLevelString("damage_type", level, null),
                numericData.get("damage_type"),
                defaultDamageType(path));
        ElementType element = parseElement(
                definition.getLevelString("element", level, null),
                numericData.get("element"),
                defaultElement(path));
        SkillHitPolicy defaultHitPolicy = damageType == RagnarDamageType.PHYSICAL
                ? SkillHitPolicy.BASIC_ATTACK
                : SkillHitPolicy.ALWAYS_HIT;
        SkillHitPolicy hitPolicy = parseHitPolicy(
                definition.getLevelString("hit_policy", level, null),
                numericData.get("hit_policy"),
                defaultHitPolicy);
        double damagePercent = definition.getLevelDouble("damage_percent", level, defaultDamagePercent(path, level));
        int hitCount = definition.getLevelInt("hit_count", level, "cold_bolt".equals(path) ? Math.min(level, 10) : 1);
        double aoeRadius = definition.getLevelDouble("aoe_radius", level,
                definition.getLevelDouble("reveal_radius", level, defaultAoeRadius(path)));
        double splashRatio = definition.getLevelDouble("splash_ratio", level, 1.0D);
        double accuracyBonus = definition.getLevelDouble("accuracy_bonus", level, defaultAccuracyBonus(path, level));
        double defenseBypassPercent = definition.getLevelDouble("def_bypass_percent", level,
                defaultDefenseBypassPercent(path, damagePercent));
        double flatDamageBonus = definition.getLevelDouble("flat_damage_bonus", level, defaultFlatDamageBonus(path, level));
        double undeadMultiplier = definition.getLevelDouble("undead_multiplier", level, defaultUndeadMultiplier(path, level));
        SkillRangeType rangeType = parseRangeType(
                definition.getLevelString("range_type", level, null),
                numericData.get("range_type"),
                defaultRangeType(path, damageType));
        SkillElementPolicy elementPolicy = parseElementPolicy(
                definition.getLevelString("element_policy", level, null),
                numericData.get("element_policy"),
                defaultElementPolicy(path, damageType));
        SkillDefensePolicy defensePolicy = parseDefensePolicy(
                definition.getLevelString("defense_policy", level, null),
                numericData.get("defense_policy"),
                defenseBypassPercent >= damagePercent && damagePercent > 0.0D ? SkillDefensePolicy.IGNORE_DEF : SkillDefensePolicy.NORMAL);
        SkillMultiHitPolicy multiHitPolicy = parseMultiHitPolicy(
                definition.getLevelString("multi_hit_policy", level, null),
                numericData.get("multi_hit_policy"),
                hitCount > 1 ? SkillMultiHitPolicy.PER_HIT : SkillMultiHitPolicy.SINGLE);
        return Optional.of(new SkillCombatSpec(damageType, element, hitPolicy, damagePercent, hitCount, aoeRadius,
                splashRatio, accuracyBonus, defenseBypassPercent, flatDamageBonus, undeadMultiplier,
                rangeType, elementPolicy, defensePolicy, multiHitPolicy));
    }

    public static boolean isMigratedCombatSkill(ResourceLocation id) {
        if (id == null || !"ragnarmmo".equals(id.getNamespace())) {
            return false;
        }
        return switch (id.getPath()) {
            case "arrow_shower", "bash", "cold_bolt", "double_strafe", "envenom", "fire_ball", "fire_bolt",
                    "fire_wall", "frost_diver", "lightning_bolt", "magnum_break", "mammonite", "napalm_beat",
                    "ruwach", "soul_strike", "thunder_storm" -> true;
            default -> false;
        };
    }

    public static boolean shouldExecuteLegacyEffectAfterContract(ResourceLocation id) {
        if (id == null || !"ragnarmmo".equals(id.getNamespace())) {
            return false;
        }
        return switch (id.getPath()) {
            case "cold_bolt", "fire_ball", "frost_diver", "napalm_beat", "soul_strike" -> true;
            default -> false;
        };
    }

    private static boolean isCombatSkill(ISkillDefinition definition, int level) {
        return definition.getLevelDataMap().containsKey(level)
                || isMigratedCombatSkill(definition.getId());
    }

    private static RagnarDamageType parseDamageType(String raw, Double numeric, RagnarDamageType fallback) {
        if (raw != null && !raw.isBlank()) {
            String normalized = normalize(raw);
            return switch (normalized) {
                case "physical", "phys" -> RagnarDamageType.PHYSICAL;
                case "magical", "magic" -> RagnarDamageType.MAGICAL;
                case "true" -> RagnarDamageType.TRUE;
                default -> fallback;
            };
        }
        if (numeric == null) {
            return fallback;
        }
        return switch ((int) Math.round(numeric)) {
            case 0 -> RagnarDamageType.PHYSICAL;
            case 1 -> RagnarDamageType.MAGICAL;
            case 2 -> RagnarDamageType.TRUE;
            default -> fallback;
        };
    }

    private static ElementType parseElement(String raw, Double numeric, ElementType fallback) {
        if (raw != null && !raw.isBlank()) {
            String normalized = normalize(raw);
            if ("shadow".equals(normalized)) {
                return ElementType.DARK;
            }
            try {
                return ElementType.valueOf(normalized.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return fallback;
            }
        }
        if (numeric == null) {
            return fallback;
        }
        int ordinal = (int) Math.round(numeric);
        ElementType[] values = ElementType.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    private static SkillHitPolicy parseHitPolicy(String raw, Double numeric, SkillHitPolicy fallback) {
        if (raw != null && !raw.isBlank()) {
            String normalized = normalize(raw);
            return switch (normalized) {
                case "basic_attack", "basic", "hit_flee" -> SkillHitPolicy.BASIC_ATTACK;
                case "always_hit", "always", "autohit" -> SkillHitPolicy.ALWAYS_HIT;
                default -> fallback;
            };
        }
        if (numeric == null) {
            return fallback;
        }
        return switch ((int) Math.round(numeric)) {
            case 0 -> SkillHitPolicy.BASIC_ATTACK;
            case 1 -> SkillHitPolicy.ALWAYS_HIT;
            default -> fallback;
        };
    }

    private static String normalize(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static SkillRangeType parseRangeType(String raw, Double numeric, SkillRangeType fallback) {
        if (raw != null && !raw.isBlank()) {
            return switch (normalize(raw)) {
                case "melee" -> SkillRangeType.MELEE;
                case "ranged", "range", "projectile" -> SkillRangeType.RANGED;
                case "magic", "magical" -> SkillRangeType.MAGIC;
                case "ground", "ground_target" -> SkillRangeType.GROUND;
                default -> fallback;
            };
        }
        if (numeric == null) {
            return fallback;
        }
        return switch ((int) Math.round(numeric)) {
            case 0 -> SkillRangeType.MELEE;
            case 1 -> SkillRangeType.RANGED;
            case 2 -> SkillRangeType.MAGIC;
            case 3 -> SkillRangeType.GROUND;
            default -> fallback;
        };
    }

    private static SkillElementPolicy parseElementPolicy(String raw, Double numeric, SkillElementPolicy fallback) {
        if (raw != null && !raw.isBlank()) {
            return switch (normalize(raw)) {
                case "weapon", "weapon_element" -> SkillElementPolicy.WEAPON;
                case "skill", "forced", "force", "forced_element" -> SkillElementPolicy.SKILL;
                default -> fallback;
            };
        }
        if (numeric == null) {
            return fallback;
        }
        return switch ((int) Math.round(numeric)) {
            case 0 -> SkillElementPolicy.WEAPON;
            case 1 -> SkillElementPolicy.SKILL;
            default -> fallback;
        };
    }

    private static SkillDefensePolicy parseDefensePolicy(String raw, Double numeric, SkillDefensePolicy fallback) {
        if (raw != null && !raw.isBlank()) {
            return switch (normalize(raw)) {
                case "normal" -> SkillDefensePolicy.NORMAL;
                case "ignore", "ignore_def", "ignore_defense" -> SkillDefensePolicy.IGNORE_DEF;
                default -> fallback;
            };
        }
        if (numeric == null) {
            return fallback;
        }
        return switch ((int) Math.round(numeric)) {
            case 0 -> SkillDefensePolicy.NORMAL;
            case 1 -> SkillDefensePolicy.IGNORE_DEF;
            default -> fallback;
        };
    }

    private static SkillMultiHitPolicy parseMultiHitPolicy(String raw, Double numeric, SkillMultiHitPolicy fallback) {
        if (raw != null && !raw.isBlank()) {
            return switch (normalize(raw)) {
                case "single", "aggregate", "aggregated" -> SkillMultiHitPolicy.SINGLE;
                case "per_hit", "multi", "multi_hit", "separate" -> SkillMultiHitPolicy.PER_HIT;
                default -> fallback;
            };
        }
        if (numeric == null) {
            return fallback;
        }
        return switch ((int) Math.round(numeric)) {
            case 0 -> SkillMultiHitPolicy.SINGLE;
            case 1 -> SkillMultiHitPolicy.PER_HIT;
            default -> fallback;
        };
    }

    private static RagnarDamageType defaultDamageType(String skillPath) {
        return switch (skillPath) {
            case "cold_bolt", "fire_ball", "fire_bolt", "fire_wall", "frost_diver", "napalm_beat", "ruwach",
                    "soul_strike", "lightning_bolt", "thunder_storm" -> RagnarDamageType.MAGICAL;
            default -> RagnarDamageType.PHYSICAL;
        };
    }

    private static ElementType defaultElement(String skillPath) {
        return switch (skillPath) {
            case "fire_ball", "fire_bolt", "fire_wall", "magnum_break" -> ElementType.FIRE;
            case "cold_bolt", "frost_diver" -> ElementType.WATER;
            case "lightning_bolt", "thunder_storm" -> ElementType.WIND;
            case "envenom" -> ElementType.POISON;
            case "ruwach" -> ElementType.HOLY;
            case "napalm_beat", "soul_strike" -> ElementType.GHOST;
            default -> ElementType.NEUTRAL;
        };
    }

    private static double defaultDamagePercent(String skillPath, int level) {
        return switch (skillPath) {
            default -> 100.0D;
        };
    }

    private static double defaultAoeRadius(String skillPath) {
        return switch (skillPath) {
            case "ruwach" -> 5.0D;
            default -> 0.0D;
        };
    }

    private static SkillRangeType defaultRangeType(String skillPath, RagnarDamageType damageType) {
        return switch (skillPath) {
            case "arrow_shower", "double_strafe" -> SkillRangeType.RANGED;
            case "fire_wall", "thunder_storm" -> SkillRangeType.GROUND;
            default -> damageType == RagnarDamageType.MAGICAL ? SkillRangeType.MAGIC : SkillRangeType.MELEE;
        };
    }

    private static SkillElementPolicy defaultElementPolicy(String skillPath, RagnarDamageType damageType) {
        return switch (skillPath) {
            case "envenom", "fire_ball", "fire_bolt", "fire_wall", "frost_diver",
                    "cold_bolt", "lightning_bolt", "thunder_storm", "magnum_break",
                    "napalm_beat", "soul_strike", "ruwach" -> SkillElementPolicy.SKILL;
            default -> damageType == RagnarDamageType.MAGICAL ? SkillElementPolicy.SKILL : SkillElementPolicy.WEAPON;
        };
    }

    private static double defaultAccuracyBonus(String skillPath, int level) {
        return switch (skillPath) {
            case "bash" -> 5.0D * level;
            case "magnum_break" -> 10.0D * level;
            default -> 0.0D;
        };
    }

    private static double defaultDefenseBypassPercent(String skillPath, double damagePercent) {
        return switch (skillPath) {
            case "magnum_break" -> damagePercent / 6.0D;
            default -> 0.0D;
        };
    }

    private static double defaultFlatDamageBonus(String skillPath, int level) {
        return switch (skillPath) {
            case "envenom" -> ThiefSkillFormulaService.envenomFlatBonus(level);
            default -> 0.0D;
        };
    }

    private static double defaultUndeadMultiplier(String skillPath, int level) {
        return switch (skillPath) {
            case "soul_strike" -> 1.0D + 0.05D * Math.max(0, Math.min(10, level));
            default -> 1.0D;
        };
    }
}
