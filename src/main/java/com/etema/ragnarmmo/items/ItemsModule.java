package com.etema.ragnarmmo.items;

import com.etema.ragnarmmo.items.cards.RagnarCardItems;
import com.etema.ragnarmmo.items.loot.RagnarLootModifiers;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ItemsModule {
    private ItemsModule() {
    }

    public static void init(IEventBus modBus) {
        UtilityItems.register(modBus);
        RagnarWeaponItems.register(modBus);
        RagnarCardItems.register(modBus);
        RagnarLootModifiers.register(modBus);
    }
}
