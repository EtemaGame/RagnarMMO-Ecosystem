package com.etema.ragnarmmo.items.runtime;

import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.items.cards.CardEquipType;
import com.etema.ragnarmmo.items.cards.CardRegistry;
import com.etema.ragnarmmo.items.data.RoItemRule;
import com.etema.ragnarmmo.items.data.RoItemRuleLoader;
import com.etema.ragnarmmo.items.data.RoItemRuleSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RoItemRuleResolver {
    private static final Map<Item, RoItemRule> CACHE = new ConcurrentHashMap<>();

    private RoItemRuleResolver() {
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static RoItemRule resolve(Item item) {
        if (item == null) {
            return RoItemRule.EMPTY;
        }
        return CACHE.computeIfAbsent(item, RoItemRuleResolver::computeRule);
    }

    public static RoItemRule resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return RoItemRule.EMPTY;
        }
        RoItemRule baseRule = resolve(stack.getItem());
        java.util.List<String> slottedCards = RoItemNbtHelper.getSlottedCards(stack);
        if (slottedCards.isEmpty()) {
            return baseRule;
        }

        Map<StatKeys, Integer> mergedBonuses = new EnumMap<>(StatKeys.class);
        mergedBonuses.putAll(baseRule.attributeBonuses());
        for (String cardId : slottedCards) {
            var cardDefinition = CardRegistry.getInstance().get(cardId);
            if (cardDefinition == null || cardDefinition.modifiers() == null) {
                continue;
            }
            cardDefinition.modifiers().forEach((attributeId, value) ->
                    mergeStatBonus(mergedBonuses, attributeId, value));
        }

        String displayName = baseRule.displayName() != null
                ? baseRule.displayName()
                : stack.getHoverName().getString();
        return new RoItemRule(
                displayName,
                mergedBonuses,
                baseRule.requiredBaseLevel(),
                baseRule.allowedJobs(),
                baseRule.cardSlots(),
                baseRule.showTooltip(),
                baseRule.combatProfile());
    }

    public static boolean hasRule(Item item) {
        RoItemRule rule = resolve(item);
        return rule != null && !rule.isEmpty();
    }

    public static int getCacheSize() {
        return CACHE.size();
    }

    private static RoItemRule computeRule(Item item) {
        RoItemRuleSet ruleSet = RoItemRuleLoader.getRuleSet();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) {
            return RoItemRule.EMPTY;
        }

        CardEquipType equipType = RoEquipmentTypeResolver.resolve(new ItemStack(item));
        RoItemRule resolved = RoItemRule.EMPTY;

        if (equipType != CardEquipType.ANY) {
            RoItemRule byModAndType = ruleSet.getByModAndType(itemId.getNamespace(), equipType);
            if (byModAndType != null) {
                resolved = RoItemRule.merge(resolved, byModAndType);
            }
        }

        for (Map.Entry<ResourceLocation, RoItemRule> entry : ruleSet.getTagRules().entrySet()) {
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, entry.getKey());
            if (new ItemStack(item).is(tagKey)) {
                resolved = RoItemRule.merge(resolved, entry.getValue());
                break;
            }
        }

        RoItemRule byId = ruleSet.getByItemId(itemId);
        if (byId != null) {
            resolved = RoItemRule.merge(resolved, byId);
        }
        return resolved != null ? resolved : RoItemRule.EMPTY;
    }

    private static void mergeStatBonus(Map<StatKeys, Integer> bonuses, String attributeId, double value) {
        if (attributeId == null || !attributeId.startsWith("ragnarmmo:")) {
            return;
        }
        try {
            String statName = attributeId.substring("ragnarmmo:".length()).toUpperCase(java.util.Locale.ROOT);
            StatKeys statKey = StatKeys.valueOf(statName);
            int amount = (int) Math.round(value);
            bonuses.put(statKey, bonuses.getOrDefault(statKey, 0) + amount);
        } catch (IllegalArgumentException ignored) {
            // Non-stat card modifiers are consumed by combat/social systems later.
        }
    }
}
