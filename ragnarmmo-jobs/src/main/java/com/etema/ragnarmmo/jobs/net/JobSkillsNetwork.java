package com.etema.ragnarmmo.jobs.net;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public final class JobSkillsNetwork {
    private JobSkillsNetwork() {
    }

    public static void register(SimpleChannel channel, AtomicInteger id) {
        channel.messageBuilder(JobSkillsSyncPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(JobSkillsSyncPacket::encode)
                .decoder(JobSkillsSyncPacket::decode)
                .consumerMainThread(JobSkillsSyncPacket::handle)
                .add();

        channel.messageBuilder(ServerboundUpgradeSkillPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundUpgradeSkillPacket::encode)
                .decoder(ServerboundUpgradeSkillPacket::decode)
                .consumerMainThread(ServerboundUpgradeSkillPacket::handle)
                .add();

        channel.messageBuilder(ServerboundSetHotbarSlotPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundSetHotbarSlotPacket::encode)
                .decoder(ServerboundSetHotbarSlotPacket::decode)
                .consumerMainThread(ServerboundSetHotbarSlotPacket::handle)
                .add();

        channel.messageBuilder(ServerboundUseJobSkillPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundUseJobSkillPacket::encode)
                .decoder(ServerboundUseJobSkillPacket::decode)
                .consumerMainThread(ServerboundUseJobSkillPacket::handle)
                .add();

        channel.messageBuilder(ServerboundChangeJobPacket.class, id.getAndIncrement(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundChangeJobPacket::encode)
                .decoder(ServerboundChangeJobPacket::decode)
                .consumerMainThread(ServerboundChangeJobPacket::handle)
                .add();
    }
}
