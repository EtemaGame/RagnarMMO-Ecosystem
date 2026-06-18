package com.etema.ragnarmmo.combat.client;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import com.etema.ragnarmmo.combat.hand.AttackHandResolver;
import com.etema.ragnarmmo.combat.timing.AttackCadenceCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.player.LocalPlayer;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID, value = Dist.CLIENT)
public final class ClientBasicAttackInputHandler {
    private static boolean wasAttackDown;
    private static int sequenceId;
    private static boolean nextDualWieldOffhand;
    private static int localAttackCooldownTicks;

    private ClientBasicAttackInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft == null || player == null || minecraft.level == null) {
            resetHoldState(true);
            return;
        }

        if (minecraft.screen != null || player.isSpectator() || !player.isAlive()) {
            resetHoldState(true);
            return;
        }

        boolean attackDown = minecraft.options.keyAttack.isDown();
        if (!attackDown) {
            resetHoldState(true);
            return;
        }

        if (!wasAttackDown) {
            sequenceId = Math.max(sequenceId, player.tickCount + 1);
        }
        wasAttackDown = true;

        if (localAttackCooldownTicks > 0) {
            localAttackCooldownTicks--;
        }

        if (AttackHandResolver.shouldResetCycle(player)) {
            nextDualWieldOffhand = false;
        }

        if (!(minecraft.hitResult instanceof EntityHitResult hitResult)) {
            return;
        }

        if (localAttackCooldownTicks > 0) {
            return;
        }

        boolean dualWielding = AttackHandResolver.isDualWielding(player);
        boolean offHand = dualWielding && nextDualWieldOffhand && AttackHandResolver.isValidAttackHand(player, true);
        int targetId = hitResult.getEntity().getId();
        minecraft.player.swing(offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        CombatPacketBridge.sendBasicAttack(sequenceId++, targetId, offHand);
        localAttackCooldownTicks = AttackCadenceCalculator.computeIntervalTicks(player, offHand);
        nextDualWieldOffhand = dualWielding ? AttackHandResolver.resolveNextHand(player, offHand) : false;
    }

    private static void resetHoldState(boolean resetCooldown) {
        wasAttackDown = false;
        nextDualWieldOffhand = false;
        if (resetCooldown) {
            localAttackCooldownTicks = 0;
        }
    }
}
