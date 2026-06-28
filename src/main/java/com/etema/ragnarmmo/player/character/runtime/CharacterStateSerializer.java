package com.etema.ragnarmmo.player.character.runtime;

import com.etema.ragnarmmo.items.equipment.RagnarEquipmentHandler;
import com.etema.ragnarmmo.items.equipment.RagnarEquipmentProvider;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkills;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import com.etema.ragnarmmo.lifeskills.LifeSkillCapability;
import com.etema.ragnarmmo.lifeskills.LifeSkillManager;
import com.etema.ragnarmmo.player.character.data.CharacterSaveData;
import com.etema.ragnarmmo.player.stats.capability.PlayerStats;
import com.etema.ragnarmmo.player.stats.capability.PlayerStatsProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class CharacterStateSerializer {
    private CharacterStateSerializer() {
    }

    public static CharacterSaveData capture(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        player.getCapability(PlayerStatsProvider.CAP).ifPresent(stats -> tag.put("Stats", stats.serializeNBT()));
        PlayerJobSkillsProvider.get(player).ifPresent(skills -> tag.put("Skills", skills.serializeNBT()));
        LifeSkillCapability.get(player).ifPresent(lifeSkills -> tag.put("LifeSkills", lifeSkills.serializeNBT()));
        RagnarEquipmentProvider.get(player).ifPresent(equipment -> tag.put("RagnarEquipment", equipment.serializeNBT()));

        tag.put("Inventory", player.getInventory().save(new ListTag()));
        tag.put("EnderChest", player.getEnderChestInventory().createTag());

        CompoundTag location = new CompoundTag();
        location.putString("Dimension", player.level().dimension().location().toString());
        location.putDouble("X", player.getX());
        location.putDouble("Y", player.getY());
        location.putDouble("Z", player.getZ());
        location.putFloat("YRot", player.getYRot());
        location.putFloat("XRot", player.getXRot());
        tag.put("Location", location);
        tag.putFloat("Health", player.getHealth());
        tag.putInt("FoodLevel", player.getFoodData().getFoodLevel());
        tag.putFloat("Saturation", player.getFoodData().getSaturationLevel());
        return new CharacterSaveData(tag);
    }

    public static void apply(ServerPlayer player, CharacterSaveData saveData) {
        CompoundTag tag = saveData == null ? new CompoundTag() : saveData.serializeNBT();
        applyStats(player, tag.getCompound("Stats"));
        applySkills(player, tag.getCompound("Skills"));
        applyLifeSkills(player, tag.getCompound("LifeSkills"));
        applyEquipment(player, tag.getCompound("RagnarEquipment"));
        player.getInventory().load(tag.getList("Inventory", 10));
        player.getEnderChestInventory().fromTag(tag.getList("EnderChest", 10));
        applyLocation(player, tag.getCompound("Location"));
        float health = tag.contains("Health") ? tag.getFloat("Health") : player.getMaxHealth();
        player.setHealth(Math.max(1.0F, Math.min(health, player.getMaxHealth())));
        if (tag.contains("FoodLevel")) {
            player.getFoodData().setFoodLevel(tag.getInt("FoodLevel"));
        }
        if (tag.contains("Saturation")) {
            player.getFoodData().setSaturation(tag.getFloat("Saturation"));
        }
        player.inventoryMenu.broadcastChanges();
    }

    public static CharacterSaveData createNew(ServerPlayer player) {
        applyStats(player, new PlayerStats().serializeNBT());
        applySkills(player, new PlayerJobSkills().serializeNBT());
        applyLifeSkills(player, new LifeSkillManager().serializeNBT());
        applyEquipment(player, new RagnarEquipmentHandler(player).serializeNBT());
        player.getInventory().clearContent();
        player.getEnderChestInventory().clearContent();
        CompoundTag tag = capture(player).serializeNBT();
        CompoundTag location = new CompoundTag();
        ServerLevel level = player.serverLevel();
        var spawn = level.getSharedSpawnPos();
        location.putString("Dimension", level.dimension().location().toString());
        location.putDouble("X", spawn.getX() + 0.5D);
        location.putDouble("Y", spawn.getY());
        location.putDouble("Z", spawn.getZ() + 0.5D);
        location.putFloat("YRot", player.getYRot());
        location.putFloat("XRot", player.getXRot());
        tag.put("Location", location);
        tag.putFloat("Health", player.getMaxHealth());
        return new CharacterSaveData(tag);
    }

    private static void applyStats(ServerPlayer player, CompoundTag tag) {
        player.getCapability(PlayerStatsProvider.CAP).ifPresent(stats -> stats.deserializeNBT(tag));
    }

    private static void applySkills(ServerPlayer player, CompoundTag tag) {
        PlayerJobSkillsProvider.get(player).ifPresent(skills -> skills.deserializeNBT(tag));
    }

    private static void applyLifeSkills(ServerPlayer player, CompoundTag tag) {
        LifeSkillCapability.get(player).ifPresent(lifeSkills -> lifeSkills.deserializeNBT(tag));
    }

    private static void applyEquipment(ServerPlayer player, CompoundTag tag) {
        RagnarEquipmentProvider.get(player).ifPresent(equipment -> equipment.deserializeNBT(tag));
    }

    private static void applyLocation(ServerPlayer player, CompoundTag location) {
        if (location == null || !location.contains("Dimension")) {
            return;
        }
        ResourceLocation dimensionId = ResourceLocation.tryParse(location.getString("Dimension"));
        if (dimensionId == null) {
            return;
        }
        ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId);
        ServerLevel targetLevel = player.server.getLevel(dimension);
        if (targetLevel == null) {
            targetLevel = player.server.overworld();
        }
        double x = location.getDouble("X");
        double y = location.getDouble("Y");
        double z = location.getDouble("Z");
        float yRot = location.getFloat("YRot");
        float xRot = location.getFloat("XRot");
        player.teleportTo(targetLevel, x, y, z, yRot, xRot);
    }
}
