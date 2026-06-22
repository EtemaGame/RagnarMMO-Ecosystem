package com.etema.ragnarmmo.items.equipment;

import com.etema.ragnarmmo.items.RagnarMMOItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOItems.MOD_ID)
public final class RagnarEquipmentProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<RagnarEquipmentHandler> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RagnarMMOItems.MOD_ID, "equipment");

    private final RagnarEquipmentHandler handler;
    private final LazyOptional<RagnarEquipmentHandler> optional;

    public RagnarEquipmentProvider(Player player) {
        this.handler = new RagnarEquipmentHandler(player);
        this.optional = LazyOptional.of(() -> handler);
    }

    public static LazyOptional<RagnarEquipmentHandler> get(Player player) {
        return player == null ? LazyOptional.empty() : player.getCapability(CAP);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(ID, new RagnarEquipmentProvider(player));
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
        }

        try {
            var oldEquipment = event.getOriginal().getCapability(CAP);
            var newEquipment = event.getEntity().getCapability(CAP);
            oldEquipment.ifPresent(oldHandler -> newEquipment.ifPresent(newHandler ->
                    newHandler.deserializeNBT(oldHandler.serializeNBT())));
        } finally {
            if (event.isWasDeath()) {
                event.getOriginal().invalidateCaps();
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == CAP ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return handler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        handler.deserializeNBT(nbt);
    }
}
