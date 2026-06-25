package com.etema.ragnarmmo.jobs.data;

import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.skills.api.ISkillDefinition;
import com.etema.ragnarmmo.skills.api.RagnarSkillDefinitionsAPI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SkillDefinitionRegistry {
    private static final String[] TREE_FILES = {
            "novice_1", "swordsman_1", "archer_1", "acolyte_1", "thief_1", "mage_1", "merchant_1"
    };

    private static final Map<ResourceLocation, SkillDefinition> SKILLS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Set<ResourceLocation>> TREE_SKILLS = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private SkillDefinitionRegistry() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (String file : TREE_FILES) {
            loadTree(loader, file);
        }
        RagnarSkillDefinitionsAPI.registerAccessor(id -> get(id).map(def -> (ISkillDefinition) def));
        bootstrapped = true;
    }

    public static Optional<SkillDefinition> get(ResourceLocation id) {
        bootstrap();
        return Optional.ofNullable(SKILLS.get(id));
    }

    public static boolean isAllowedForJob(ResourceLocation skillId, JobType currentJob) {
        bootstrap();
        if (skillId == null || currentJob == null) {
            return false;
        }
        SkillDefinition definition = SKILLS.get(skillId);
        if (definition == null) {
            return false;
        }
        if (definition.jobs().isEmpty() || "NOVICE".equalsIgnoreCase(definition.tier())) {
            return true;
        }
        for (JobType job : definition.jobs()) {
            if (currentJob.matchesExactOrAncestor(job)) {
                return true;
            }
        }
        return false;
    }

    public static List<ResourceLocation> getTreeSkillIds(String treeFile) {
        bootstrap();
        if (treeFile == null || treeFile.isBlank()) {
            return List.of();
        }
        ResourceLocation treeId = ResourceLocation.fromNamespaceAndPath("ragnarmmo", treeFile);
        Set<ResourceLocation> ids = TREE_SKILLS.get(treeId);
        return ids == null ? List.of() : List.copyOf(ids);
    }

    private static void loadTree(ClassLoader loader, String file) {
        String path = "data/ragnarmmo/skill_trees/" + file + ".json";
        ResourceLocation treeId = ResourceLocation.fromNamespaceAndPath("ragnarmmo", file);
        Set<ResourceLocation> ids = TREE_SKILLS.computeIfAbsent(treeId, key -> new LinkedHashSet<>());
        try (var in = loader.getResourceAsStream(path)) {
            if (in == null) {
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("skills") || !root.get("skills").isJsonArray()) {
                return;
            }
            for (var element : root.getAsJsonArray("skills")) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject node = element.getAsJsonObject();
                if (!node.has("id")) {
                    continue;
                }
                ResourceLocation id = ResourceLocation.tryParse(node.get("id").getAsString());
                if (id == null) {
                    continue;
                }
                ids.add(id);
                loadSkill(loader, id);
            }
        } catch (Exception ignored) {
        }
    }

    private static void loadSkill(ClassLoader loader, ResourceLocation id) {
        if (SKILLS.containsKey(id)) {
            return;
        }
        String path = "data/" + id.getNamespace() + "/skills/" + id.getPath() + ".json";
        try (var in = loader.getResourceAsStream(path)) {
            if (in == null) {
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject progression = root.has("progression") && root.get("progression").isJsonObject()
                    ? root.getAsJsonObject("progression")
                    : new JsonObject();
            int maxLevel = progression.has("max_level") ? progression.get("max_level").getAsInt() : 1;
            int upgradeCost = progression.has("upgrade_cost") ? progression.get("upgrade_cost").getAsInt() : 1;
            boolean canUpgrade = !progression.has("can_upgrade_with_points")
                    || progression.get("can_upgrade_with_points").getAsBoolean();
            int cooldownTicks = root.has("cooldown_ticks") ? root.get("cooldown_ticks").getAsInt() : 0;
            int castDelayTicks = root.has("cast_delay_ticks") ? root.get("cast_delay_ticks").getAsInt() : 0;
            JsonObject costs = root.has("costs") && root.get("costs").isJsonObject()
                    ? root.getAsJsonObject("costs")
                    : new JsonObject();
            int baseCost = costs.has("base_cost") ? costs.get("base_cost").getAsInt() : 0;
            int costPerLevel = costs.has("cost_per_level") ? costs.get("cost_per_level").getAsInt() : 0;
            String texture = "";
            if (root.has("ui") && root.get("ui").isJsonObject()) {
                JsonObject ui = root.getAsJsonObject("ui");
                if (ui.has("texture")) {
                    texture = ui.get("texture").getAsString();
                }
            }

            Map<ResourceLocation, Integer> requirements = new LinkedHashMap<>();
            if (root.has("requirements") && root.get("requirements").isJsonObject()) {
                for (var entry : root.getAsJsonObject("requirements").entrySet()) {
                    ResourceLocation requiredId = ResourceLocation.tryParse(entry.getKey());
                    if (requiredId != null) {
                        requirements.put(requiredId, Math.max(1, entry.getValue().getAsInt()));
                    }
                }
            }

            Map<String, Double> combatNumericDefaults = new LinkedHashMap<>();
            Map<String, String> combatStringDefaults = new LinkedHashMap<>();
            if (root.has("combat") && root.get("combat").isJsonObject()) {
                for (var valueEntry : root.getAsJsonObject("combat").entrySet()) {
                    if (valueEntry.getValue().isJsonPrimitive()
                            && valueEntry.getValue().getAsJsonPrimitive().isNumber()) {
                        combatNumericDefaults.put(valueEntry.getKey(), valueEntry.getValue().getAsDouble());
                    } else if (valueEntry.getValue().isJsonPrimitive()
                            && valueEntry.getValue().getAsJsonPrimitive().isString()) {
                        combatStringDefaults.put(valueEntry.getKey(), valueEntry.getValue().getAsString());
                    }
                }
            }

            Map<Integer, Map<String, Double>> levelData = new LinkedHashMap<>();
            Map<Integer, Map<String, String>> levelStringData = new LinkedHashMap<>();
            if (root.has("level_data") && root.get("level_data").isJsonObject()) {
                for (var levelEntry : root.getAsJsonObject("level_data").entrySet()) {
                    try {
                        int level = Integer.parseInt(levelEntry.getKey());
                        if (!levelEntry.getValue().isJsonObject()) {
                            continue;
                        }
                        Map<String, Double> values = new LinkedHashMap<>(combatNumericDefaults);
                        Map<String, String> stringValues = new LinkedHashMap<>(combatStringDefaults);
                        for (var valueEntry : levelEntry.getValue().getAsJsonObject().entrySet()) {
                            if (valueEntry.getValue().isJsonPrimitive()
                                    && valueEntry.getValue().getAsJsonPrimitive().isNumber()) {
                                values.put(valueEntry.getKey(), valueEntry.getValue().getAsDouble());
                            } else if (valueEntry.getValue().isJsonPrimitive()
                                    && valueEntry.getValue().getAsJsonPrimitive().isString()) {
                                stringValues.put(valueEntry.getKey(), valueEntry.getValue().getAsString());
                            }
                        }
                        levelData.put(level, Map.copyOf(values));
                        levelStringData.put(level, Map.copyOf(stringValues));
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if (!combatNumericDefaults.isEmpty() || !combatStringDefaults.isEmpty()) {
                levelData.put(1, Map.copyOf(combatNumericDefaults));
                levelStringData.put(1, Map.copyOf(combatStringDefaults));
            }

            Set<JobType> jobs = new LinkedHashSet<>();
            if (root.has("jobs") && root.get("jobs").isJsonArray()) {
                for (var jobElement : root.getAsJsonArray("jobs")) {
                    jobs.add(JobType.fromId(jobElement.getAsString()));
                }
            }

            SKILLS.put(id, new SkillDefinition(
                    id,
                    root.has("display_name") ? root.get("display_name").getAsString() : id.getPath(),
                    root.has("category") ? root.get("category").getAsString() : "",
                    root.has("tier") ? root.get("tier").getAsString() : "",
                    root.has("usage") ? root.get("usage").getAsString() : "",
                    Math.max(1, maxLevel),
                    Math.max(0, upgradeCost),
                    canUpgrade,
                    Math.max(0, cooldownTicks),
                    Math.max(0, castDelayTicks),
                    Math.max(0, baseCost),
                    Math.max(0, costPerLevel),
                    texture,
                    Map.copyOf(requirements),
                    Map.copyOf(levelData),
                    Map.copyOf(levelStringData),
                    Set.copyOf(jobs)));
        } catch (Exception ignored) {
        }
    }
}
