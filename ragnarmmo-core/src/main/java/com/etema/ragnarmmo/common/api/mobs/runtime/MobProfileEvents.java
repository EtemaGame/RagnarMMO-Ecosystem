package com.etema.ragnarmmo.common.api.mobs.runtime;

import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileProvider;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileState;
import com.etema.ragnarmmo.common.api.mobs.query.MobConsumerDataOrigin;
import com.etema.ragnarmmo.common.api.mobs.query.MobConsumerInspectionStatsView;
import com.etema.ragnarmmo.common.api.mobs.query.MobConsumerReadView;
import com.etema.ragnarmmo.common.api.mobs.query.MobConsumerReadViewResolver;
import com.etema.ragnarmmo.core.RagnarMMOCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = RagnarMMOCore.MOD_ID)
public final class MobProfileEvents {
    static {
        MobConsumerReadViewResolver.register(MobProfileEvents::resolveReadView);
    }

    private MobProfileEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        MobProfileBootstrap.ensureInitialized(living);
    }

    private static Optional<MobConsumerReadView> resolveReadView(LivingEntity entity) {
        return MobProfileProvider.get(entity).resolve()
                .filter(MobProfileState::isInitialized)
                .map(MobProfileState::profile)
                .map(profile -> {
                    ResourceLocation entityTypeId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                    if (entityTypeId == null) {
                        entityTypeId = ResourceLocation.fromNamespaceAndPath("minecraft", "unknown");
                    }
                    return new MobConsumerReadView(
                            entityTypeId,
                            MobConsumerDataOrigin.NEW_RUNTIME_PROFILE,
                            profile.level(),
                            profile.rank(),
                            profile.race(),
                            profile.element(),
                            profile.elementLevel(),
                            profile.size(),
                            MobProfileBootstrap.isBossLike(entity),
                            true,
                            new MobConsumerInspectionStatsView(profile.maxHp(), profile.atkMin(), profile.atkMax(),
                                    profile.def(), profile.mdef()));
                });
    }
}
