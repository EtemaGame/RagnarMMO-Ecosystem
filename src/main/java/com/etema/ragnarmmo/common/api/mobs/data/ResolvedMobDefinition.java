package com.etema.ragnarmmo.common.api.mobs.data;

import com.etema.ragnarmmo.common.api.mobs.MobRank;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Declarative authored mob definition after template expansion and override resolution.
 *
 * <p>This type is still declarative data. It is not the runtime-final mob profile.</p>
 */
public record ResolvedMobDefinition(
        ResourceLocation entity,
        @Nullable MobRank rank,
        @Nullable Integer level,
        @Nullable Integer baseExp,
        @Nullable Integer jobExp,
        @Nullable MobRoStatsBlock roStats,
        @Nullable MobDirectStatsBlock directStats,
        @Nullable String race,
        @Nullable String element,
        @Nullable Integer elementLevel,
        @Nullable String size,
        @Nullable Integer attackRange,
        @Nullable RagnarAiFlags ai,
        @Nullable RagnarMovementConfig movement,
        @Nullable RagnarLootBehavior lootBehavior,
        @Nullable RagnarMetamorphosis metamorphosis,
        @Nullable RagnarSpawnDefinition spawn) {
}
