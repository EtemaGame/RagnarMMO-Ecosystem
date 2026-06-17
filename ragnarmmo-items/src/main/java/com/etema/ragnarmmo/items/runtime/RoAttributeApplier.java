package com.etema.ragnarmmo.items.runtime;

import com.etema.ragnarmmo.common.api.attributes.RagnarAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.items.data.RoItemRule;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.RegistryObject;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RoAttributeApplier {
    public static final EquipmentSlot[] TRACKED_SLOTS = {
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private static final Map<StatKeys, RegistryObject<Attribute>> STAT_TO_ATTR = new EnumMap<>(StatKeys.class);
    private static final Map<String, UUID> UUID_CACHE = new ConcurrentHashMap<>();

    static {
        STAT_TO_ATTR.put(StatKeys.STR, RagnarAttributes.STR);
        STAT_TO_ATTR.put(StatKeys.AGI, RagnarAttributes.AGI);
        STAT_TO_ATTR.put(StatKeys.VIT, RagnarAttributes.VIT);
        STAT_TO_ATTR.put(StatKeys.INT, RagnarAttributes.INT);
        STAT_TO_ATTR.put(StatKeys.DEX, RagnarAttributes.DEX);
        STAT_TO_ATTR.put(StatKeys.LUK, RagnarAttributes.LUK);
    }

    private RoAttributeApplier() {
    }

    public static UUID getModifierUUID(EquipmentSlot slot, StatKeys stat) {
        String key = "ragnarmmo_items:" + slot.name() + "_" + stat.name();
        return UUID_CACHE.computeIfAbsent(key, value -> UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static void applySlotBonuses(Player player, EquipmentSlot slot, RoItemRule rule, boolean meetsRequirements) {
        if (player == null || slot == null) {
            return;
        }

        for (StatKeys stat : StatKeys.values()) {
            RegistryObject<Attribute> attribute = STAT_TO_ATTR.get(stat);
            if (attribute == null || !attribute.isPresent()) {
                continue;
            }
            AttributeInstance instance = player.getAttribute(attribute.get());
            if (instance == null) {
                continue;
            }

            UUID modifierId = getModifierUUID(slot, stat);
            instance.removeModifier(modifierId);

            double amount = 0.0D;
            if (meetsRequirements && rule != null && rule.hasAttributeBonuses()) {
                amount = rule.getBonus(stat);
            }
            if (amount != 0.0D) {
                instance.addTransientModifier(new AttributeModifier(
                        modifierId,
                        "RoItem_" + slot.name() + "_" + stat.name(),
                        amount,
                        AttributeModifier.Operation.ADDITION));
            }
        }
    }

    public static void clearAllBonuses(Player player) {
        if (player == null) {
            return;
        }
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            applySlotBonuses(player, slot, RoItemRule.EMPTY, false);
        }
    }

    public static void refreshAllSlots(Player player) {
        if (player == null) {
            return;
        }
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            refreshSlot(player, slot);
        }
    }

    public static void refreshSlot(Player player, EquipmentSlot slot) {
        if (player == null || slot == null) {
            return;
        }
        var stack = player.getItemBySlot(slot);
        RoItemRule rule = stack.isEmpty() ? RoItemRule.EMPTY : RoItemRuleResolver.resolve(stack);
        applySlotBonuses(player, slot, rule, RoRequirementChecker.meetsRequirements(player, rule));
    }

    public static int getTotalBonus(Player player, StatKeys stat) {
        if (player == null || stat == null) {
            return 0;
        }
        int total = 0;
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            var stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                RoItemRule rule = RoItemRuleResolver.resolve(stack);
                if (RoRequirementChecker.meetsRequirements(player, rule)) {
                    total += rule.getBonus(stat);
                }
            }
        }
        return total;
    }
}
