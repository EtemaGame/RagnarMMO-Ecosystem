package com.etema.ragnarmmo.player.character.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class CharacterAccountStore extends SavedData {
    private static final String ID = "ragnarmmo_characters";

    private final Map<UUID, CharacterAccount> accounts = new HashMap<>();

    public static CharacterAccountStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                CharacterAccountStore::load,
                CharacterAccountStore::new,
                ID);
    }

    public CharacterAccount account(UUID playerId) {
        return accounts.computeIfAbsent(playerId, ignored -> new CharacterAccount());
    }

    public static CharacterAccountStore load(CompoundTag tag) {
        CharacterAccountStore store = new CharacterAccountStore();
        CompoundTag accountsTag = tag.getCompound("Accounts");
        for (String key : accountsTag.getAllKeys()) {
            try {
                store.accounts.put(UUID.fromString(key), CharacterAccount.deserializeNBT(accountsTag.getCompound(key)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return store;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag accountsTag = new CompoundTag();
        accounts.forEach((id, account) -> accountsTag.put(id.toString(), account.serializeNBT()));
        tag.put("Accounts", accountsTag);
        return tag;
    }
}
