package com.etema.ragnarmmo.skills.api;

/**
 * Categories for skill organization.
 * Skill category used by the data-driven skill system.
 */
public enum SkillCategory {
    CLASS_PASSIVE("Class Tree"),
    LIFE("Life Skill"),
    MISC("Misc"),
    
    NOVICE("Novice"),
    SWORDSMAN("Swordsman"),
    MAGE("Mage"),
    ARCHER("Archer"),
    THIEF("Thief"),
    MERCHANT("Merchant"),
    ACOLYTE("Acolyte");

    private final String displayName;

    SkillCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public static SkillCategory fromId(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            return valueOf(id.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
