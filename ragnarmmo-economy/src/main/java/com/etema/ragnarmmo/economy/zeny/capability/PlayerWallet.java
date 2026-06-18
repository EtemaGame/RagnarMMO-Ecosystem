package com.etema.ragnarmmo.economy.zeny.capability;

import com.etema.ragnarmmo.core.api.economy.CurrencyAccount;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerWallet implements CurrencyAccount, INBTSerializable<CompoundTag> {
    private long zeny;
    private boolean dirty = true;

    @Override
    public long balance() {
        return zeny;
    }

    @Override
    public void setBalance(long amount) {
        long clamped = Math.max(0L, amount);
        if (this.zeny != clamped) {
            this.zeny = clamped;
            this.dirty = true;
        }
    }

    public long getZeny() {
        return balance();
    }

    public void setZeny(long amount) {
        setBalance(amount);
    }

    public void addZeny(long amount) {
        credit(amount);
    }

    public boolean consumeZeny(long amount) {
        return debit(amount);
    }

    public boolean consumeDirty() {
        if (dirty) {
            dirty = false;
            return true;
        }
        return false;
    }

    public void markDirty() {
        dirty = true;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Zeny", zeny);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("Zeny")) {
            zeny = Math.max(0L, nbt.getLong("Zeny"));
        }
    }
}
