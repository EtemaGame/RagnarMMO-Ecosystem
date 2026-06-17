package com.etema.ragnarmmo.jobs.client;

import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.common.api.jobs.RagnarJobsAPI;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.jobs.net.ServerboundChangeJobPacket;
import com.etema.ragnarmmo.jobs.net.ServerboundSetHotbarSlotPacket;
import com.etema.ragnarmmo.jobs.net.ServerboundUpgradeSkillPacket;
import com.etema.ragnarmmo.jobs.net.ServerboundUseJobSkillPacket;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JobSkillsClientCache {
    private static final Map<ResourceLocation, Integer> LEVELS = new LinkedHashMap<>();
    private static final Map<Integer, ResourceLocation> HOTBAR = new LinkedHashMap<>();

    private JobSkillsClientCache() {
    }

    public static void registerApiHooks() {
        RagnarSkillsAPI.registerLocalLevelAccessor(JobSkillsClientCache::getLevel);
        RagnarSkillsAPI.registerUpgradeRequest(skillId -> Network.sendToServer(new ServerboundUpgradeSkillPacket(skillId)));
        RagnarJobsAPI.registerChangeJobRequest(job -> Network.sendToServer(new ServerboundChangeJobPacket(job.getId())));
    }

    public static void requestUse(ResourceLocation skillId) {
        if (skillId != null) {
            Network.sendToServer(new ServerboundUseJobSkillPacket(skillId));
        }
    }

    public static void requestSetHotbarSlot(int slot, ResourceLocation skillId) {
        Network.sendToServer(new ServerboundSetHotbarSlotPacket(slot, skillId));
    }

    public static void replace(Map<ResourceLocation, Integer> levels, Map<Integer, ResourceLocation> hotbar) {
        LEVELS.clear();
        if (levels != null) {
            LEVELS.putAll(levels);
        }
        HOTBAR.clear();
        if (hotbar != null) {
            HOTBAR.putAll(hotbar);
        }
    }

    public static int getLevel(ResourceLocation skillId) {
        return LEVELS.getOrDefault(skillId, 0);
    }

    public static ResourceLocation getHotbarSlot(int slot) {
        return HOTBAR.get(slot);
    }
}
