package com.etema.ragnarmmo.items.equipment;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class RagnarEquipmentTags {
    public static final TagKey<Item> MID_HEAD_EQUIPPABLE = itemTag("mid_head_equippable");
    public static final TagKey<Item> ACCESSORY_EQUIPPABLE = itemTag("accessory_equippable");

    private RagnarEquipmentTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(RagnarMMOItems.MOD_ID, path));
    }
}
