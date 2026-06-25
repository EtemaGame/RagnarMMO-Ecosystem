package com.etema.ragnarmmo.combat.ground;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCombat.MOD_ID)
public final class GroundCellEvents {
    private GroundCellEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) {
            return;
        }
        for (var level : event.getServer().getAllLevels()) {
            GroundCellService.tick(level);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || event.getAmount() <= 0.0F) {
            return;
        }
        Entity direct = event.getSource().getDirectEntity();
        Entity source = event.getSource().getEntity();
        if (source instanceof ServerPlayer && direct == source) {
            return;
        }
        if (isRangedPhysical(direct) && GroundCellService.blocksPhysicalRanged(target)) {
            block(event);
            return;
        }
        if (isMeleePhysical(source, direct) && GroundCellService.consumePhysicalMeleeBlock(target)) {
            block(event);
        }
    }

    private static boolean isRangedPhysical(Entity direct) {
        return direct instanceof Projectile;
    }

    private static boolean isMeleePhysical(Entity source, Entity direct) {
        return source instanceof LivingEntity && !(direct instanceof Projectile);
    }

    private static void block(LivingHurtEvent event) {
        event.setCanceled(true);
        event.setAmount(0.0F);
    }
}
