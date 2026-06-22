package com.etema.ragnarmmo.items.hook;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.items.runtime.RangedWeaponStatsHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RangedWeaponSnapshotHook {
    private RangedWeaponSnapshotHook() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        if (!(arrow.getOwner() instanceof Player player)) {
            return;
        }
        if (arrow.getPersistentData().contains(RangedWeaponStatsHelper.SNAPSHOT_TAG)) {
            return;
        }

        ItemStack weapon = resolveRangedWeapon(player);
        if (weapon.isEmpty()) {
            return;
        }

        float drawRatio = RangedWeaponStatsHelper.estimateDrawRatio(arrow, weapon);
        RangedWeaponStatsHelper.snapshotArrow(arrow, player, weapon, drawRatio);
        arrow.setCritArrow(false);
    }

    private static ItemStack resolveRangedWeapon(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (RangedWeaponStatsHelper.supports(mainHand)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (RangedWeaponStatsHelper.supports(offHand)) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}
