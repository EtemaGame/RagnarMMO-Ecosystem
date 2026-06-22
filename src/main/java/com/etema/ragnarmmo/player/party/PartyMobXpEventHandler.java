package com.etema.ragnarmmo.player.party;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileProvider;
import com.etema.ragnarmmo.common.api.mobs.capability.MobProfileState;
import com.etema.ragnarmmo.common.api.mobs.profile.MobProfile;
import com.etema.ragnarmmo.common.api.mobs.runtime.MobProfileBootstrap;
import com.etema.ragnarmmo.common.api.mobs.util.MobProfileEligibility;
import com.etema.ragnarmmo.core.config.RagnarCoreConfigs;
import com.etema.ragnarmmo.player.progression.PlayerProgressionService;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import com.etema.ragnarmmo.social.RagnarMMOSocial;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOSocial.MOD_ID)
public final class PartyMobXpEventHandler {
    private PartyMobXpEventHandler() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide() || event.getEntity() instanceof Player) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer) || killer.getServer() == null) {
            return;
        }
        LivingEntity killed = event.getEntity();
        if (!MobProfileEligibility.isEligible(killed) && !MobProfileBootstrap.isBossLike(killed)) {
            return;
        }

        MobProfile profile = MobProfileBootstrap.ensureInitialized(killed)
                .or(() -> MobProfileProvider.get(killed).resolve()
                        .filter(MobProfileState::isInitialized)
                        .map(MobProfileState::profile))
                .orElse(MobProfileState.defaultProfile());

        RagnarCoreAPI.get(killer).ifPresent(stats -> {
            int baseExp = applyLevelPenalty(Math.max(1, profile.baseExp()), Math.max(1, stats.getLevel()), profile.level(),
                    MobProfileBootstrap.isBossLike(killed));
            int jobExp = applyLevelPenalty(Math.max(1, profile.jobExp()), Math.max(1, stats.getLevel()), profile.level(),
                    MobProfileBootstrap.isBossLike(killed));
            PartyXpService.PartyXpAward partyAward = PartyXpService.distributeKillXp(killer, baseExp, jobExp, killer.getServer());
            PlayerProgressionService progression = PlayerProgressionService.forJobId(ResourceLocation.tryParse(stats.getJobId()));
            int awardedBase = progression.applyBaseExpRate(partyAward.baseExp());
            int awardedJob = progression.applyJobExpRate(partyAward.jobExp());
            int gained = stats.addExpAndProcessLevelUps(awardedBase,
                    RagnarCoreConfigs.SERVER.progression.pointsPerLevel.get(), progression::baseExpToNext);
            int jobGained = stats.addJobExpAndProcessLevelUps(awardedJob, progression::jobExpToNext);

            killer.sendSystemMessage(Component.translatable("message.ragnarmmo.exp_gain_dual",
                    awardedBase, awardedJob, killed.getDisplayName()));
            if (gained > 0) {
                killer.sendSystemMessage(Component.translatable("message.ragnarmmo.level_up", gained));
            }
            if (jobGained > 0) {
                killer.sendSystemMessage(Component.translatable("message.ragnarmmo.job_level_up", jobGained));
            }
            PlayerStatsSyncService.sync(killer, stats);
            PartyXpService.updatePartyMemberHud(killer);
        });
    }

    private static int applyLevelPenalty(int exp, int playerLevel, int mobLevel, boolean bossLike) {
        if (bossLike || exp <= 0) {
            return Math.max(1, exp);
        }
        int diff = mobLevel - playerLevel;
        double multiplier;
        if (diff >= 25) multiplier = 1.50D;
        else if (diff >= 20) multiplier = 1.30D;
        else if (diff >= 15) multiplier = 1.20D;
        else if (diff >= 10) multiplier = 1.10D;
        else if (diff >= 6) multiplier = 1.05D;
        else if (diff >= -5) multiplier = 1.00D;
        else if (diff >= -10) multiplier = 0.90D;
        else if (diff >= -15) multiplier = 0.70D;
        else if (diff >= -20) multiplier = 0.50D;
        else if (diff >= -25) multiplier = 0.30D;
        else if (diff >= -30) multiplier = 0.10D;
        else multiplier = 0.01D;
        return Math.max(1, (int) Math.round(exp * multiplier));
    }
}
