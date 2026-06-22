package com.etema.ragnarmmo.skills.api;

/**
 * Skill tier for RO-style tree organization.
 * Skill tier used by the data-driven skill system.
 *
 * - NOVICE: Common starter skills available to all
 * - FIRST: 1st job class skills (Swordsman, Mage, etc.)
 * - TRANSCENDENT / THIRD / FOURTH: reserved entries for future expansions,
 *   not part of the current core scope
 * - LIFE: Life skills (Mining, Fishing, etc.)
 */
public enum SkillTier {
    NOVICE("Novice"),
    FIRST("First Class"),
    TRANSCENDENT("Transcendent"),
    THIRD("Third Class"),
    FOURTH("Fourth Class"),
    LIFE("Life");

    private final String displayName;

    SkillTier(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public static SkillTier fromId(String id) {
        if (id == null || id.isEmpty())
            return null;
        try {
            return valueOf(id.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
