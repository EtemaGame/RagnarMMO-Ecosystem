package com.etema.ragnarmmo.economy.zeny.capability;

import com.etema.ragnarmmo.core.api.economy.CurrencyAccount;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerWallet implements CurrencyAccount, INBTSerializable<CompoundTag> {

    private long zeny = 0;
    private boolean isDirty = true; // Force sync on init

    @Override
    public long balance() {
        return zeny;
    }

    @Override
    public void setBalance(long amount) {
        if (this.zeny != amount) {
            this.zeny = Math.max(0, amount);
            this.isDirty = true;
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
        if (isDirty) {
            isDirty = false;
            return true;
        }
        return false;
    }

    public void markDirty() {
        this.isDirty = true;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Zeny", this.zeny);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("Zeny")) {
            this.zeny = nbt.getLong("Zeny");
        }
    }
}
