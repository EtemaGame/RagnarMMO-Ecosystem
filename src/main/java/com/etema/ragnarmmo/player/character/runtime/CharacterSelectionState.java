package com.etema.ragnarmmo.player.character.runtime;

import java.util.UUID;

public final class CharacterSelectionState {
    private UUID selectedCharacterId;
    private boolean selectionRequired = true;

    public UUID selectedCharacterId() {
        return selectedCharacterId;
    }

    public void selectedCharacterId(UUID selectedCharacterId) {
        this.selectedCharacterId = selectedCharacterId;
    }

    public boolean selectionRequired() {
        return selectionRequired;
    }

    public void selectionRequired(boolean selectionRequired) {
        this.selectionRequired = selectionRequired;
    }
}
