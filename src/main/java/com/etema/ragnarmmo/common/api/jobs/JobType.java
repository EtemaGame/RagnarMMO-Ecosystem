package com.etema.ragnarmmo.common.api.jobs;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Defines various jobs (classes) and their skill bonuses.
 */
public enum JobType {
    NOVICE("Novice"),

    // First Classes (Primary)
    SWORDSMAN("Swordsman"),
    MAGE("Mage"),
    ARCHER("Archer"),
    THIEF("Thief"),
    MERCHANT("Merchant"),
    ACOLYTE("Acolyte");

    private final String displayName;
    private final Map<ResourceLocation, Double> xpMultipliers;
    private final Set<ResourceLocation> classTreeSkills;

    JobType(String displayName) {
        this.displayName = displayName;
        this.xpMultipliers = new java.util.LinkedHashMap<>();
        this.classTreeSkills = new LinkedHashSet<>();
    }

    static {
        for (JobType job : values()) {
            job.registerBonuses();
        }
    }

    private void registerBonuses() {
        switch (this) {
            case NOVICE -> {
                classTreeSkills.add(skillId("first_aid"));
                classTreeSkills.add(skillId("basic_skill"));
                classTreeSkills.add(skillId("play_dead"));
            }
            case SWORDSMAN -> {
                classTreeSkills.add(skillId("sword_mastery"));
                classTreeSkills.add(skillId("bash"));
                classTreeSkills.add(skillId("provoke"));
                classTreeSkills.add(skillId("one_hand_mastery"));
                classTreeSkills.add(skillId("two_hand_mastery"));
                classTreeSkills.add(skillId("endurance"));

                // XP Bonuses (Examples, to be refined if needed)
                xpMultipliers.put(skillId("sword_mastery"), 1.3);
            }
            case MAGE -> {
                classTreeSkills.add(skillId("staff_mastery"));
                classTreeSkills.add(skillId("spell_knowledge"));
                classTreeSkills.add(skillId("mana_control"));
                classTreeSkills.add(skillId("magic_amplification"));
                classTreeSkills.add(skillId("elemental_affinity"));
                classTreeSkills.add(skillId("magic_guard"));
                classTreeSkills.add(skillId("arcane_regeneration"));
                classTreeSkills.add(skillId("overcast"));
            }
            case ARCHER -> {
                classTreeSkills.add(skillId("bow_mastery"));
                classTreeSkills.add(skillId("accuracy_training"));
                classTreeSkills.add(skillId("critical_shot"));
                classTreeSkills.add(skillId("evasion_boost"));
                classTreeSkills.add(skillId("wind_walker"));
                classTreeSkills.add(skillId("kiting_instinct"));
            }
            case THIEF -> {
                classTreeSkills.add(skillId("dagger_mastery"));
                classTreeSkills.add(skillId("backstab_training"));
                classTreeSkills.add(skillId("stealth_instinct"));
                classTreeSkills.add(skillId("flee_training"));
                classTreeSkills.add(skillId("poison_expertise"));
                classTreeSkills.add(skillId("fatal_instinct"));
            }
            case ACOLYTE -> {
                classTreeSkills.add(skillId("mace_mastery"));
                classTreeSkills.add(skillId("faith"));
                classTreeSkills.add(skillId("divine_protection"));
                classTreeSkills.add(skillId("heal_power"));
                classTreeSkills.add(skillId("holy_resistance"));
                classTreeSkills.add(skillId("blessing_aura"));
            }
            case MERCHANT -> {
                classTreeSkills.add(skillId("trading_knowledge"));
                classTreeSkills.add(skillId("weapon_maintenance"));
                classTreeSkills.add(skillId("armor_maintenance"));
                classTreeSkills.add(skillId("overcharge"));
                classTreeSkills.add(skillId("business_mind"));
            }
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return name().toLowerCase(Locale.ROOT);
    }

    public double getXpMultiplier(ResourceLocation skillId) {
        if (skillId == null) {
            return 1.0;
        }
        return xpMultipliers.getOrDefault(skillId, 1.0);
    }

    /**
     * Gets all allowed skill IDs as ResourceLocations.
     *
     * @return Set of allowed skill ResourceLocations
     */
    public Set<ResourceLocation> getAllowedSkillIds() {
        return Set.copyOf(classTreeSkills);
    }

    private static Set<ResourceLocation> skillIds(String... paths) {
        LinkedHashSet<ResourceLocation> ids = new LinkedHashSet<>();
        for (String path : paths) {
            ids.add(ResourceLocation.fromNamespaceAndPath("ragnarmmo", path));
        }
        return Set.copyOf(ids);
    }

    private static ResourceLocation skillId(String path) {
        return ResourceLocation.fromNamespaceAndPath("ragnarmmo", path);
    }

    /**
     * Returns true if this job is a magical class (uses Mana).
     */
    public boolean isMagical() {
        return this == MAGE || this == ACOLYTE;
    }

    /**
     * Returns true if this job is a physical class (uses SP).
     */
    public boolean isPhysical() {
        return !isMagical();
    }

    /**
     * Returns the display label for the resource bar (always "SP" after Phase 2 unification).
     */
    public String getResourceLabel() {
        return "SP";
    }

    public static JobType fromId(String id) {
        String normalized = normalizeKey(id);
        if (normalized.isBlank())
            return NOVICE;

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return NOVICE;
        }
    }

