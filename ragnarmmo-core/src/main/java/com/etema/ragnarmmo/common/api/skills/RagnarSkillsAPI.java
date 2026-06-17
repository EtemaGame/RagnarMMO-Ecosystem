package com.etema.ragnarmmo.common.api.skills;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RagnarSkillsAPI {
    @FunctionalInterface
    public interface SkillsAccessor extends Function<Player, Optional<IPlayerSkills>> {
    }

    @FunctionalInterface
    public interface LocalLevelAccessor {
        int getLevel(ResourceLocation skillId);
    }

    private static final SkillsAccessor DEFAULT_ACCESSOR = player -> Optional.empty();
    private static final LocalLevelAccessor DEFAULT_LOCAL_LEVELS = skillId -> 0;
    private static final Consumer<ResourceLocation> DEFAULT_UPGRADE_REQUEST = skillId -> {
    };

    private static final AtomicReference<SkillsAccessor> ACCESSOR = new AtomicReference<>(DEFAULT_ACCESSOR);
    private static final AtomicReference<LocalLevelAccessor> LOCAL_LEVELS = new AtomicReference<>(DEFAULT_LOCAL_LEVELS);
    private static final AtomicReference<Consumer<ResourceLocation>> UPGRADE_REQUEST =
            new AtomicReference<>(DEFAULT_UPGRADE_REQUEST);

    private RagnarSkillsAPI() {
    }

    public static void registerAccessor(SkillsAccessor accessor) {
        ACCESSOR.set(accessor != null ? accessor : DEFAULT_ACCESSOR);
    }

    public static Optional<IPlayerSkills> get(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return ACCESSOR.get().apply(player);
    }

    public static void registerLocalLevelAccessor(LocalLevelAccessor accessor) {
        LOCAL_LEVELS.set(accessor != null ? accessor : DEFAULT_LOCAL_LEVELS);
    }

    public static int getLocalLevel(ResourceLocation skillId) {
        if (skillId == null) {
            return 0;
        }
        return LOCAL_LEVELS.get().getLevel(skillId);
    }

    public static void registerUpgradeRequest(Consumer<ResourceLocation> request) {
        UPGRADE_REQUEST.set(request != null ? request : DEFAULT_UPGRADE_REQUEST);
    }

    public static void requestUpgrade(ResourceLocation skillId) {
        if (skillId != null) {
            UPGRADE_REQUEST.get().accept(skillId);
        }
    }

    public static Map<ResourceLocation, Integer> getLocalLevelsSnapshot() {
        return Map.of();
    }
}
