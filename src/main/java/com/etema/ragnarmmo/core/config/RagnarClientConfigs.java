package com.etema.ragnarmmo.core.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class RagnarClientConfigs {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    private RagnarClientConfigs() {
    }

    public static final class Client {
        public final Hud hud;

        Client(ForgeConfigSpec.Builder builder) {
            this.hud = new Hud(builder);
        }
    }

    public static final class Hud {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue replaceVanillaSurvivalBars;
        public final HudComponent status;
        public final HudComponent partyFrame;
        public final HudComponent skillHotbar;
        public final HudComponent notifications;

        Hud(ForgeConfigSpec.Builder builder) {
            builder.comment("Client HUD overlay configuration").push("hud");
            enabled = builder.define("enabled", true);
            replaceVanillaSurvivalBars = builder.define("replace_vanilla_survival_bars", true);
            status = new HudComponent(builder, "status", 0.5D, 1.0D, 10);
            partyFrame = new HudComponent(builder, "party_frame", 0.0D, 0.12D, 20);
            skillHotbar = new HudComponent(builder, "skill_hotbar", 0.5D, 0.78D, 30);
            notifications = new HudComponent(builder, "notifications", 1.0D, 0.18D, 40);
            builder.pop();
        }

        public static final class HudComponent {
            public final ForgeConfigSpec.DoubleValue anchorX;
            public final ForgeConfigSpec.DoubleValue anchorY;
            public final ForgeConfigSpec.BooleanValue enabled;
            public final ForgeConfigSpec.DoubleValue scale;
            public final ForgeConfigSpec.IntValue backgroundAlpha;
            public final ForgeConfigSpec.BooleanValue showBackground;
            public final ForgeConfigSpec.IntValue zOrder;

            HudComponent(ForgeConfigSpec.Builder builder, String name, double x, double y, int z) {
                builder.push(name);
                anchorX = builder.defineInRange("anchor_x", x, 0.0D, 1.0D);
                anchorY = builder.defineInRange("anchor_y", y, 0.0D, 1.0D);
                enabled = builder.define("enabled", true);
                scale = builder.defineInRange("scale", 1.0D, 0.1D, 5.0D);
                backgroundAlpha = builder.defineInRange("background_alpha", 100, 0, 255);
                showBackground = builder.define("show_background", true);
                zOrder = builder.defineInRange("z_order", z, -1000, 1000);
                builder.pop();
            }
        }
    }
}
