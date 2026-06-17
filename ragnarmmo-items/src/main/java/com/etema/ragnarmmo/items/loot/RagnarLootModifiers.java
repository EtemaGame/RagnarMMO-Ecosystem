package com.etema.ragnarmmo.items.loot;

import com.etema.ragnarmmo.items.LegacyRagnarItemIds;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RagnarLootModifiers {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    LegacyRagnarItemIds.LEGACY_MOD_ID);

    public static final RegistryObject<Codec<RagnarLootModifier>> SKILL_LOOT =
            SERIALIZERS.register("skill_loot", RagnarLootModifier::createCodec);
    public static final RegistryObject<Codec<RagnarLootModifier>> RAGNAR_LOOT =
            SERIALIZERS.register("ragnar_loot", RagnarLootModifier::createCodec);

    private RagnarLootModifiers() {
    }

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