    public static String normalizeKey(String id) {
        if (id == null) {
            return "";
        }

        String normalized = id.trim();
        if (normalized.isBlank()) {
            return "";
        }

        if (normalized.contains(":")) {
            normalized = normalized.substring(normalized.indexOf(':') + 1);
        }

        return normalized.toUpperCase(Locale.ROOT);
    }

    // ── Class Hierarchy ──

    /** Set of all First Classes (Primary). */
    public static final Set<JobType> FIRST_CLASSES = Set.of(
            SWORDSMAN, MAGE, ARCHER, THIEF, MERCHANT, ACOLYTE);

    /**
     * Returns the tier of this job in the class hierarchy.
     * 0 = Novice, 1 = first class.
     */
    public int getTier() {
        return switch (this) {
            case NOVICE -> 0;
            case SWORDSMAN, MAGE, ARCHER, THIEF, MERCHANT, ACOLYTE -> 1;
            default -> 0;
        };
    }

    /**
     * Returns the prerequisite (parent) job needed to reach this class.
     * Returns null for NOVICE.
     */
    public JobType getParent() {
        return switch (this) {
            case NOVICE -> null;
            case SWORDSMAN, MAGE, ARCHER, THIEF, MERCHANT, ACOLYTE -> NOVICE;
            default -> null;
        };
    }

    /**
     * Returns the list of jobs this class can promote to.
     * Empty list if there are no promotions (leaf class or not yet implemented).
     */
    public List<JobType> getPromotions() {
        return switch (this) {
            case NOVICE -> List.of(SWORDSMAN, MAGE, ARCHER, THIEF, MERCHANT, ACOLYTE);
            default -> List.of();
        };
    }

    /**
     * Resolves the first-class ancestor for any job.
     */
    public JobType getFirstClassAncestor() {
        return switch (getTier()) {
            case 0 -> null;
            case 1 -> this;
            default -> getParent();
        };
    }

    public boolean hasPromotions() {
        return !getPromotions().isEmpty();
    }

    public boolean canPromoteTo(JobType target) {
        return target != null && getPromotions().contains(target);
    }

    public boolean matchesExactOrAncestor(JobType allowedJob) {
        if (allowedJob == null) {
            return false;
        }
        if (this == allowedJob) {
            return true;
        }
        JobType firstClass = getFirstClassAncestor();
        return firstClass != null && firstClass == allowedJob;
    }

    public boolean matchesSkillRule(String allowedJobId) {
        String normalized = normalizeKey(allowedJobId);
        if (normalized.isBlank()) {
            return false;
        }
        if (NOVICE.name().equals(normalized)) {
            return true;
        }
        if (name().equals(normalized)) {
            return true;
        }
        JobType firstClass = getFirstClassAncestor();
        return firstClass != null && firstClass.name().equals(normalized);
    }
}
