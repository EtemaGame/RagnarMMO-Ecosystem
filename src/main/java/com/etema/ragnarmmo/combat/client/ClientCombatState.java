package com.etema.ragnarmmo.combat.client;

public final class ClientCombatState {
    private static boolean combatModeEnabled = true;

    private ClientCombatState() {
    }

    public static boolean isCombatModeEnabled() {
        return combatModeEnabled;
    }

    public static void toggleCombatMode() {
        combatModeEnabled = !combatModeEnabled;
    }
}
