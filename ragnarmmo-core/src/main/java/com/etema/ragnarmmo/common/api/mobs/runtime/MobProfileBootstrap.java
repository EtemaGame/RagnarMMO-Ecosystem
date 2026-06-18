package com.etema.ragnarmmo.common.api.mobs.runtime;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.mobs.MobRank;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileProvider;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileState;
import com.etema.ragnarmmo.common.api.mobs.data.ResolvedMobDefinition;
import com.etema.ragnarmmo.common.api.mobs.data.load.MobDefinitionRegistry;
import com.etema.ragnarmmo.common.api.mobs.data.resolve.MobDefinitionResolver;
import com.etema.ragnarmmo.common.api.mobs.profile.MobProfile;
import com.etema.ragnarmmo.common.api.mobs.profile.MobProfileFactory;
import com.etema.ragnarmmo.common.api.mobs.util.MobProfileEligibility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public final class MobProfileBootstrap {
    private static final MobProfileFactory FACTORY = new MobProfileFactory();

    private MobProfileBootstrap() {
    }

    public static Optional<MobProfile> ensureInitialized(LivingEntity living) {
        return MobProfileProvider.get(living).resolve().flatMap(state -> ensureInitialized(living, state));
    }

    public static Optional<MobProfile> ensureInitialized(LivingEntity living, MobProfileState state) {
        if (living == null || living.level().isClientSide()) {
            return Optional.empty();
        }
        MobProfileEligibility.Classification classification = MobProfileEligibility.classify(living);
        if (classification == MobProfileEligibility.Classification.INELIGIBLE) {
            state.clearProfile();
            return Optional.empty();
        }
        if (state.isInitialized()) {
            return Optional.of(state.profile());
        }

        Optional<ResolvedMobDefinition> definition = resolveDefinition(living);
        MobRank rank = definition.map(ResolvedMobDefinition::rank)
                .filter(value -> value != null)
                .orElseGet(() -> defaultRank(living));
        int level = definition.map(ResolvedMobDefinition::level)
                .filter(value -> value != null && value > 0)
                .orElseGet(() -> defaultLevel(living));
        MobProfile profile = FACTORY.create(level, rank, definition);
        state.setProfile(profile);
        MobAttributeHelper.applyAttributes(living, profile);
        return Optional.of(profile);
    }

    private static Optional<ResolvedMobDefinition> resolveDefinition(LivingEntity living) {
        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType());
        if (typeId == null) {
            return Optional.empty();
        }
        var registry = MobDefinitionRegistry.getInstance();
        return registry.getDefinition(typeId)
                .map(definition -> MobDefinitionResolver.resolve(definition,
                        definition.template() == null ? null : registry.getTemplate(definition.template()).orElse(null)).definition());
    }

    private static MobRank defaultRank(LivingEntity living) {
        if (isVanillaBoss(living)) {
            return MobRank.BOSS;
        }
        return living.getType().getCategory() == MobCategory.MONSTER ? MobRank.NORMAL : MobRank.NORMAL;
    }

    private static int defaultLevel(LivingEntity living) {
        if (living.level() instanceof ServerLevel level) {
            Player nearest = level.getNearestPlayer(living, 64.0D);
            if (nearest instanceof ServerPlayer player) {
                return RagnarCoreAPI.get(player).map(stats -> Math.max(1, stats.getLevel())).orElse(1);
            }
        }
        return isVanillaBoss(living) ? 80 : 1;
    }

    public static boolean isBossLike(LivingEntity living) {
        return isVanillaBoss(living) || MobProfileProvider.get(living).resolve()
                .filter(MobProfileState::isInitialized)
                .map(state -> state.profile().rank() == MobRank.BOSS)
                .orElse(false);
    }

    private static boolean isVanillaBoss(LivingEntity living) {
        return living instanceof WitherBoss || living instanceof EnderDragon;
    }
}
