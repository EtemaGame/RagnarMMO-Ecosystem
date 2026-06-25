package com.etema.ragnarmmo.jobs.player;

import com.etema.ragnarmmo.common.api.skills.IPlayerSkills;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerJobSkills implements IPlayerSkills {
    public static final int HOTBAR_SIZE = 9;

    private final Map<ResourceLocation, Integer> levels = new LinkedHashMap<>();
    private final ResourceLocation[] hotbar = new ResourceLocation[HOTBAR_SIZE];
    private final Map<ResourceLocation, Long> cooldowns = new LinkedHashMap<>();
    private boolean dirty = true;

    @Override
    public int getSkillLevel(ResourceLocation skillId) {
        return levels.getOrDefault(skillId, 0);
    }

    @Override
    public Map<ResourceLocation, Integer> getSkillLevels() {
        return Map.copyOf(levels);
    }

    public void setSkillLevel(ResourceLocation skillId, int level) {
        if (skillId == null) {
            return;
        }
        int clamped = Math.max(0, level);
        if (clamped == 0) {
            levels.remove(skillId);
        } else {
            levels.put(skillId, clamped);
        }
        dirty = true;
    }

    public ResourceLocation getHotbarSlot(int slot) {
        return slot >= 0 && slot < HOTBAR_SIZE ? hotbar[slot] : null;
    }

    public Map<Integer, ResourceLocation> getHotbarSnapshot() {
        Map<Integer, ResourceLocation> snapshot = new LinkedHashMap<>();
        for (int i = 0; i < hotbar.length; i++) {
            if (hotbar[i] != null) {
                snapshot.put(i, hotbar[i]);
            }
        }
        return Map.copyOf(snapshot);
    }

    public void setHotbarSlot(int slot, ResourceLocation skillId) {
        if (slot < 0 || slot >= HOTBAR_SIZE) {
            return;
        }
        hotbar[slot] = skillId;
        dirty = true;
    }

    public boolean isOnCooldown(ResourceLocation skillId, long gameTime) {
        return skillId != null && cooldowns.getOrDefault(skillId, 0L) > gameTime;
    }

    public void setCooldown(ResourceLocation skillId, long gameTime, int ticks) {
        if (skillId == null || ticks <= 0) {
            return;
        }
        cooldowns.put(skillId, gameTime + ticks);
        dirty = true;
    }

    public boolean consumeDirty() {
        boolean wasDirty = dirty;
        dirty = false;
        return wasDirty;
    }

    public void markDirty() {
        dirty = true;
    }

    public void resetAll() {
        levels.clear();
        Arrays.fill(hotbar, null);
        cooldowns.clear();
        dirty = true;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag skillLevels = new CompoundTag();
        for (var entry : levels.entrySet()) {
            skillLevels.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("SkillLevels", skillLevels);

        ListTag hotbarTag = new ListTag();
        for (ResourceLocation skillId : hotbar) {
            hotbarTag.add(net.minecraft.nbt.StringTag.valueOf(skillId == null ? "" : skillId.toString()));
        }
        tag.put("hotbar", hotbarTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        levels.clear();
        Arrays.fill(hotbar, null);
        cooldowns.clear();

        if (nbt.contains("SkillLevels")) {
            CompoundTag skillLevels = nbt.getCompound("SkillLevels");
            for (String key : skillLevels.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id != null) {
                    levels.put(id, Math.max(0, skillLevels.getInt(key)));
                }
            }
        } else {
            // Legacy monolith format: each skill id was a top-level compound with a level field.
            for (String key : nbt.getAllKeys()) {
                if (isReservedKey(key)) {
                    continue;
                }
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id != null) {
                    CompoundTag skillTag = nbt.getCompound(key);
                    int level = skillTag.contains("level") ? skillTag.getInt("level") : 0;
                    if (level > 0) {
                        levels.put(id, level);
                    }
                }
            }
        }

        if (nbt.contains("hotbar")) {
            ListTag hotbarTag = nbt.getList("hotbar", 8);
            for (int i = 0; i < HOTBAR_SIZE && i < hotbarTag.size(); i++) {
                ResourceLocation skillId = ResourceLocation.tryParse(hotbarTag.getString(i));
                hotbar[i] = skillId;
            }
        }
        markDirty();
    }

    private static boolean isReservedKey(String key) {
        return "SkillLevels".equals(key)
                || "cartInventory".equals(key)
                || "hotbar".equals(key)
                || "cooldowns".equals(key)
                || "globalCooldown".equals(key)
                || "globalCooldownDuration".equals(key)
                || "warpPortal".equals(key);
    }
}
