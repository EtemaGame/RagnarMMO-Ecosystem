package com.etema.ragnarmmo.items.cards;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class CardItem extends Item {
    public static final String TAG_CARD_ID = "card_id";
    public static final String TAG_CARD_NAME = "card_name";
    public static final String TAG_CARD_MOB = "card_mob";
    public static final String TAG_CARD_MODIFIERS = "card_modifiers";
    public static final String TAG_CARD_EQUIP_TYPE = "card_equip_type";
    public static final String TAG_CARD_DESC = "card_desc";

    public CardItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createStack(CardDefinition definition) {
        ItemStack stack = new ItemStack(RagnarCardItems.CARD.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_CARD_ID, definition.id());
        tag.putString(TAG_CARD_NAME, definition.displayName());
        tag.putString(TAG_CARD_MOB, definition.mobId());
        tag.putString(TAG_CARD_EQUIP_TYPE, definition.equipType().name());
        tag.putString(TAG_CARD_DESC, definition.translationKey());

        CompoundTag modifiers = new CompoundTag();
        for (Map.Entry<String, Double> entry : definition.modifiers().entrySet()) {
            modifiers.putDouble(entry.getKey(), entry.getValue());
        }
        tag.put(TAG_CARD_MODIFIERS, modifiers);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CARD_ID)) {
            return;
        }
        tooltip.add(Component.literal(tag.getString(TAG_CARD_NAME)).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal(tag.getString(TAG_CARD_MOB)).withStyle(ChatFormatting.GRAY));
        if (tag.contains(TAG_CARD_EQUIP_TYPE)) {
            tooltip.add(Component.literal(CardEquipType.fromString(tag.getString(TAG_CARD_EQUIP_TYPE)).displayName())
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_CARD_NAME) && !tag.getString(TAG_CARD_NAME).isBlank()) {
            return Component.literal(tag.getString(TAG_CARD_NAME)).withStyle(ChatFormatting.YELLOW);
        }
        return super.getName(stack);
    }

    @Nullable
    public static String getCardId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_CARD_ID) ? tag.getString(TAG_CARD_ID) : null;
    }
}
