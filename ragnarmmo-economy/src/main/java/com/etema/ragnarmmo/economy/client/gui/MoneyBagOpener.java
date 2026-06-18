package com.etema.ragnarmmo.economy.client.gui;

import com.etema.ragnarmmo.economy.client.ui.MoneyBagScreen;
import net.minecraft.client.Minecraft;

public final class MoneyBagOpener {
    private MoneyBagOpener() {
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new MoneyBagScreen());
    }
}
