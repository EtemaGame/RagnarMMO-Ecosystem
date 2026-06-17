package com.etema.ragnarmmo.player.stats.capability;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.player.stats.PlayerStatsModule;
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
import com.etema.ragnarmmo.player.stats.compute.StatResolutionService;

@Mod.EventBusSubscriber(modid = PlayerStatsModule.MOD_ID)
public class PlayerStatsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<IPlayerStats> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation PLAYER_STATS_ID = ResourceLocation.fromNamespaceAndPath(PlayerStatsModule.MOD_ID, "player_stats");

    static {
        RagnarCoreAPI.registerAccessor(player -> player == null
                ? java.util.Optional.empty()
                : player.getCapability(CAP).resolve());
    }

    private final PlayerStats impl;
    private final LazyOptional<IPlayerStats> opt;

    public PlayerStatsProvider(Player owner) {
        this.impl = new PlayerStats();
        this.impl.bind(owner);
        this.opt = LazyOptional.of(() -> impl);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(PLAYER_STATS_ID, new PlayerStatsProvider(player));
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
        }

        try {
            var oldOpt = event.getOriginal().getCapability(CAP);
            var newOpt = event.getEntity().getCapability(CAP);
            if (oldOpt.isPresent() && newOpt.isPresent()) {
                IPlayerStats oldStats = oldOpt.resolve().get();
                IPlayerStats newStats = newOpt.resolve().get();
                newStats.deserializeNBT(oldStats.serializeNBT());
                newStats.markDirty();
            }
        } finally {
            if (event.isWasDeath()) {
                event.getOriginal().invalidateCaps();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resolveAndSync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resolveAndSync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resolveAndSync(player);
        }
    }

    private static void resolveAndSync(ServerPlayer player) {
        player.getCapability(CAP).ifPresent(stats -> StatResolutionService.resolve(player, stats));
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
