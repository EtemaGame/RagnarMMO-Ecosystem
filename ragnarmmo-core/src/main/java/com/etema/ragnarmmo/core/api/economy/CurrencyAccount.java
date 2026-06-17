package com.etema.ragnarmmo.core.api.economy;

/**
 * Minimal account contract used by non-economy modules.
 */
public interface CurrencyAccount {
    long balance();

    void setBalance(long amount);

    default void credit(long amount) {
        if (amount > 0) {
            setBalance(balance() + amount);
        }
    }

    default boolean debit(long amount) {
        if (amount <= 0) {
            return true;
        }
        long current = balance();
        if (current < amount) {
            return false;
        }
        setBalance(current - amount);
        return true;
    }
}
