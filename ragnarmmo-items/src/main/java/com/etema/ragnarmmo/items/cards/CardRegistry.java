package com.etema.ragnarmmo.items.cards;

import com.mojang.logging.LogUtils;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CardRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CardRegistry INSTANCE = new CardRegistry();

    private final Map<String, CardDefinition> byId = new ConcurrentHashMap<>();
    private final Map<String, List<CardDefinition>> byMob = new ConcurrentHashMap<>();

    private CardRegistry() {
    }

    public static CardRegistry getInstance() {
        return INSTANCE;
    }

    public void clear() {
        byId.clear();
        byMob.clear();
    }

    public void register(CardDefinition definition) {
        if (definition == null || definition.id() == null || definition.id().isBlank()) {
            return;
        }

        byId.put(definition.id(), definition);
        if (definition.mobId() != null && !definition.mobId().isBlank()) {
            byMob.computeIfAbsent(definition.mobId(), ignored -> new ArrayList<>()).add(definition);
        }
        LOGGER.debug("Registered card: {} (mob: {}, rate: {}, equipType: {})",
                definition.id(), definition.mobId(), definition.dropRate(), definition.equipType());
    }

    public int size() {
        return byId.size();
    }

    @Nullable
    public CardDefinition get(String cardId) {
        return byId.get(cardId);
    }

    public List<CardDefinition> getForMob(String mobId) {
        return byMob.getOrDefault(mobId, List.of());
    }

    @Nullable
    public CardDefinition rollDrop(String mobId, int luk, RandomSource random) {
        if (random == null) {
            return null;
        }

        List<CardDefinition> candidates = getForMob(mobId);
        if (candidates.isEmpty()) {
            return null;
        }

        double lukMultiplier = 1.0D + (Math.max(0, luk) * 0.01D);
        for (CardDefinition card : candidates) {
            double effectiveRate = card.dropRate() * lukMultiplier;
            if (random.nextDouble() < effectiveRate) {
                return card;
            }
        }
        return null;
    }
}
