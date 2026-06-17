package com.etema.ragnarmmo.player.party;

import com.etema.ragnarmmo.social.RagnarMMOSocial;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RagnarMMOSocial.MOD_ID)
public final class PartyLootService {
    private static final String TAG_PARTY_ID = "RagnarMMOPartyLootParty";
    private static final String TAG_OWNER_ID = "RagnarMMOPartyLootOwner";
    private static final String TAG_PRIORITY_UNTIL = "RagnarMMOPartyLootPriorityUntil";
    private static final String TAG_MODE = "RagnarMMOPartyLootMode";

    private PartyLootService() {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer) || killer.getServer() == null) {
            return;
        }

        PartySavedData data = PartySavedData.get(killer.getServer());
        Party party = data.getPartyByPlayer(killer.getUUID());
        if (party == null || party.getSettings().getLootMode() == PartySettings.LootMode.OFF) {
            return;
        }

        List<ServerPlayer> eligible = party.getEligibleMembersForLoot(killer);
        if (eligible.isEmpty()) {
            return;
        }

        boolean cursorChanged = false;
        for (ItemEntity drop : event.getDrops()) {
            UUID owner = switch (party.getSettings().getLootMode()) {
                case FREE -> null;
                case PRIORITY -> killer.getUUID();
                case ROUND_ROBIN -> {
                    cursorChanged = true;
                    yield party.nextLootOwner(eligible);
                }
                case OFF -> null;
            };
            markPartyDrop(drop, party, owner, killer.level().getGameTime());
        }

        if (cursorChanged) {
            data.markDirty();
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.getServer() == null) {
            return;
        }

        ItemEntity item = event.getItem();
        CompoundTag tag = item.getPersistentData();
        if (!tag.hasUUID(TAG_PARTY_ID)) {
            return;
        }

        Party party = PartySavedData.get(player.getServer()).getParty(tag.getUUID(TAG_PARTY_ID));
        if (party == null || !party.isMember(player.getUUID())) {
            event.setCanceled(true);
            return;
        }

        if (!tag.hasUUID(TAG_OWNER_ID)) {
            return;
        }

        long priorityUntil = tag.getLong(TAG_PRIORITY_UNTIL);
        if (player.level().getGameTime() <= priorityUntil && !player.getUUID().equals(tag.getUUID(TAG_OWNER_ID))) {
            event.setCanceled(true);
            if (player.tickCount % 40 == 0) {
                player.displayClientMessage(Component.literal("Party loot is reserved for another member."), true);
            }
        }
    }

    private static void markPartyDrop(ItemEntity drop, Party party, UUID owner, long gameTime) {
        if (drop == null || party == null) {
            return;
        }
        CompoundTag tag = drop.getPersistentData();
        tag.putUUID(TAG_PARTY_ID, party.getPartyId());
        tag.putString(TAG_MODE, party.getSettings().getLootMode().name());
        if (owner != null) {
            tag.putUUID(TAG_OWNER_ID, owner);
            tag.putLong(TAG_PRIORITY_UNTIL, gameTime + party.getSettings().getLootPrioritySeconds() * 20L);
        } else {
            tag.remove(TAG_OWNER_ID);
            tag.putLong(TAG_PRIORITY_UNTIL, 0L);
        }
    }
}
