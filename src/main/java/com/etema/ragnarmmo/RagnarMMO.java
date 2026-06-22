package com.etema.ragnarmmo;

import com.etema.ragnarmmo.combat.RagnarMMOCombat;
import com.etema.ragnarmmo.core.RagnarMMOCore;
import com.etema.ragnarmmo.items.RagnarMMOItems;
import com.etema.ragnarmmo.jobs.RagnarMMOJobs;
import com.etema.ragnarmmo.lifeskills.RagnarMMOLifeSkills;
import com.etema.ragnarmmo.mobs.RagnarMMOMobs;
import com.etema.ragnarmmo.social.RagnarMMOSocial;
import net.minecraftforge.fml.common.Mod;

@Mod(RagnarMMO.MOD_ID)
public final class RagnarMMO {
    public static final String MOD_ID = "ragnarmmo";

    public RagnarMMO() {
        RagnarMMOCore.init();
        RagnarMMOItems.init();
        RagnarMMOJobs.init();
        RagnarMMOCombat.init();
        RagnarMMOLifeSkills.init();
        RagnarMMOMobs.init();
        RagnarMMOSocial.init();
    }
}
