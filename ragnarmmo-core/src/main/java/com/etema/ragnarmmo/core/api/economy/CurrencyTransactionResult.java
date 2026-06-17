package com.etema.ragnarmmo.core.api.economy;

public record CurrencyTransactionResult(boolean accepted, long balance, String reason) {
    public static CurrencyTransactionResult accepted(long balance) {
        return new CurrencyTransactionResult(true, Math.max(0L, balance), "accepted");
    }

    public static CurrencyTransactionResult rejected(long balance, String reason) {
        return new CurrencyTransactionResult(false, Math.max(0L, balance),
                reason == null || reason.isBlank() ? "rejected" : reason);
    }
}
