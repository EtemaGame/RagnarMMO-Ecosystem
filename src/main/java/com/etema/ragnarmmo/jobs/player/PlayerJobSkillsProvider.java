package com.etema.ragnarmmo.jobs.player;

import com.etema.ragnarmmo.common.api.skills.RagnarSkillsAPI;
import com.etema.ragnarmmo.jobs.RagnarMMOJobs;
import com.etema.ragnarmmo.jobs.net.JobSkillsSyncService;
import com.etema.ragnarmmo.player.character.runtime.CharacterSelectionService;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

@Mod.EventBusSubscriber(modid = RagnarMMOJobs.MOD_ID)
public final class PlayerJobSkillsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<PlayerJobSkills> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("ragnarmmo", "player_skills");

    static {
        RagnarSkillsAPI.registerAccessor(player -> player == null
                ? java.util.Optional.empty()
                : player.getCapability(CAP).map(skills -> skills));
    }

    private final PlayerJobSkills impl = new PlayerJobSkills();
    private final LazyOptional<PlayerJobSkills> opt = LazyOptional.of(() -> impl);

    public static LazyOptional<PlayerJobSkills> get(Player player) {
        return player == null ? LazyOptional.empty() : player.getCapability(CAP);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ID, new PlayerJobSkillsProvider());
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
        }
        try {
            event.getOriginal().getCapability(CAP).ifPresent(oldSkills ->
                    event.getEntity().getCapability(CAP).ifPresent(newSkills ->
                            newSkills.deserializeNBT(oldSkills.serializeNBT())));
        } finally {
            if (event.isWasDeath()) {
                event.getOriginal().invalidateCaps();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (CharacterSelectionService.isSelectionRequired(player)) {
                return;
            }
            JobSkillsSyncService.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JobSkillsSyncService.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JobSkillsSyncService.sync(player);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == CAP ? opt.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return impl.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        impl.deserializeNBT(nbt);
    }
}
