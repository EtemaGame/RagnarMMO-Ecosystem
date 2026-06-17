package com.etema.ragnarmmo.core.api.economy;

import java.util.Optional;
import java.util.UUID;

public interface CurrencyService {
    Optional<? extends CurrencyAccount> account(UUID ownerId);

    default long balance(UUID ownerId) {
        return account(ownerId).map(CurrencyAccount::balance).orElse(0L);
    }

    default CurrencyTransactionResult credit(UUID ownerId, long amount) {
        if (amount <= 0) {
            return CurrencyTransactionResult.rejected(balance(ownerId), "amount_must_be_positive");
        }
        Optional<? extends CurrencyAccount> account = account(ownerId);
        if (account.isEmpty()) {
            return CurrencyTransactionResult.rejected(0L, "account_not_found");
        }
        CurrencyAccount currencyAccount = account.get();
        currencyAccount.credit(amount);
        return CurrencyTransactionResult.accepted(currencyAccount.balance());
    }

    default CurrencyTransactionResult debit(UUID ownerId, long amount) {
        if (amount <= 0) {
            return CurrencyTransactionResult.accepted(balance(ownerId));
        }
        Optional<? extends CurrencyAccount> account = account(ownerId);
        if (account.isEmpty()) {
            return CurrencyTransactionResult.rejected(0L, "account_not_found");
        }
        CurrencyAccount currencyAccount = account.get();
        if (!currencyAccount.debit(amount)) {
            return CurrencyTransactionResult.rejected(currencyAccount.balance(), "insufficient_funds");
        }
        return CurrencyTransactionResult.accepted(currencyAccount.balance());
    }
}
