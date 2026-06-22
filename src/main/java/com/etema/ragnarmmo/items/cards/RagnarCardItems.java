package com.etema.ragnarmmo.items.cards;

import com.etema.ragnarmmo.items.LegacyRagnarItemIds;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RagnarCardItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LegacyRagnarItemIds.LEGACY_MOD_ID);

    public static final RegistryObject<Item> CARD = ITEMS.register("others/cards/card",
            () -> new CardItem(new Item.Properties().stacksTo(64)));

    private RagnarCardItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
