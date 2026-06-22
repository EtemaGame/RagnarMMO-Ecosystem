package com.etema.ragnarmmo.combat.element;

import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileProvider;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileState;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class CombatPropertyResolver {
    private CombatPropertyResolver() {
    }

    public static ElementType getDefensiveElement(LivingEntity entity) {
        return getDefensiveElementProperty(entity).type();
    }

    public static int getDefensiveElementLevel(LivingEntity entity) {
        return getDefensiveElementProperty(entity).level();
    }

    public static ElementProperty getDefensiveElementProperty(LivingEntity entity) {
        return MobProfileProvider.get(entity).resolve()
                .filter(MobProfileState::isInitialized)
                .map(state -> new ElementProperty(parseElement(state.profile().element()), state.profile().elementLevel()))
                .orElse(new ElementProperty(ElementType.NEUTRAL, 1));
    }

    public static String getRace(LivingEntity entity) {
        return MobProfileProvider.get(entity).resolve()
                .filter(MobProfileState::isInitialized)
                .map(state -> state.profile().race())
                .orElse("unknown");
    }

    public static ElementType getAttackElement(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty() || weapon.getTag() == null) {
            return ElementType.NEUTRAL;
        }
        CompoundTag tag = weapon.getTag();
        String raw = firstString(tag, "ragnarmmo_element", "ro_element", "element");
        return parseElement(raw);
    }

    public static CombatMath.MobSize getEntitySize(LivingEntity entity) {
        if (entity == null) {
            return CombatMath.MobSize.MEDIUM;
        }
        var profiled = MobProfileProvider.get(entity).resolve()
                .filter(MobProfileState::isInitialized)
                .map(state -> parseSize(state.profile().size()));
        if (profiled.isPresent()) {
            return profiled.get();
        }
        float width = entity.getBbWidth();
        if (width < 0.7F) {
            return CombatMath.MobSize.SMALL;
        }
        if (width > 1.2F) {
            return CombatMath.MobSize.LARGE;
        }
        return CombatMath.MobSize.MEDIUM;
    }

    private static ElementType parseElement(String raw) {
        if (raw == null || raw.isBlank()) {
            return ElementType.NEUTRAL;
        }
        try {
            return ElementType.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return switch (raw.trim().toLowerCase(java.util.Locale.ROOT)) {
                case "shadow" -> ElementType.DARK;
                default -> ElementType.NEUTRAL;
            };
        }
    }

    private static CombatMath.MobSize parseSize(String raw) {
        if (raw == null || raw.isBlank()) {
            return CombatMath.MobSize.MEDIUM;
        }
        try {
            return CombatMath.MobSize.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return CombatMath.MobSize.MEDIUM;
        }
    }

    private static String firstString(CompoundTag tag, String... keys) {
        for (String key : keys) {
            if (tag.contains(key)) {
                String value = tag.getString(key);
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return "";
    }
}
