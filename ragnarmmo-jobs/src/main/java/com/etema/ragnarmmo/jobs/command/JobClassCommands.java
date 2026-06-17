package com.etema.ragnarmmo.jobs.command;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.jobs.runtime.JobChangeService;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class JobClassCommands {
    private JobClassCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createNode() {
        return Commands.literal("job")
                .then(Commands.literal("current")
                        .executes(ctx -> current(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("change")
                        .then(Commands.argument("class", StringArgumentType.word())
                                .executes(ctx -> change(
                                        ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "class")))));
    }

    private static int current(ServerPlayer player) {
        RagnarCoreAPI.get(player).ifPresent(stats -> {
            JobType job = JobType.fromId(stats.getJobId());
            player.sendSystemMessage(Component.literal(
                    job.getDisplayName() + " Job Lv " + stats.getJobLevel()
                            + " (" + stats.getSkillPoints() + " skill points)")
                    .withStyle(ChatFormatting.GOLD));
        });
        return 1;
    }

    private static int change(ServerPlayer player, String rawJobId) {
        JobType requestedJob = JobType.fromId(rawJobId);
        return JobChangeService.changeToFirstClass(player, requestedJob) ? 1 : 0;
    }
}
