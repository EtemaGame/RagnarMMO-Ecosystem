package com.etema.ragnarmmo.player.character.data;

public final class CharacterNameValidator {
    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 16;

    private CharacterNameValidator() {
    }

    public static String normalize(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }

    public static boolean isValid(String raw) {
        String name = normalize(raw);
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != ' ' && c != '_') {
                return false;
            }
        }
        return true;
    }
}
