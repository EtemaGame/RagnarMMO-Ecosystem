package com.etema.ragnarmmo.bestiary.data;

import com.etema.ragnarmmo.social.RagnarMMOSocial;
import com.etema.ragnarmmo.bestiary.api.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class BestiaryDetailsResolver {
    private static final int MAX_NATURAL_DROPS = 24;

    private BestiaryDetailsResolver() {
    }

    public static BestiaryEntryDetailsDto resolve(ResourceLocation entityId, BestiaryOverride override, MinecraftServer server) {
        String descriptionId = override != null ? override.descriptionId() : "";
        BestiarySpawnInfoDto spawn = override != null ? override.spawn().orElse(null) : null;
        List<BestiaryDropInfoDto> drops = new ArrayList<>();
        if (override != null) {
            drops.addAll(override.drops());
        }
        drops.addAll(resolveNaturalLootDrops(entityId, server));
        resolveZenyDrop(entityId).ifPresent(drops::add);

        return new BestiaryEntryDetailsDto(
                entityId,
                descriptionId,
                resolveStats(entityId),
                drops,
                spawn);
    }

    private static List<BestiaryDropInfoDto> resolveNaturalLootDrops(ResourceLocation entityId, MinecraftServer server) {
        if (server == null) {
            return List.of();
        }
        var type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        if (type == null) {
            return List.of();
        }
        ResourceLocation lootTableId = type.getDefaultLootTable();
        if (lootTableId == null || lootTableId.equals(net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY)) {
            return List.of();
        }

        LootTable table = server.getLootData().getLootTable(lootTableId);
        if (table == LootTable.EMPTY) {
            return List.of();
        }

        Set<ResourceLocation> itemIds = new LinkedHashSet<>();
        collectLootItems(server, lootTableId, table, itemIds, new LinkedHashSet<>(), 0);
        return itemIds.stream()
                .limit(MAX_NATURAL_DROPS)
                .map(itemId -> new BestiaryDropInfoDto(
                        itemId,
                        0,
                        0,
                        0.0D,
                        BestiaryDropSource.NATURAL_LOOT_TABLE,
                        "",
                        "gui.ragnarmmo.bestiary.drop.natural_loot"))
                .toList();
    }

    private static void collectLootItems(
            MinecraftServer server,
            ResourceLocation tableId,
            LootTable table,
            Set<ResourceLocation> itemIds,
            Set<ResourceLocation> visitedTables,
            int depth) {
        if (table == null || table == LootTable.EMPTY || depth > 4 || !visitedTables.add(tableId)) {
            return;
        }
        for (LootPool pool : reflectArray(table, "pools", LootPool.class)) {
            for (LootPoolEntryContainer entry : reflectArray(pool, "entries", LootPoolEntryContainer.class)) {
                collectLootEntry(server, entry, itemIds, visitedTables, depth);
            }
        }
    }

    private static void collectLootEntry(
            MinecraftServer server,
            LootPoolEntryContainer entry,
            Set<ResourceLocation> itemIds,
            Set<ResourceLocation> visitedTables,
            int depth) {
        if (entry instanceof LootItem) {
            Item item = reflectField(entry, "item", Item.class);
            ResourceLocation itemId = item != null ? ForgeRegistries.ITEMS.getKey(item) : null;
            if (itemId != null) {
                itemIds.add(itemId);
            }
            return;
        }
        if (entry instanceof LootTableReference) {
            ResourceLocation nestedId = reflectField(entry, "name", ResourceLocation.class);
            if (nestedId != null) {
                LootTable nested = server.getLootData().getLootTable(nestedId);
                collectLootItems(server, nestedId, nested, itemIds, visitedTables, depth + 1);
            }
            return;
        }

        for (LootPoolEntryContainer child : reflectArray(entry, "children", LootPoolEntryContainer.class)) {
            collectLootEntry(server, child, itemIds, visitedTables, depth + 1);
        }
    }

    private static <T> List<T> reflectArray(Object owner, String fieldName, Class<T> type) {
        Object value = reflectField(owner, fieldName, Object.class);
        if (value == null || !value.getClass().isArray()) {
            return List.of();
        }
        int length = java.lang.reflect.Array.getLength(value);
        List<T> out = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            Object item = java.lang.reflect.Array.get(value, i);
            if (type.isInstance(item)) {
                out.add(type.cast(item));
            }
        }
        return out;
    }

    private static <T> T reflectField(Object owner, String fieldName, Class<T> type) {
        Class<?> cls = owner.getClass();
        while (cls != null) {
            try {
                Field field = cls.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(owner);
                return type.isInstance(value) ? type.cast(value) : null;
            } catch (NoSuchFieldException ignored) {
                cls = cls.getSuperclass();
            } catch (ReflectiveOperationException | RuntimeException ex) {
                RagnarMMOSocial.LOGGER.debug("Unable to inspect loot field {} on {}: {}",
                        fieldName, owner.getClass().getName(), ex.getMessage());
                return null;
            }
        }
        return null;
    }

    private static BestiaryStatPreviewDto resolveStats(ResourceLocation entityId) {
        return null;
    }

    private static java.util.Optional<BestiaryDropInfoDto> resolveZenyDrop(ResourceLocation entityId) {
        var type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        if (type == null || MobRewardClassifier.classify(type) != MobRewardDisposition.REWARD_ELIGIBLE) {
            return java.util.Optional.empty();
        }
        ResourceLocation copperId = ResourceLocation.fromNamespaceAndPath(
                RagnarMMOSocial.LEGACY_NAMESPACE,
                "others/zeny/copper_zeny");
        if (!ForgeRegistries.ITEMS.containsKey(copperId)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new BestiaryDropInfoDto(
                copperId,
                0,
                0,
                0.0D,
                BestiaryDropSource.RAGNAR_ZENY,
                "Zeny",
                "gui.ragnarmmo.bestiary.drop.zeny_dynamic"));
    }
}
