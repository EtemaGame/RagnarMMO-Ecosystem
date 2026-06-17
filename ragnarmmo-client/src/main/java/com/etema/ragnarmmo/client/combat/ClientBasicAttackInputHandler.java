package com.etema.ragnarmmo.client.combat;

import com.etema.ragnarmmo.client.ClientCombatState;
import com.etema.ragnarmmo.client.RagnarMMOClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOClient.MOD_ID, value = Dist.CLIENT)
public final class ClientBasicAttackInputHandler {
    private static final String COMBAT_MOD_ID = "ragnarmmo_combat";
    private static final int MIN_PACKET_INTERVAL_TICKS = 1;

    private static int sequenceId;
    private static int localCooldownTicks;
    private static boolean nextDualWieldOffhand;

    private ClientBasicAttackInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !ModList.get().isLoaded(COMBAT_MOD_ID)) {
            return;
        }

        if (localCooldownTicks > 0) {
            localCooldownTicks--;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!canSendAutoAttack(minecraft)) {
            nextDualWieldOffhand = false;
            return;
        }

        if (localCooldownTicks > 0 || !(minecraft.hitResult instanceof EntityHitResult hit)) {
            return;
        }

        Entity target = hit.getEntity();
        boolean offHand = chooseOffhand(minecraft);
        CombatPacketBridge.sendBasicAttack(sequenceId++, target.getId(), offHand);
        localCooldownTicks = MIN_PACKET_INTERVAL_TICKS;
    }

    private static boolean canSendAutoAttack(Minecraft minecraft) {
        return minecraft != null
                && minecraft.level != null
                && minecraft.player != null
                && minecraft.screen == null
                && ClientCombatState.isCombatModeEnabled()
                && minecraft.options.keyAttack.isDown()
                && minecraft.player.isAlive()
                && !minecraft.player.isSpectator();
    }

    private static boolean chooseOffhand(Minecraft minecraft) {
        ItemStack offhand = minecraft.player.getOffhandItem();
        if (!isLegalOffhandAttack(offhand)) {
            nextDualWieldOffhand = false;
            return false;
        }
        boolean offHand = nextDualWieldOffhand;
        nextDualWieldOffhand = !nextDualWieldOffhand;
        return offHand;
    }

    private static boolean isLegalOffhandAttack(ItemStack stack) {
        return !stack.isEmpty()
                && !(stack.getItem() instanceof ShieldItem)
                && !(stack.getItem() instanceof ProjectileWeaponItem);
    }
}
