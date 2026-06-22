package com.etema.ragnarmmo.jobs;

import com.etema.ragnarmmo.RagnarMMO;
import com.etema.ragnarmmo.common.net.Network;
import com.etema.ragnarmmo.skills.api.RagnarSkillDefinitionsAPI;
import com.etema.ragnarmmo.jobs.client.JobSkillsClientCache;
import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import com.etema.ragnarmmo.jobs.net.JobSkillsNetwork;
import com.etema.ragnarmmo.jobs.runtime.JobSkillEffectRegistry;
import com.etema.ragnarmmo.jobs.runtime.JobPassiveStatsContributor;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class RagnarMMOJobs {
    public static final String MOD_ID = RagnarMMO.MOD_ID;

    private RagnarMMOJobs() {
    }

    public static void init() {
        SkillDefinitionRegistry.bootstrap();
        RagnarSkillDefinitionsAPI.registerAccessor(id -> SkillDefinitionRegistry.get(id).map(def -> (com.etema.ragnarmmo.skills.api.ISkillDefinition) def));
        JobSkillEffectRegistry.bootstrapDefaults();
        DerivedStatsService.registerContributor(JobPassiveStatsContributor.INSTANCE);
        Network.registerPackets(JobSkillsNetwork::register);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> JobSkillsClientCache::registerApiHooks);
    }
}
