package com.etema.ragnarmmo.core.client;

import com.etema.ragnarmmo.player.stats.network.DerivedStatsSyncPacket;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncPacket;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class CoreClientPacketHandler {
    public interface Handler {
        void handlePlayerStatsSync(PlayerStatsSyncPacket packet);

        void handleDerivedStatsSync(DerivedStatsSyncPacket packet);
    }

    private static final AtomicReference<Handler> HANDLER = new AtomicReference<>();

    private CoreClientPacketHandler() {
    }

    public static void register(Handler handler) {
        HANDLER.set(handler);
    }

    public static Optional<Handler> current() {
        return Optional.ofNullable(HANDLER.get());
    }
}
