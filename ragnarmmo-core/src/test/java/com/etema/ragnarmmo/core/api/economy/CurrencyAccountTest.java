package com.etema.ragnarmmo.core.api.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CurrencyAccountTest {
    @Test
    void accountClampsAndTransfersThroughGenericContract() {
        TestAccount account = new TestAccount();

        account.setBalance(-10);
        assertEquals(0, account.balance());

        account.credit(25);
        assertEquals(25, account.balance());

        assertFalse(account.debit(30));
        assertEquals(25, account.balance());

        assertTrue(account.debit(10));
        assertEquals(15, account.balance());
    }

    private static final class TestAccount implements CurrencyAccount {
        private long balance;

        @Override
        public long balance() {
            return balance;
        }

        @Override
        public void setBalance(long amount) {
            balance = Math.max(0L, amount);
        }
    }
}
