package com.etema.ragnarmmo.economy.client;

import com.etema.ragnarmmo.economy.zeny.capability.PlayerWalletProvider;
import com.etema.ragnarmmo.economy.zeny.network.WalletSyncPacket;
import net.minecraft.client.Minecraft;

public final class EconomyClientPacketHandler {
    private EconomyClientPacketHandler() {
    }

    public static void handleWalletSync(WalletSyncPacket msg) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        PlayerWalletProvider.get(minecraft.player).ifPresent(wallet -> wallet.setZeny(msg.zeny));
    }
}
