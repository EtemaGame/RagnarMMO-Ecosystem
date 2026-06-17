package com.etema.ragnarmmo.combat.engine;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.api.BasicAttackFailureReason;
import com.etema.ragnarmmo.combat.api.BasicAttackOutcome;
import com.etema.ragnarmmo.combat.api.BasicAttackSource;
import com.etema.ragnarmmo.combat.api.CombatHitResultType;
import com.etema.ragnarmmo.combat.api.CombatRejectReason;
import com.etema.ragnarmmo.combat.api.CombatResolution;
import com.etema.ragnarmmo.combat.api.RagnarAttackRequest;
import com.etema.ragnarmmo.combat.api.ResolvedTargetCandidate;
import com.etema.ragnarmmo.combat.formula.AccuracyFormulaService;
import com.etema.ragnarmmo.combat.formula.DamageFormulaService;
import com.etema.ragnarmmo.combat.formula.DefenseFormulaService;
import com.etema.ragnarmmo.combat.state.CombatActorState;
import com.etema.ragnarmmo.combat.targeting.ServerTargetResolver;
import com.etema.ragnarmmo.combat.timing.AttackCadenceCalculator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RagnarCombatEngine {
    private static final RagnarCombatEngine INSTANCE = new RagnarCombatEngine();

    private final Map<UUID, CombatActorState> actorStates = new ConcurrentHashMap<>();

    private RagnarCombatEngine() {
    }

    public static RagnarCombatEngine get() {
        return INSTANCE;
    }

    public BasicAttackOutcome processBasicAttackRequest(ServerPlayer player, RagnarAttackRequest request,
            BasicAttackSource source) {
        if (player == null) {
            return BasicAttackOutcome.rejected(source, CombatRejectReason.MISSING_ACTOR, true, List.of());
        }
        if (player.isSpectator()) {
            return BasicAttackOutcome.rejected(source, CombatRejectReason.ACTOR_SPECTATOR, true, List.of());
        }
        if (!player.isAlive()) {
            return BasicAttackOutcome.rejected(source, CombatRejectReason.ACTOR_DEAD, true, List.of());
        }

        RagnarAttackRequest safeRequest = request != null ? request : RagnarAttackRequest.empty(-1);
        CombatActorState state = actorStates.computeIfAbsent(player.getUUID(), ignored -> new CombatActorState());
        long now = player.serverLevel().getGameTime();
        if (source == BasicAttackSource.CLIENT_PACKET && state.isStale(safeRequest.sequenceId())) {
            return BasicAttackOutcome.rejected(source, CombatRejectReason.STALE_SEQUENCE, true, List.of());
        }
        if (!state.basicAttackReady(now)) {
            return BasicAttackOutcome.rejected(source, CombatRejectReason.BASIC_ATTACK_COOLDOWN, true, List.of());
        }

        ItemStack weapon = attackStack(player, safeRequest.offHand());
        if (safeRequest.offHand() && !isLegalOffhandAttack(weapon)) {
            return BasicAttackOutcome.rejected(source, CombatRejectReason.INVALID_OFFHAND, true, List.of());
        }

        List<ResolvedTargetCandidate> targetResults = ServerTargetResolver.resolve(player, safeRequest.candidates());
        List<ResolvedTargetCandidate> acceptedTargets = targetResults.stream()
                .filter(ResolvedTargetCandidate::accepted)
                .toList();
        if (acceptedTargets.isEmpty()) {
            return BasicAttackOutcome.rejected(source, CombatRejectReason.NO_VALID_TARGETS, true, targetResults);
        }

        IPlayerStats attackerStats = RagnarCoreAPI.get(player).orElse(null);
        if (attackerStats == null) {
            return BasicAttackOutcome.infrastructureFallback(source,
                    BasicAttackFailureReason.MISSING_ATTACKER_STATS, targetResults);
        }

        List<CombatResolution> resolutions = new ArrayList<>();
        for (ResolvedTargetCandidate target : acceptedTargets) {
            if (player.level().getEntity(target.entityId()) instanceof LivingEntity livingTarget) {
                CombatResolution resolution = resolve(player, attackerStats, livingTarget, weapon);
                resolutions.add(resolution);
                apply(player, livingTarget, resolution);
            }
        }

        state.acceptSequence(safeRequest.sequenceId());
        state.setBasicAttackCooldown(now, AttackCadenceCalculator.computeIntervalTicks(player, weapon, safeRequest.offHand()));
        player.resetAttackStrengthTicker();
        return BasicAttackOutcome.resolved(source, resolutions, targetResults, false);
    }

    private CombatResolution resolve(ServerPlayer attacker, IPlayerStats stats, LivingEntity target, ItemStack weapon) {
        int str = totalStat(attacker, stats, StatKeys.STR);
        int dex = totalStat(attacker, stats, StatKeys.DEX);
        int luk = totalStat(attacker, stats, StatKeys.LUK);
        int level = Math.max(1, stats.getLevel());
        boolean ranged = isRangedWeapon(weapon);

        double attackerHit = AccuracyFormulaService.hit(dex, level, 0.0D);
        double defenderFlee = targetFlee(target);
        double hitRate = AccuracyFormulaService.hitRate(attackerHit, defenderFlee);
        boolean hit = attacker.getRandom().nextDouble() <= hitRate;
        if (!hit) {
            return new CombatResolution(target.getId(), CombatHitResultType.MISS, 0.0D, 0.0D, false, hitRate);
        }

        double critChance = AccuracyFormulaService.criticalChance(luk, 0.0D);
        boolean critical = attacker.getRandom().nextDouble() <= critChance;
        double rawDamage = DamageFormulaService.damageVariance(
                DamageFormulaService.totalAtk(str, dex, luk, weaponAttack(attacker, weapon), 0.0D, ranged),
                dex,
                luk,
                new java.util.Random(attacker.getRandom().nextLong()));
        if (critical) {
            rawDamage *= DamageFormulaService.critDamageMultiplier();
        }

        double finalDamage = DefenseFormulaService.applyPhysicalDefense(rawDamage, targetSoftDefense(target),
                DefenseFormulaService.physicalDamageReduction(targetHardDefense(target)));
        return new CombatResolution(target.getId(), critical ? CombatHitResultType.CRIT : CombatHitResultType.HIT,
                rawDamage, finalDamage, critical, hitRate);
    }

    private void apply(ServerPlayer attacker, LivingEntity target, CombatResolution resolution) {
        if (!resolution.dealsDamage() || resolution.finalDamage() <= 0.0D) {
            return;
        }
        target.invulnerableTime = 0;
        boolean damaged = target.hurt(attacker.damageSources().playerAttack(attacker), (float) resolution.finalDamage());
        if (damaged) {
            target.invulnerableTime = 0;
        }
    }

    private static int totalStat(ServerPlayer player, IPlayerStats stats, StatKeys key) {
        double value = StatAttributes.getTotal(player, key);
        if (value > 0.0D) {
            return (int) Math.round(value);
        }
        return Math.max(1, stats.get(key));
    }

    private static double weaponAttack(ServerPlayer player, ItemStack weapon) {
        if (weapon == player.getMainHandItem()) {
            return mainHandAttack(player);
        }
        double attack = 1.0D;
        for (var modifier : weapon.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION) {
                attack += modifier.getAmount();
            }
        }
        return Math.max(1.0D, attack);
    }

    private static double mainHandAttack(ServerPlayer player) {
        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        return attribute != null ? Math.max(1.0D, attribute.getValue()) : 1.0D;
    }

    private static ItemStack attackStack(ServerPlayer player, boolean offHand) {
        return offHand ? player.getOffhandItem() : player.getMainHandItem();
    }

    private static boolean isLegalOffhandAttack(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof ShieldItem) && !(stack.getItem() instanceof ProjectileWeaponItem);
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ProjectileWeaponItem;
    }

    private static double targetFlee(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return Math.max(0.0D, StatAttributes.getTotal(player, StatKeys.AGI) + player.experienceLevel);
        }
        var movement = target.getAttribute(Attributes.MOVEMENT_SPEED);
        double speed = movement != null ? movement.getValue() : 0.2D;
        return Math.max(0.0D, speed * 100.0D);
    }

    private static double targetSoftDefense(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return DefenseFormulaService.softDef((int) Math.round(StatAttributes.getTotal(player, StatKeys.VIT)));
        }
        return Math.max(0.0D, target.getArmorValue() * 0.5D);
    }

    private static double targetHardDefense(LivingEntity target) {
        return Math.max(0.0D, target.getArmorValue());
    }
}
