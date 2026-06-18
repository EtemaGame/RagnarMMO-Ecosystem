package com.etema.ragnarmmo.economy.zeny;

import com.etema.ragnarmmo.economy.LegacyRagnarEconomyIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ZenyItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LegacyRagnarEconomyIds.LEGACY_MOD_ID);

    public static final RegistryObject<Item> COPPER_ZENY = ITEMS.register("others/zeny/copper_zeny",
            () -> new ZenyItem(new Item.Properties().stacksTo(64), 1));
    public static final RegistryObject<Item> SILVER_ZENY = ITEMS.register("others/zeny/silver_zeny",
            () -> new ZenyItem(new Item.Properties().stacksTo(64), 9));
    public static final RegistryObject<Item> GOLD_ZENY = ITEMS.register("others/zeny/gold_zeny",
            () -> new ZenyItem(new Item.Properties().stacksTo(64), 81));
    public static final RegistryObject<Item> MONEY_BAG = ITEMS.register("others/zeny/money_bag",
            () -> new MoneyBagItem(new Item.Properties().stacksTo(1)));

    private ZenyItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static int getValue(ItemStack stack) {
        if (stack.is(COPPER_ZENY.get())) {
            return 1;
        }
        if (stack.is(SILVER_ZENY.get())) {
            return 9;
        }
        if (stack.is(GOLD_ZENY.get())) {
            return 81;
        }
        return 0;
    }

    private static final class ZenyItem extends Item {
        private final int value;

        private ZenyItem(Properties properties, int value) {
            super(properties);
            this.value = value;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal(value + " zeny compatibility value").withStyle(ChatFormatting.GRAY));
        }
    }
}
