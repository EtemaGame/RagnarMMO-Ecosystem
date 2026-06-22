package com.etema.ragnarmmo.player.party;

import net.minecraft.nbt.CompoundTag;

/**
 * Configurable settings for a party.
 * These settings are persisted along with the party.
 */
public class PartySettings {
    public enum LootMode {
        FREE,
        PRIORITY,
        ROUND_ROBIN,
        OFF;

        public static LootMode fromId(String id) {
            if (id == null || id.isBlank()) {
                return PRIORITY;
            }
            try {
                return valueOf(id.trim().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return PRIORITY;
            }
        }
    }

    private boolean xpShareEnabled;
    private double shareRange;
    private LootMode lootMode;
    private int lootPrioritySeconds;

    // Defaults
    private static final boolean DEFAULT_XP_SHARE_ENABLED = true;
    private static final double DEFAULT_SHARE_RANGE = 50.0;
    private static final LootMode DEFAULT_LOOT_MODE = LootMode.PRIORITY;
    private static final int DEFAULT_LOOT_PRIORITY_SECONDS = 10;

    public PartySettings() {
        this.xpShareEnabled = DEFAULT_XP_SHARE_ENABLED;
        this.shareRange = DEFAULT_SHARE_RANGE;
        this.lootMode = DEFAULT_LOOT_MODE;
        this.lootPrioritySeconds = DEFAULT_LOOT_PRIORITY_SECONDS;
    }

    private PartySettings(boolean xpShareEnabled, double shareRange, LootMode lootMode, int lootPrioritySeconds) {
        this.xpShareEnabled = xpShareEnabled;
        this.shareRange = shareRange;
        this.lootMode = lootMode == null ? DEFAULT_LOOT_MODE : lootMode;
        this.lootPrioritySeconds = clampPrioritySeconds(lootPrioritySeconds);
    }

    // === Getters and Setters ===

    public boolean isXpShareEnabled() {
        return xpShareEnabled;
    }

    public void setXpShareEnabled(boolean enabled) {
        this.xpShareEnabled = enabled;
    }

    public double getShareRange() {
        return shareRange;
    }

    public void setShareRange(double range) {
        this.shareRange = Math.max(0, Math.min(200, range)); // Clamp 0-200 blocks
    }

    public LootMode getLootMode() {
        return lootMode;
    }

    public void setLootMode(LootMode lootMode) {
        this.lootMode = lootMode == null ? DEFAULT_LOOT_MODE : lootMode;
    }

    public int getLootPrioritySeconds() {
        return lootPrioritySeconds;
    }

    public void setLootPrioritySeconds(int seconds) {
        this.lootPrioritySeconds = clampPrioritySeconds(seconds);
    }

    // === NBT Serialization ===

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("xpShareEnabled", xpShareEnabled);
        tag.putDouble("shareRange", shareRange);
        tag.putString("lootMode", lootMode.name());
        tag.putInt("lootPrioritySeconds", lootPrioritySeconds);
        return tag;
    }

    public static PartySettings load(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return new PartySettings();
        }

        boolean xpShareEnabled = tag.contains("xpShareEnabled")
            ? tag.getBoolean("xpShareEnabled")
            : DEFAULT_XP_SHARE_ENABLED;
        double shareRange = tag.contains("shareRange")
            ? tag.getDouble("shareRange")
            : DEFAULT_SHARE_RANGE;
        LootMode lootMode = tag.contains("lootMode")
                ? LootMode.fromId(tag.getString("lootMode"))
                : DEFAULT_LOOT_MODE;
        int lootPrioritySeconds = tag.contains("lootPrioritySeconds")
                ? tag.getInt("lootPrioritySeconds")
                : DEFAULT_LOOT_PRIORITY_SECONDS;

        return new PartySettings(xpShareEnabled, shareRange, lootMode, lootPrioritySeconds);
    }

    @Override
    public String toString() {
        return String.format("PartySettings[xpShare=%s, range=%.1f, loot=%s, priority=%ss]",
                xpShareEnabled, shareRange, lootMode, lootPrioritySeconds);
    }

    private static int clampPrioritySeconds(int seconds) {
        return Math.max(0, Math.min(120, seconds));
    }
}
