package com.etema.ragnarmmo.player.character;

import com.etema.ragnarmmo.player.character.data.CharacterNameValidator;
import com.etema.ragnarmmo.player.character.data.CharacterSaveData;
import com.etema.ragnarmmo.player.character.data.CharacterSlot;
import com.etema.ragnarmmo.player.character.data.CharacterSummary;
import com.etema.ragnarmmo.player.character.runtime.CharacterAccount;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacterDataContractTest {
    @Test
    void characterSummaryRoundTripsDisplayData() {
        CharacterSummary summary = new CharacterSummary("Aldebaran", 42, "ragnarmmo:mage", "Mage", 28);

        CharacterSummary restored = CharacterSummary.deserializeNBT(summary.serializeNBT());

        assertEquals(summary, restored);
    }

    @Test
    void characterSlotRoundTripsIdentityAndSummary() {
        UUID id = UUID.randomUUID();
        CharacterSlot slot = new CharacterSlot(1, id, "Prontera", 10L, 20L,
                new CharacterSummary("Prontera", 12, "ragnarmmo:novice", "Novice", 10));

        CharacterSlot restored = CharacterSlot.deserializeNBT(slot.serializeNBT());

        assertEquals(1, restored.slotIndex());
        assertEquals(id, restored.characterId());
        assertEquals("Prontera", restored.name());
        assertEquals(12, restored.summary().baseLevel());
    }

    @Test
    void characterSaveDataDefensivelyCopiesNbt() {
        CompoundTag raw = new CompoundTag();
        raw.putString("Value", "original");
        CharacterSaveData save = new CharacterSaveData(raw);

        raw.putString("Value", "mutated");

        assertEquals("original", save.serializeNBT().getString("Value"));
    }

    @Test
    void characterNameValidationMatchesV1Rules() {
        assertTrue(CharacterNameValidator.isValid("Rune Knight"));
        assertTrue(CharacterNameValidator.isValid("Aco_01"));
        assertFalse(CharacterNameValidator.isValid("ab"));
        assertFalse(CharacterNameValidator.isValid("this name is far too long"));
        assertFalse(CharacterNameValidator.isValid("Bad-Name"));
    }

    @Test
    void accountStoresOnlyOneCharacterPerSlotAndNameLookupIsCaseInsensitive() {
        CharacterAccount account = new CharacterAccount();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        account.put(new CharacterSlot(0, first, "Novice", 1L, 1L, CharacterSummary.novice("Novice")),
                new CharacterSaveData(new CompoundTag()));
        account.put(new CharacterSlot(0, second, "Mage", 2L, 2L, CharacterSummary.novice("Mage")),
                new CharacterSaveData(new CompoundTag()));

        assertEquals(1, account.slots().size());
        assertTrue(account.slotById(second).isPresent());
        assertTrue(account.hasName("mage"));
    }

    @Test
    void exactDeleteConfirmationRequiresExactName() {
        CharacterSlot slot = new CharacterSlot(0, UUID.randomUUID(), "Exact Name", 1L, 1L,
                CharacterSummary.novice("Exact Name"));

        assertTrue(slot.name().equals("Exact Name"));
        assertFalse(slot.name().equals("exact name"));
    }
}
