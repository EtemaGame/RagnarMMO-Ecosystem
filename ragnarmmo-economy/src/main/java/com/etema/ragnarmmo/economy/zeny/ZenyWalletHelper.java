package com.etema.ragnarmmo.economy.zeny;

import com.etema.ragnarmmo.economy.zeny.capability.PlayerWalletProvider;
import com.etema.ragnarmmo.economy.zeny.network.EconomyNetwork;
import com.etema.ragnarmmo.economy.zeny.network.WalletSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ZenyWalletHelper {
    public static final int COPPER_VALUE = 1;
    public static final int SILVER_VALUE = 9;
    public static final int GOLD_VALUE = 81;

    private ZenyWalletHelper() {
    }

    public static boolean isZeny(ItemStack stack) {
        return stack.is(ZenyItems.COPPER_ZENY.get())
                || stack.is(ZenyItems.SILVER_ZENY.get())
                || stack.is(ZenyItems.GOLD_ZENY.get());
    }

    public static int getTotalZeny(ServerPlayer player) {
        return PlayerWalletProvider.get(player)
                .map(wallet -> (int) Math.min(Integer.MAX_VALUE, wallet.balance()))
                .orElse(0);
    }

    public static boolean tryConsume(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return true;
        }

        return PlayerWalletProvider.get(player)
                .map(wallet -> {
                    boolean success = wallet.debit(amount);
                    if (success) {
                        sync(player, wallet.balance());
                    }
                    return success;
                })
                .orElse(false);
    }

    public static void addZeny(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }

        PlayerWalletProvider.get(player).ifPresent(wallet -> {
            wallet.credit(amount);
            sync(player, wallet.balance());
        });
    }

    public static String formatZeny(long amount) {
        return amount + " Zeny";
    }

    public static int getValue(ItemStack stack) {
        if (stack.is(ZenyItems.GOLD_ZENY.get())) {
            return GOLD_VALUE;
        }
        if (stack.is(ZenyItems.SILVER_ZENY.get())) {
            return SILVER_VALUE;
        }
        if (stack.is(ZenyItems.COPPER_ZENY.get())) {
            return COPPER_VALUE;
        }
        return 0;
    }

    private static void sync(ServerPlayer player, long balance) {
        EconomyNetwork.registerOnce();
        com.etema.ragnarmmo.common.net.Network.sendToPlayer(player, new WalletSyncPacket(balance));
    }
}
