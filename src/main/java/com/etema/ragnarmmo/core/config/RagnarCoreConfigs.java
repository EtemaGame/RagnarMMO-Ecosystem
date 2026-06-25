package com.etema.ragnarmmo.core.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class RagnarCoreConfigs {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();
    }

    private RagnarCoreConfigs() {
    }

    public static final class Server {
        public final Progression progression;
        public final Caps caps;
        public final Logging logging;

        Server(ForgeConfigSpec.Builder builder) {
            this.progression = new Progression(builder);
            this.caps = new Caps(builder);
            this.logging = new Logging(builder);
        }
    }

    public static final class Progression {
        public final ForgeConfigSpec.DoubleValue expGlobalMultiplier;
        public final ForgeConfigSpec.DoubleValue jobExpGlobalMultiplier;
        public final ForgeConfigSpec.DoubleValue baseExpDeathPenaltyRate;
        public final ForgeConfigSpec.DoubleValue jobExpDeathPenaltyRate;
        public final ForgeConfigSpec.BooleanValue usePreRenewalStatPointCurve;
        public final ForgeConfigSpec.IntValue baseStatPoints;
        public final ForgeConfigSpec.IntValue pointsPerLevel;
        public final ForgeConfigSpec.DoubleValue skillToJobExpMultiplier;
        public final ForgeConfigSpec.DoubleValue skillToBaseExpMultiplier;

        Progression(ForgeConfigSpec.Builder builder) {
            builder.comment("RagnarMMO core progression settings").push("progression");
            expGlobalMultiplier = builder.defineInRange("exp_global_multiplier", 1.0, 0.01, 100.0);
            jobExpGlobalMultiplier = builder.defineInRange("job_exp_global_multiplier", 1.0, 0.01, 100.0);
            baseExpDeathPenaltyRate = builder.defineInRange("base_exp_death_penalty_rate", 0.05, 0.0, 1.0);
            jobExpDeathPenaltyRate = builder.defineInRange("job_exp_death_penalty_rate", 0.05, 0.0, 1.0);
            usePreRenewalStatPointCurve = builder.define("use_pre_renewal_stat_point_curve", true);
            skillToBaseExpMultiplier = builder.defineInRange("skill_to_base_exp_multiplier", 0.5, 0.0, 10.0);
            skillToJobExpMultiplier = builder.defineInRange("skill_to_job_exp_multiplier", 0.5, 0.0, 10.0);
            baseStatPoints = builder.defineInRange("base_stat_points", 48, 0, 500);
            pointsPerLevel = builder.defineInRange("points_per_level", 3, 0, 50);
            builder.pop();
        }
    }

    public static final class Caps {
        public final ForgeConfigSpec.IntValue maxLevel;
        public final ForgeConfigSpec.IntValue maxJobLevel;
        public final ForgeConfigSpec.IntValue maxStatValue;
        public final ForgeConfigSpec.IntValue noviceMaxLevel;
        public final ForgeConfigSpec.IntValue noviceMaxJobLevel;

        Caps(ForgeConfigSpec.Builder builder) {
            builder.comment("RagnarMMO core level and stat caps").push("caps");
            maxLevel = builder.defineInRange("max_level", 99, 1, 9999);
            maxJobLevel = builder.defineInRange("max_job_level", 50, 1, 9999);
            maxStatValue = builder.defineInRange("max_stat_value", 99, 1, 9999);
            noviceMaxLevel = builder.defineInRange("novice_max_level", 10, 1, 100);
            noviceMaxJobLevel = builder.defineInRange("novice_max_job_level", 10, 1, 100);
            builder.pop();
        }
    }

    public static final class Logging {
        public final ForgeConfigSpec.BooleanValue debug;
        public final ForgeConfigSpec.BooleanValue debugPlayerData;
        public final ForgeConfigSpec.IntValue warnRateLimitSeconds;

        Logging(ForgeConfigSpec.Builder builder) {
            builder.comment("RagnarMMO core logging and debug options").push("logging");
            debug = builder.define("debug", false);
            debugPlayerData = builder.define("debug_player_data", false);
            warnRateLimitSeconds = builder.defineInRange("warn_rate_limit_seconds", 60, 1, 3600);
            builder.pop();
        }
    }
}
