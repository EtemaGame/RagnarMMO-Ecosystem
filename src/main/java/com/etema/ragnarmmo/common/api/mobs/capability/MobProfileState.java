package com.etema.ragnarmmo.common.api.mobs.capability;

import com.etema.ragnarmmo.common.api.mobs.MobRank;
import com.etema.ragnarmmo.common.api.mobs.profile.MobProfile;
import com.etema.ragnarmmo.common.api.stats.RoBaseStats;
import net.minecraft.nbt.CompoundTag;

public final class MobProfileState {
    private MobProfile profile = defaultProfile();
    private boolean initialized;

    public MobProfile profile() {
        return profile;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setProfile(MobProfile profile) {
        this.profile = profile == null ? defaultProfile() : profile;
        this.initialized = profile != null;
    }

    public void clearProfile() {
        this.profile = defaultProfile();
        this.initialized = false;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Initialized", initialized);
        tag.putInt("Level", profile.level());
        tag.putString("Rank", profile.rank().name());
        tag.putInt("BaseStr", profile.baseStats().str());
        tag.putInt("BaseAgi", profile.baseStats().agi());
        tag.putInt("BaseVit", profile.baseStats().vit());
        tag.putInt("BaseInt", profile.baseStats().intel());
        tag.putInt("BaseDex", profile.baseStats().dex());
        tag.putInt("BaseLuk", profile.baseStats().luk());
        tag.putInt("MaxHp", profile.maxHp());
        tag.putInt("AtkMin", profile.atkMin());
        tag.putInt("AtkMax", profile.atkMax());
        tag.putInt("MatkMin", profile.matkMin());
        tag.putInt("MatkMax", profile.matkMax());
        tag.putInt("Def", profile.def());
        tag.putInt("MDef", profile.mdef());
        tag.putInt("Hit", profile.hit());
        tag.putInt("Flee", profile.flee());
        tag.putInt("Crit", profile.crit());
        tag.putInt("Aspd", profile.aspd());
        tag.putDouble("MoveSpeed", profile.moveSpeed());
        tag.putInt("BaseExp", profile.baseExp());
        tag.putInt("JobExp", profile.jobExp());
        tag.putString("Race", profile.race());
        tag.putString("Element", profile.element());
        tag.putInt("ElementLevel", profile.elementLevel());
        tag.putString("Size", profile.size());
        tag.putInt("AttackRange", profile.attackRange());
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        MobRank rank = readEnum(nbt.getString("Rank"), MobRank.NORMAL, MobRank.class);
        initialized = nbt.getBoolean("Initialized");
        int atkMin = Math.max(0, nbt.getInt("AtkMin"));
        int matkMin = Math.max(0, nbt.getInt("MatkMin"));
        profile = new MobProfile(
                Math.max(1, nbt.getInt("Level")),
                rank,
                readBaseStats(nbt),
                Math.max(1, nbt.getInt("MaxHp")),
                atkMin,
                Math.max(atkMin, nbt.getInt("AtkMax")),
                matkMin,
                Math.max(matkMin, nbt.getInt("MatkMax")),
                Math.max(0, nbt.getInt("Def")),
                Math.max(0, nbt.getInt("MDef")),
                Math.max(0, nbt.getInt("Hit")),
                Math.max(0, nbt.getInt("Flee")),
                Math.max(0, nbt.getInt("Crit")),
                Math.max(1, nbt.getInt("Aspd")),
                Math.max(0.0001D, nbt.getDouble("MoveSpeed")),
                Math.max(0, nbt.getInt("BaseExp")),
                Math.max(0, nbt.getInt("JobExp")),
                token(nbt.getString("Race"), "unknown"),
                token(nbt.getString("Element"), "neutral"),
                clampElementLevel(nbt.contains("ElementLevel") ? nbt.getInt("ElementLevel") : 1),
                token(nbt.getString("Size"), "medium"),
                Math.max(0, nbt.contains("AttackRange") ? nbt.getInt("AttackRange") : 2));
    }

    private static RoBaseStats readBaseStats(CompoundTag nbt) {
        return new RoBaseStats(
                Math.max(1, nbt.getInt("BaseStr")),
                Math.max(1, nbt.getInt("BaseAgi")),
                Math.max(1, nbt.getInt("BaseVit")),
                Math.max(1, nbt.getInt("BaseInt")),
                Math.max(1, nbt.getInt("BaseDex")),
                Math.max(1, nbt.getInt("BaseLuk")));
    }

    private static <T extends Enum<T>> T readEnum(String raw, T fallback, Class<T> enumClass) {
        try {
            return Enum.valueOf(enumClass, raw);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static String token(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int clampElementLevel(int value) {
        return Math.max(1, Math.min(4, value));
    }

    public static MobProfile defaultProfile() {
        return new MobProfile(1, MobRank.NORMAL, RoBaseStats.novice(), 20, 2, 4, 0, 0,
                0, 0, 10, 5, 1, 150, 0.2D, 1, 1, "unknown", "neutral", 1, "medium", 2);
    }
}
