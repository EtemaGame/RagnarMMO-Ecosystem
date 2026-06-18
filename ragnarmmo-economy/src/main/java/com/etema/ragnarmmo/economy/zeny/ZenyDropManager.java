package com.etema.ragnarmmo.economy.zeny;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ZenyDropManager {
    private ZenyDropManager() {
    }

    public static List<ItemStack> calculateDrops(LivingEntity killed, Player killer, RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        int base = Math.max(1, Math.round(killed.getMaxHealth() / 2.0F));
        int luckBonus = killer != null ? Math.max(0, Math.round(killer.getLuck())) : 0;

        int copper = 1 + random.nextInt(Math.max(1, base + luckBonus));
        drops.add(new ItemStack(ZenyItems.COPPER_ZENY.get(), copper));

        if (random.nextDouble() < 0.15D + (luckBonus * 0.01D)) {
            drops.add(new ItemStack(ZenyItems.SILVER_ZENY.get(), 1 + random.nextInt(2)));
        }

        if (random.nextDouble() < 0.02D + (luckBonus * 0.002D)) {
            drops.add(new ItemStack(ZenyItems.GOLD_ZENY.get(), 1));
        }

        return drops;
    }
}
