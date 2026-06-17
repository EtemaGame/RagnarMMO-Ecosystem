package com.etema.ragnarmmo.items.runtime;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public final class ZenyWalletHelper {
    public static final int COPPER_VALUE = com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.COPPER_VALUE;
    public static final int SILVER_VALUE = com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.SILVER_VALUE;
    public static final int GOLD_VALUE = com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.GOLD_VALUE;

    private ZenyWalletHelper() {
    }

    public static boolean isZeny(ItemStack stack) {
        return com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.isZeny(stack);
    }

    public static int getTotalZeny(ServerPlayer player) {
        return com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.getTotalZeny(player);
    }

    public static boolean tryConsume(ServerPlayer player, int amount) {
        return com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.tryConsume(player, amount);
    }

    public static void addZeny(ServerPlayer player, int amount) {
        com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.addZeny(player, amount);
    }

    public static String formatZeny(long amount) {
        return com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.formatZeny(amount);
    }

    public static int getValue(ItemStack stack) {
        return com.etema.ragnarmmo.economy.zeny.ZenyWalletHelper.getValue(stack);
    }
}
