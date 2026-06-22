package com.etema.ragnarmmo.player.progression.event;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.core.RagnarMMOCore;
import com.etema.ragnarmmo.core.config.RagnarCoreConfigs;
import com.etema.ragnarmmo.player.progression.PlayerProgressionService;
import com.etema.ragnarmmo.player.stats.compute.StatResolutionService;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCore.MOD_ID)
public final class PlayerProgressionEvents {
    private PlayerProgressionEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        RagnarCoreAPI.get(player).ifPresent(stats -> {
            PlayerProgressionService service = PlayerProgressionService.forJobId(ResourceLocation.tryParse(stats.getJobId()));
            int baseLost = service.computeBaseDeathPenaltyLoss(stats.getExp());
            int jobLost = service.computeJobDeathPenaltyLoss(stats.getJobExp());
            if (baseLost <= 0 && jobLost <= 0) {
                return;
            }
            stats.setExp(Math.max(0, stats.getExp() - baseLost));
            stats.setJobExp(Math.max(0, stats.getJobExp() - jobLost));
            StatResolutionService.resolve(player, stats);
            PlayerStatsSyncService.sync(player, stats);
            player.sendSystemMessage(Component.translatable("message.ragnarmmo.exp_loss_death_dual", baseLost, jobLost));
        });
    }

    public static int applyBaseExpRate(ServerPlayer player, int rawExp) {
        return RagnarCoreAPI.get(player)
                .map(stats -> PlayerProgressionService.forJobId(ResourceLocation.tryParse(stats.getJobId()))
                        .applyBaseExpRate(rawExp))
                .orElseGet(() -> Math.max(1, (int) Math.round(rawExp * RagnarCoreConfigs.SERVER.progression.expGlobalMultiplier.get())));
    }
}
