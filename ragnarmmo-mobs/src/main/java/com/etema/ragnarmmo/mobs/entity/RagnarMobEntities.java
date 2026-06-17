package com.etema.ragnarmmo.mobs.entity;

import com.etema.ragnarmmo.mobs.LegacyRagnarMobIds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RagnarMobEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LegacyRagnarMobIds.LEGACY_MOD_ID);

    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> PORING = register("poring", 0.7F, 0.5F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> POPORING = register("poporing", 0.7F, 0.5F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> DROP = register("drop", 0.7F, 0.5F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> MARIN = register("marin", 0.7F, 0.5F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> LUNATIC = register("lunatic", 0.6F, 0.8F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> FABRE = register("fabre", 0.5F, 0.35F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> PUPA = register("pupa", 0.45F, 0.45F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> MUKA = register("muka", 0.8F, 1.4F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> CREAMY = register("creamy", 0.6F, 0.45F);
    public static final RegistryObject<EntityType<SimpleRagnarMobEntity>> CREAMY_FEAR =
            register("creamy_fear", 0.75F, 0.55F);

    private RagnarMobEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    private static RegistryObject<EntityType<SimpleRagnarMobEntity>> register(String id, float width, float height) {
        return ENTITIES.register(id, () -> EntityType.Builder
                .of(SimpleRagnarMobEntity::new, MobCategory.CREATURE)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(LegacyRagnarMobIds.LEGACY_MOD_ID + ":" + id));
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        for (RegistryObject<EntityType<SimpleRagnarMobEntity>> entity : java.util.List.of(
                PORING, POPORING, DROP, MARIN, LUNATIC, FABRE, PUPA, MUKA, CREAMY, CREAMY_FEAR)) {
            event.put(entity.get(), SimpleRagnarMobEntity.createAttributes().build());
        }
    }
}
