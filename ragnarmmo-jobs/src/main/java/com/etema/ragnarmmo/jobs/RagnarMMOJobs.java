package com.etema.ragnarmmo.jobs;

import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.jobs.command.JobClassCommands;
import com.etema.ragnarmmo.jobs.command.JobSkillCommands;
import com.etema.ragnarmmo.jobs.client.JobSkillsClientCache;
import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import com.etema.ragnarmmo.jobs.net.JobSkillsNetwork;
import com.etema.ragnarmmo.jobs.runtime.JobSkillEffectRegistry;
import com.etema.ragnarmmo.jobs.runtime.JobPassiveStatsContributor;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(RagnarMMOJobs.MOD_ID)
public final class RagnarMMOJobs {
    public static final String MOD_ID = "ragnarmmo_jobs";

    public RagnarMMOJobs() {
        SkillDefinitionRegistry.bootstrap();
        JobSkillEffectRegistry.bootstrapDefaults();
        DerivedStatsService.registerContributor(JobPassiveStatsContributor.INSTANCE);
        Network.registerPackets(JobSkillsNetwork::register);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> JobSkillsClientCache::registerApiHooks);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(JobClassCommands.createNode());
        event.getDispatcher().register(JobSkillCommands.createNode());
    }
}
