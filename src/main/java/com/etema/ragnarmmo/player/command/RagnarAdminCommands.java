package com.etema.ragnarmmo.player.command;

import com.etema.ragnarmmo.RagnarMMO;
import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.player.RoPlayerSyncDomain;
import com.etema.ragnarmmo.common.api.stats.ChangeReason;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import com.etema.ragnarmmo.jobs.net.JobSkillsSyncService;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import com.etema.ragnarmmo.player.stats.compute.StatResolutionService;
import com.etema.ragnarmmo.player.stats.network.PlayerStatsSyncService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = RagnarMMO.MOD_ID)
public final class RagnarAdminCommands {
    private RagnarAdminCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("roadmin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("stats")
                        .then(Commands.literal("reset")
                                .executes(ctx -> resetStats(ctx, self(ctx)))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> resetStats(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                .then(Commands.literal("skills")
                        .then(Commands.literal("reset")
                                .executes(ctx -> resetSkills(ctx, self(ctx)))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> resetSkills(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                .then(Commands.literal("stat")
                        .then(Commands.literal("set").then(statValueCommand(false)))
                        .then(Commands.literal("add").then(statValueCommand(true))))
                .then(Commands.literal("level")
                        .then(Commands.literal("base").then(Commands.literal("set").then(levelCommand(false))))
                        .then(Commands.literal("job").then(Commands.literal("set").then(levelCommand(true)))))
                .then(Commands.literal("exp")
                        .then(Commands.literal("base")
                                .then(Commands.literal("set").then(expCommand(false, false)))
                                .then(Commands.literal("add").then(expCommand(false, true))))
                        .then(Commands.literal("job")
                                .then(Commands.literal("set").then(expCommand(true, false)))
                                .then(Commands.literal("add").then(expCommand(true, true)))))
                .then(Commands.literal("job")
                        .then(Commands.literal("set")
                                .then(Commands.argument("job", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(JobType.values()).map(JobType::getId), builder))
                                        .executes(ctx -> setJob(ctx, self(ctx)))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> setJob(ctx, EntityArgument.getPlayer(ctx, "target")))))))
                .then(Commands.literal("status")
                        .then(Commands.literal("apply")
                                .then(Commands.argument("status", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(STATUS_IDS, builder))
                                        .then(Commands.argument("duration_ticks", IntegerArgumentType.integer(1))
                                                .executes(ctx -> applyStatus(ctx, self(ctx)))
                                                .then(Commands.argument("target", EntityArgument.player())
                                                        .executes(ctx -> applyStatus(ctx, EntityArgument.getPlayer(ctx, "target")))))))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("status", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(STATUS_IDS, builder))
                                        .executes(ctx -> clearStatus(ctx, self(ctx)))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> clearStatus(ctx, EntityArgument.getPlayer(ctx, "target"))))))));
    }

    private static final java.util.List<String> STATUS_IDS = java.util.List.of(
            "poison",
            "silence",
            "blind",
            "chaos",
            "frozen",
            "stone_curse",
            "hiding",
            "cloaking",
            "stun",
            "sleep",
            "curse",
            "bleeding");

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> statValueCommand(boolean add) {
        return Commands.argument("stat", StringArgumentType.word())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(StatKeys.values())
                                .filter(key -> key != StatKeys.LEVEL)
                                .map(StatKeys::id), builder))
                .then(Commands.argument("value", IntegerArgumentType.integer())
                        .executes(ctx -> setStat(ctx, self(ctx), add))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> setStat(ctx, EntityArgument.getPlayer(ctx, "target"), add))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, Integer> levelCommand(boolean jobLevel) {
        return Commands.argument("value", IntegerArgumentType.integer(1))
                .executes(ctx -> setLevel(ctx, self(ctx), jobLevel))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> setLevel(ctx, EntityArgument.getPlayer(ctx, "target"), jobLevel)));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, Integer> expCommand(boolean jobExp, boolean add) {
        return Commands.argument("value", IntegerArgumentType.integer(0))
                .executes(ctx -> setExp(ctx, self(ctx), jobExp, add))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> setExp(ctx, EntityArgument.getPlayer(ctx, "target"), jobExp, add)));
    }

    private static int resetStats(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        IPlayerStats stats = statsOrFail(context, target);
        if (stats == null) {
            return 0;
        }
        String jobId = stats.getJobId();
        int baseLevel = stats.getLevel();
        int jobLevel = stats.getJobLevel();
        int baseExp = stats.getExp();
        int jobExp = stats.getJobExp();
        int skillPoints = stats.getSkillPoints();
        stats.resetAll(ChangeReason.ADMIN_COMMAND);
        stats.setJobId(jobId, ChangeReason.ADMIN_COMMAND);
        stats.setLevel(baseLevel, ChangeReason.ADMIN_COMMAND);
        stats.setJobLevel(jobLevel, ChangeReason.ADMIN_COMMAND);
        stats.setExp(baseExp);
        stats.setJobExp(jobExp);
        stats.setSkillPoints(skillPoints);
        StatResolutionService.resolve(target, stats);
        return success(context, "Reset primary stats for " + target.getGameProfile().getName() + ".");
    }

    private static int resetSkills(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        PlayerJobSkillsProvider.get(target).ifPresent(skills -> {
            skills.resetAll();
            JobSkillsSyncService.sync(target);
        });
        return success(context, "Reset skills and hotbar for " + target.getGameProfile().getName() + ".");
    }

    private static int setStat(CommandContext<CommandSourceStack> context, ServerPlayer target, boolean add) {
        IPlayerStats stats = statsOrFail(context, target);
        if (stats == null) {
            return 0;
        }
        String raw = StringArgumentType.getString(context, "stat");
        StatKeys key = StatKeys.fromId(raw).filter(value -> value != StatKeys.LEVEL).orElse(null);
        if (key == null) {
            context.getSource().sendFailure(Component.literal("Unknown primary stat: " + raw));
            return 0;
        }
        int value = IntegerArgumentType.getInteger(context, "value");
        if (add) {
            stats.addStat(key, value, ChangeReason.ADMIN_COMMAND);
        } else {
            stats.setStat(key, value, ChangeReason.ADMIN_COMMAND);
        }
        StatResolutionService.resolve(target, stats);
        return success(context, key.id() + " is now " + stats.get(key) + " for " + target.getGameProfile().getName() + ".");
    }

    private static int setLevel(CommandContext<CommandSourceStack> context, ServerPlayer target, boolean jobLevel) {
        IPlayerStats stats = statsOrFail(context, target);
        if (stats == null) {
            return 0;
        }
        int value = IntegerArgumentType.getInteger(context, "value");
        if (jobLevel) {
            stats.setJobLevel(value, ChangeReason.ADMIN_COMMAND);
        } else {
            stats.setLevel(value, ChangeReason.ADMIN_COMMAND);
        }
        StatResolutionService.resolve(target, stats);
        return success(context, (jobLevel ? "Job level" : "Base level") + " is now "
                + (jobLevel ? stats.getJobLevel() : stats.getLevel()) + " for " + target.getGameProfile().getName() + ".");
    }

    private static int setExp(CommandContext<CommandSourceStack> context, ServerPlayer target, boolean jobExp, boolean add) {
        IPlayerStats stats = statsOrFail(context, target);
        if (stats == null) {
            return 0;
        }
        int value = IntegerArgumentType.getInteger(context, "value");
        if (jobExp) {
            stats.setJobExp(add ? stats.getJobExp() + value : value);
        } else {
            stats.setExp(add ? stats.getExp() + value : value);
        }
        PlayerStatsSyncService.sync(target, stats, RoPlayerSyncDomain.PROGRESSION.bit());
        return success(context, (jobExp ? "Job exp" : "Base exp") + " is now "
                + (jobExp ? stats.getJobExp() : stats.getExp()) + " for " + target.getGameProfile().getName() + ".");
    }

    private static int setJob(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        IPlayerStats stats = statsOrFail(context, target);
        if (stats == null) {
            return 0;
        }
        JobType job = JobType.fromId(StringArgumentType.getString(context, "job"));
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RagnarMMO.MOD_ID, job.getId());
        stats.setJobId(id.toString(), ChangeReason.ADMIN_COMMAND);
        stats.setJobLevel(1, ChangeReason.ADMIN_COMMAND);
        stats.setJobExp(0);
        StatResolutionService.resolve(target, stats);
        return success(context, "Job is now " + id + " for " + target.getGameProfile().getName() + ".");
    }

    private static int applyStatus(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        String status = StringArgumentType.getString(context, "status");
        int duration = IntegerArgumentType.getInteger(context, "duration_ticks");
        if (!applyStatusById(target, status, duration)) {
            context.getSource().sendFailure(Component.literal("Unknown RO status: " + status));
            return 0;
        }
        return success(context, "Applied " + status + " to " + target.getGameProfile().getName()
                + " for " + duration + " ticks.");
    }

    private static int clearStatus(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        String status = StringArgumentType.getString(context, "status");
        if (!clearStatusById(target, status)) {
            context.getSource().sendFailure(Component.literal("Unknown RO status: " + status));
            return 0;
        }
        return success(context, "Cleared " + status + " from " + target.getGameProfile().getName() + ".");
    }

    private static boolean applyStatusById(ServerPlayer target, String status, int durationTicks) {
        switch (status) {
            case "poison" -> RoCombatStatusService.applyPoison(target, durationTicks);
            case "silence" -> RoCombatStatusService.applySilence(target, durationTicks);
            case "blind" -> RoCombatStatusService.applyBlind(target, durationTicks);
            case "chaos" -> RoCombatStatusService.applyChaos(target, durationTicks);
            case "frozen" -> RoCombatStatusService.applyFrozen(target, durationTicks);
            case "stone_curse" -> RoCombatStatusService.applyStoneCurse(target, durationTicks);
            case "hiding" -> RoCombatStatusService.applyHiding(target, durationTicks);
            case "cloaking" -> RoCombatStatusService.applyCloaking(target, durationTicks);
            case "stun" -> RoCombatStatusService.applyStun(target, durationTicks);
            case "sleep" -> RoCombatStatusService.applySleep(target, durationTicks);
            case "curse" -> RoCombatStatusService.applyCurse(target, durationTicks);
            case "bleeding" -> RoCombatStatusService.applyBleeding(target, durationTicks);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static boolean clearStatusById(ServerPlayer target, String status) {
        switch (status) {
            case "poison" -> RoCombatStatusService.clearPoison(target);
            case "silence" -> RoCombatStatusService.clearSilence(target);
            case "blind" -> RoCombatStatusService.clearBlind(target);
            case "chaos" -> RoCombatStatusService.clearChaos(target);
            case "frozen" -> RoCombatStatusService.clearFrozen(target);
            case "stone_curse" -> RoCombatStatusService.clearStoneCurse(target);
            case "hiding" -> RoCombatStatusService.clearHiding(target);
            case "cloaking" -> RoCombatStatusService.clearCloaking(target);
            case "stun" -> RoCombatStatusService.clearStun(target);
            case "sleep" -> RoCombatStatusService.clearSleep(target);
            case "curse" -> RoCombatStatusService.clearCurse(target);
            case "bleeding" -> RoCombatStatusService.clearBleeding(target);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static ServerPlayer self(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return context.getSource().getPlayerOrException();
    }

    private static IPlayerStats statsOrFail(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        var stats = RagnarCoreAPI.get(target);
        if (stats.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Target has no RagnarMMO stats capability."));
            return null;
        }
        return stats.get();
    }

    private static int success(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }
}
