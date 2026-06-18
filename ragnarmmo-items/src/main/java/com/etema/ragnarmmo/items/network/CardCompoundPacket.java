package com.etema.ragnarmmo.items.network;

import com.etema.ragnarmmo.items.cards.CardItem;
import com.etema.ragnarmmo.items.cards.CardRegistry;
import com.etema.ragnarmmo.items.runtime.RoEquipmentTypeResolver;
import com.etema.ragnarmmo.items.runtime.RoItemNbtHelper;
import com.etema.ragnarmmo.items.runtime.RoItemRuleResolver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CardCompoundPacket {
    private final int cardSlot;
    private final int equipSlot;

    public CardCompoundPacket(int cardSlot, int equipSlot) {
        this.cardSlot = cardSlot;
        this.equipSlot = equipSlot;
    }

    public static void encode(CardCompoundPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.cardSlot);
        buf.writeInt(msg.equipSlot);
    }

    public static CardCompoundPacket decode(FriendlyByteBuf buf) {
        return new CardCompoundPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(CardCompoundPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            ItemStack cardStack = player.getInventory().getItem(msg.cardSlot);
            ItemStack equipStack = player.getInventory().getItem(msg.equipSlot);
            if (cardStack.isEmpty() || !(cardStack.getItem() instanceof CardItem) || equipStack.isEmpty()) {
                return;
            }

            String cardId = CardItem.getCardId(cardStack);
            var cardDef = cardId != null ? CardRegistry.getInstance().get(cardId) : null;
            if (cardDef == null || !RoEquipmentTypeResolver.isCompatible(cardDef.equipType(), equipStack)) {
                return;
            }

            int maxSlots = RoItemRuleResolver.resolve(equipStack).cardSlots();
            if (maxSlots <= 0 || RoItemNbtHelper.getSlottedCards(equipStack).size() >= maxSlots) {
                return;
            }

            var cardModifiers = cardStack.getTag() != null && cardStack.getTag().contains(CardItem.TAG_CARD_MODIFIERS)
                    ? cardStack.getTag().getCompound(CardItem.TAG_CARD_MODIFIERS).copy()
                    : null;
            cardStack.shrink(1);
            RoItemNbtHelper.addSlottedCard(equipStack, cardId);
            if (cardModifiers != null) {
                RoItemNbtHelper.addCompoundedCardModifiers(equipStack, cardModifiers);
            }
            player.level().playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        });
        ctx.setPacketHandled(true);
    }
}
