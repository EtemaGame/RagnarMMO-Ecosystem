package com.etema.ragnarmmo.items.data;

import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.items.cards.CardEquipType;
import com.etema.ragnarmmo.items.runtime.RoItemRuleResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RoItemRuleLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RagnarMMOItems.MOD_ID + "/RoItemRuleLoader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final RoItemRuleLoader INSTANCE = new RoItemRuleLoader();

    private final RoItemRuleSet ruleSet = new RoItemRuleSet();

    public RoItemRuleLoader() {
        super(GSON, "ro_item_rules");
    }

    public static RoItemRuleLoader getInstance() {
        return INSTANCE;
    }

    public static RoItemRuleSet getRuleSet() {
        return INSTANCE.ruleSet;
    }

    public static void applyClientSync(Map<ResourceLocation, RoItemRule> itemRules,
            Map<ResourceLocation, RoItemRule> tagRules,
            Map<String, Map<CardEquipType, RoItemRule>> modTypeRules) {
        INSTANCE.ruleSet.clear();
        itemRules.forEach(INSTANCE.ruleSet::addItemRule);
        tagRules.forEach(INSTANCE.ruleSet::addTagRule);
        modTypeRules.forEach((modId, rules) -> rules.forEach((type, rule) -> INSTANCE.ruleSet.addModTypeRule(modId, type, rule)));
        RoItemRuleResolver.clearCache();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        ruleSet.clear();
        RoItemRuleResolver.clearCache();

        resources.forEach((location, jsonElement) -> {
            if (!jsonElement.isJsonObject()) {
                return;
            }
            JsonObject root = jsonElement.getAsJsonObject();
            parseItemAndTagRules(location, root);
            parseModTypeRules(location, root);
        });

        LOGGER.info("Loaded {} RO item rules ({} by item, {} by tag, {} by mod/type)",
                ruleSet.getTotalRuleCount(),
                ruleSet.getItemRuleCount(),
                ruleSet.getTagRuleCount(),
                ruleSet.getModTypeRuleCount());
    }

    private void parseItemAndTagRules(ResourceLocation location, JsonObject root) {
        root.entrySet().forEach(entry -> {
            try {
                String key = entry.getKey();
                if ("modEquipmentTypes".equals(key) || !entry.getValue().isJsonObject()) {
                    return;
                }

                boolean tag = key.startsWith("#");
                ResourceLocation targetId = ResourceLocation.parse(tag ? key.substring(1) : key);
                RoItemRule rule = parseRule(entry.getValue().getAsJsonObject(), false);
                if (tag) {
                    ruleSet.addTagRule(targetId, rule);
                } else {
                    ruleSet.addItemRule(targetId, rule);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to parse RO item rule '{}' in {}: {}", entry.getKey(), location, e.getMessage());
            }
        });
    }

    private void parseModTypeRules(ResourceLocation location, JsonObject root) {
        if (!root.has("modEquipmentTypes") || !root.get("modEquipmentTypes").isJsonObject()) {
            return;
        }

        JsonObject modRules = root.getAsJsonObject("modEquipmentTypes");
        modRules.entrySet().forEach(modEntry -> {
            String modId = modEntry.getKey().trim().toLowerCase(Locale.ROOT);
            if (!modEntry.getValue().isJsonObject()) {
                return;
            }
            modEntry.getValue().getAsJsonObject().entrySet().forEach(typeEntry -> {
                try {
                    CardEquipType equipType = CardEquipType.fromString(typeEntry.getKey());
                    if (equipType == CardEquipType.ANY || !typeEntry.getValue().isJsonObject()) {
                        return;
                    }
                    ruleSet.addModTypeRule(modId, equipType, parseRule(typeEntry.getValue().getAsJsonObject(), true));
                } catch (Exception e) {
                    LOGGER.warn("Failed to parse RO mod/type rule '{}:{}' in {}: {}", modId, typeEntry.getKey(), location, e.getMessage());
                }
            });
        });
    }

    private RoItemRule parseRule(JsonObject json, boolean defaultShowTooltip) {
        Map<StatKeys, Integer> attributeBonuses = new EnumMap<>(StatKeys.class);
        if (json.has("attributeBonuses") && json.get("attributeBonuses").isJsonObject()) {
            JsonObject bonuses = json.getAsJsonObject("attributeBonuses");
            for (StatKeys stat : StatKeys.values()) {
                String statName = stat.name().toLowerCase(Locale.ROOT);
                if (bonuses.has(statName)) {
                    attributeBonuses.put(stat, bonuses.get(statName).getAsInt());
                }
            }
        }

        Set<JobType> allowedJobs = EnumSet.noneOf(JobType.class);
        if (json.has("allowedJobs") && json.get("allowedJobs").isJsonArray()) {
            json.getAsJsonArray("allowedJobs").forEach(element -> {
                try {
                    allowedJobs.add(JobType.valueOf(element.getAsString().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                }
            });
        }

        return new RoItemRule(
                getStringOrNull(json, "displayName"),
                attributeBonuses,
                getIntOrDefault(json, "requiredBaseLevel", 0),
                allowedJobs,
                getIntOrDefault(json, "cardSlots", 0),
                getBooleanOrDefault(json, "showTooltip", defaultShowTooltip),
                parseCombatProfile(json));
    }

    private RoCombatProfile parseCombatProfile(JsonObject json) {
        if (!json.has("combatProfile") || !json.get("combatProfile").isJsonObject()) {
            return RoCombatProfile.EMPTY;
        }

        JsonObject combat = json.getAsJsonObject("combatProfile");
        RoCombatProfile.WeaponMode weaponMode = RoCombatProfile.WeaponMode.fromString(getStringOrNull(combat, "weaponMode"));
        if (weaponMode == RoCombatProfile.WeaponMode.UNSPECIFIED && getBooleanOrDefault(combat, "ranged", false)) {
            weaponMode = RoCombatProfile.WeaponMode.RANGED;
        }

        return new RoCombatProfile(
                weaponMode,
                getDoubleOrDefault(combat, "atk", 0.0D),
                getDoubleOrDefault(combat, "matk", 0.0D),
                getIntOrDefault(combat, "aspd", 0),
                getDoubleOrDefault(combat, "range", 0.0D),
                getIntOrDefault(combat, "drawTicks", 0),
                (float) getDoubleOrDefault(combat, "projectileVelocity", 0.0D),
                getStringSet(combat, "atkAttributes"),
                getStringSet(combat, "matkAttributes"),
                getStringSet(combat, "aspdAttributes"),
                getStringSet(combat, "rangeAttributes"));
    }

    private static String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : null;
    }

    private static int getIntOrDefault(JsonObject json, String key, int defaultValue) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsInt() : defaultValue;
    }

    private static double getDoubleOrDefault(JsonObject json, String key, double defaultValue) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsDouble() : defaultValue;
    }

    private static boolean getBooleanOrDefault(JsonObject json, String key, boolean defaultValue) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsBoolean() : defaultValue;
    }

    private static Set<String> getStringSet(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return Set.of();
        }
        Set<String> values = new java.util.LinkedHashSet<>();
        json.getAsJsonArray(key).forEach(element -> {
            if (element.isJsonPrimitive()) {
                String value = element.getAsString().trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        });
        return values;
    }

    @Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID)
    public static class Events {
        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener(INSTANCE);
        }
    }
}
