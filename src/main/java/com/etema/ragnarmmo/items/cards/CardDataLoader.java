package com.etema.ragnarmmo.items.cards;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public final class CardDataLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final CardDataLoader INSTANCE = new CardDataLoader();

    private CardDataLoader() {
        super(GSON, "cards");
    }

    public static CardDataLoader getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
        CardRegistry registry = CardRegistry.getInstance();
        registry.clear();
        int count = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            try {
                CardDefinition definition = parse(entry.getKey(), entry.getValue().getAsJsonObject());
                registry.register(definition);
                count++;
            } catch (Exception ex) {
                LOGGER.error("Failed to load card definition from {}: {}", entry.getKey(), ex.getMessage());
            }
        }

        LOGGER.info("RagnarMMO Items: Loaded {} card definitions", count);
    }

    private static CardDefinition parse(ResourceLocation sourceId, JsonObject json) {
        String id = getString(json, "id", sourceId.getPath());
        String displayName = getString(json, "displayName", id);
        String mobId = getString(json, "mobId", "");
        double dropRate = json.has("dropRate") ? json.get("dropRate").getAsDouble() : 0.0D;
        CardEquipType equipType = json.has("equipType")
                ? CardEquipType.fromString(json.get("equipType").getAsString())
                : CardEquipType.ANY;
        String translationKey = getString(json, "translationKey", "card.ragnarmmo." + id + ".desc");
        int modelId = json.has("modelId") ? json.get("modelId").getAsInt() : 0;

        Map<String, Double> modifiers = new HashMap<>();
        if (json.has("modifiers") && json.get("modifiers").isJsonObject()) {
            JsonObject rawModifiers = json.getAsJsonObject("modifiers");
            for (Map.Entry<String, JsonElement> modifier : rawModifiers.entrySet()) {
                modifiers.put(modifier.getKey(), modifier.getValue().getAsDouble());
            }
        }

        return new CardDefinition(id, displayName, mobId, modifiers, dropRate, equipType, translationKey, modelId);
    }

    private static String getString(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }

    @Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(INSTANCE);
        }
    }
}
