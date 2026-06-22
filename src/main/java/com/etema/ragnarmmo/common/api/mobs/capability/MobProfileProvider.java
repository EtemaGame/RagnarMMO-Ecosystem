package com.etema.ragnarmmo.common.api.mobs.capability;

import com.etema.ragnarmmo.core.RagnarMMOCore;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RagnarMMOCore.MOD_ID)
public final class MobProfileProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<MobProfileState> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RagnarMMOCore.MOD_ID, "mob_profile");

    private final MobProfileState state = new MobProfileState();
    private final LazyOptional<MobProfileState> optional = LazyOptional.of(() -> state);

    public static LazyOptional<MobProfileState> get(Entity entity) {
        return entity == null ? LazyOptional.empty() : entity.getCapability(CAP);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof LivingEntity living && !(living instanceof Player)) {
            event.addCapability(ID, new MobProfileProvider());
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == CAP ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return state.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        state.deserializeNBT(nbt);
    }
}
