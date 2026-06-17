package com.etema.ragnarmmo.player.stats.network;

import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerStatsSyncPacketTest {
    @Test
    void playerStatsPacketRoundTripsCriticalFields() {
        PlayerStatsSyncPacket packet = new PlayerStatsSyncPacket(
                42,
                7,
                11.0,
                22.0,
                33.0,
                44.0,
                12,
                345,
                67,
                8,
                90,
                3,
                "ragnarmmo:swordsman",
                true,
                10,
                11,
                12,
                13,
                14,
                15);

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        PlayerStatsSyncPacket.encode(packet, buffer);
        PlayerStatsSyncPacket decoded = PlayerStatsSyncPacket.decode(buffer);

        assertEquals(packet.entityId, decoded.entityId);
        assertEquals(packet.syncMask, decoded.syncMask);
        assertEquals(packet.level, decoded.level);
        assertEquals(packet.jobLevel, decoded.jobLevel);
        assertEquals(packet.jobId, decoded.jobId);
        assertEquals(packet.str, decoded.str);
        assertEquals(packet.luk, decoded.luk);
    }

    @Test
    void derivedStatsPacketRoundTripsServerComputedSnapshot() {
        DerivedStats stats = new DerivedStats();
        stats.physicalAttack = 123.5;
        stats.magicAttack = 45.25;
        stats.maxHealth = 987.0;
        stats.maxSP = 321.0;
        stats.lifeSteal = 0.2;

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        DerivedStatsSyncPacket.encode(new DerivedStatsSyncPacket(stats), buffer);
        DerivedStats decoded = DerivedStatsSyncPacket.decode(buffer).toDerivedStats();

        assertEquals(123.5, decoded.physicalAttack);
        assertEquals(45.25, decoded.magicAttack);
        assertEquals(987.0, decoded.maxHealth);
        assertEquals(321.0, decoded.maxSP);
        assertEquals(0.2, decoded.lifeSteal);
    }
}
