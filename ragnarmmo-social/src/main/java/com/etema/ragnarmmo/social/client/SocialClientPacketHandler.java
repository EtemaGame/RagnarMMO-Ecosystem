package com.etema.ragnarmmo.social.client;

import com.etema.ragnarmmo.achievements.capability.PlayerAchievementsProvider;
import com.etema.ragnarmmo.player.party.PartyClientData;
import com.etema.ragnarmmo.player.party.net.PartyMemberData;
import com.etema.ragnarmmo.social.RagnarMMOSocial;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class SocialClientPacketHandler {
    private SocialClientPacketHandler() {
    }

    public static void handleAchievementsSync(int entityId, CompoundTag tag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Entity entity = mc.level.getEntity(entityId);
        if (entity == null && mc.player != null && mc.player.getId() == entityId) {
            entity = mc.player;
        }

        if (entity != null) {
            entity.getCapability(PlayerAchievementsProvider.PLAYER_ACHIEVEMENTS)
                    .ifPresent(cap -> cap.deserializeNBT(tag));
        } else {
            RagnarMMOSocial.LOGGER.warn("Received achievement sync for unknown entity: {}", entityId);
        }
    }

    public static void handlePartySnapshot(boolean hasParty, UUID partyId, String partyName,
            List<PartyMemberData> members) {
        if (hasParty) {
            PartyClientData.setParty(partyId, partyName, members);
        } else {
            PartyClientData.clearParty();
        }
    }

    public static void handlePartyMemberUpdate(PartyMemberData memberData) {
        PartyClientData.updateMember(memberData);
    }
}
