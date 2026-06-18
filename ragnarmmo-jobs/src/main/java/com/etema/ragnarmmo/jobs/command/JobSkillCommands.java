package com.etema.ragnarmmo.jobs.command;

import com.etema.ragnarmmo.jobs.data.SkillDefinitionRegistry;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import com.etema.ragnarmmo.jobs.runtime.JobSkillExecutor;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class JobSkillCommands {
    private JobSkillCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createNode() {
        return Commands.literal("jobskills")
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("use")
                        .then(Commands.argument("skill", StringArgumentType.string())
                                .executes(ctx -> use(
                                        ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "skill")))))
                .then(Commands.literal("hotbar")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(0, 8))
                                .then(Commands.argument("skill", StringArgumentType.string())
                                        .executes(ctx -> setHotbar(
                                                ctx.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(ctx, "slot"),
                                                StringArgumentType.getString(ctx, "skill"))))));
    }

    private static int list(ServerPlayer player) {
        PlayerJobSkillsProvider.get(player).ifPresent(skills -> {
            player.sendSystemMessage(Component.translatable("command.ragnarmmo.jobskills.learned").withStyle(ChatFormatting.GOLD));
            skills.getSkillLevels().entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .forEach(entry -> player.sendSystemMessage(Component.translatable(
                            "command.ragnarmmo.jobskills.entry", entry.getKey(), entry.getValue()).withStyle(ChatFormatting.GRAY)));
        });
        return 1;
    }

    private static int use(ServerPlayer player, String rawSkillId) {
        ResourceLocation id = parseSkillId(rawSkillId);
        return JobSkillExecutor.use(player, id) ? 1 : 0;
    }

    private static int setHotbar(ServerPlayer player, int slot, String rawSkillId) {
        ResourceLocation id = parseSkillId(rawSkillId);
        if (id == null) {
            return 0;
        }
        PlayerJobSkillsProvider.get(player).ifPresent(skills -> {
            if (skills.getSkillLevel(id) > 0 && SkillDefinitionRegistry.get(id).map(def -> def.isActive()).orElse(false)) {
                skills.setHotbarSlot(slot, id);
                player.sendSystemMessage(Component.translatable("command.ragnarmmo.jobskills.assigned", id, slot)
                        .withStyle(ChatFormatting.GREEN));
            }
        });
        return 1;
    }

    private static ResourceLocation parseSkillId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.contains(":")
                ? ResourceLocation.tryParse(raw)
                : ResourceLocation.fromNamespaceAndPath("ragnarmmo", raw);
    }
}
