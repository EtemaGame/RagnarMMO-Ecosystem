package com.etema.ragnarmmo.items.loot;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.items.cards.CardDefinition;
import com.etema.ragnarmmo.items.cards.CardItem;
import com.etema.ragnarmmo.items.cards.CardRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class RagnarLootModifier extends LootModifier {
    public static Codec<RagnarLootModifier> createCodec() {
        return RecordCodecBuilder.create(instance ->
                codecStart(instance).apply(instance, RagnarLootModifier::new));
    }

    public RagnarLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
            LootContext context) {
        Entity killer = context.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
        Entity killed = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(killer instanceof ServerPlayer player) || !(killed instanceof LivingEntity mob)) {
            return generatedLoot;
        }

        RagnarCoreAPI.get(player).ifPresent(stats -> {
            ResourceLocation mobKey = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
            if (mobKey == null) {
                return;
            }
            CardDefinition card = CardRegistry.getInstance().rollDrop(mobKey.toString(), stats.getLUK(),
                    context.getRandom());
            if (card != null) {
                generatedLoot.add(CardItem.createStack(card));
            }
        });
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return RagnarLootModifiers.SKILL_LOOT.get();
    }
}
