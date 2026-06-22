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
import com.etema.ragnarmmo.combat.contract.CombatContract;
import com.etema.ragnarmmo.combat.credit.RoKillCreditService;
import com.etema.ragnarmmo.combat.element.CombatPropertyResolver;
import com.etema.ragnarmmo.combat.element.ElementProperty;
import com.etema.ragnarmmo.combat.element.ElementType;
import com.etema.ragnarmmo.combat.formula.CombatPropertyModifierService;
import com.etema.ragnarmmo.combat.formula.AccuracyFormulaService;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.ArcherSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.DamageFormulaService;
import com.etema.ragnarmmo.combat.formula.DefenseFormulaService;
import com.etema.ragnarmmo.combat.formula.ThiefSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.SwordmanSkillFormulaService;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import com.etema.ragnarmmo.combat.hand.AttackHandResolver;
import com.etema.ragnarmmo.combat.resolver.MobCombatProfileResolver;
import com.etema.ragnarmmo.combat.state.CombatActorState;
import com.etema.ragnarmmo.combat.targeting.ServerTargetResolver;
import com.etema.ragnarmmo.combat.timing.AttackCadenceCalculator;
import com.etema.ragnarmmo.core.api.stats.DerivedStatsService;
import com.etema.ragnarmmo.items.runtime.RangedWeaponStatsHelper;
import com.etema.ragnarmmo.items.runtime.WeaponStatHelper;
import com.etema.ragnarmmo.jobs.player.PlayerJobSkillsProvider;
import com.etema.ragnarmmo.player.stats.compute.CoreDerivedStatsCalculator;
import com.etema.ragnarmmo.player.stats.compute.CombatMath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.BowItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RagnarCombatEngine {
    private static final RagnarCombatEngine INSTANCE = new RagnarCombatEngine();

    private final Map<UUID, CombatActorState> actorStates = new ConcurrentHashMap<>();
    private final RagnarCombatFeedbackService feedbackService = new RagnarCombatFeedbackService();

    private RagnarCombatEngine() {
    }

    public static RagnarCombatEngine get() {
        return INSTANCE;
    }

    public java.util.List<CombatResolution> handleSkillUseRequest(com.etema.ragnarmmo.combat.api.CombatRequestContext ctx) {
        if (ctx == null || ctx.actor() == null) {
            return java.util.List.of();
        }
        long nowTick = ctx.actor().serverLevel().getGameTime();
        CombatActorState actorState = actorStates.computeIfAbsent(ctx.actor().getUUID(), ignored -> new CombatActorState());
        java.util.List<CombatResolution> resolutions = new RagnarSkillResolver(new RagnarHitCalculator(), new RagnarDamageCalculator(),
                new CombatContract(new RagnarHitCalculator(), new RagnarDamageCalculator()))
                .resolveSkill(ctx, actorState, nowTick);
        for (CombatResolution resolution : resolutions) {
            if (ctx.actor().level().getEntity(resolution.targetEntityId()) instanceof LivingEntity target) {
                apply(ctx.actor(), target, resolution);
            }
        }
        return resolutions;
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
        state.setBasicAttackCooldown(now, AttackCadenceCalculator.computeIntervalTicks(player, safeRequest.offHand()));
        player.resetAttackStrengthTicker();
        return BasicAttackOutcome.resolved(source, resolutions, targetResults, false);
    }

    private CombatResolution resolve(ServerPlayer attacker, IPlayerStats stats, LivingEntity target, ItemStack weapon) {
        int str = totalStat(attacker, stats, StatKeys.STR);
        int dex = totalStat(attacker, stats, StatKeys.DEX);
        int luk = totalStat(attacker, stats, StatKeys.LUK);
        int level = Math.max(1, stats.getLevel());
        boolean ranged = isRangedWeapon(weapon);

        DoubleAttackRoll doubleAttack = rollDoubleAttack(attacker, weapon);
        double attackerHit = AccuracyFormulaService.hit(dex, level, doubleAttack.hitBonus());
        double defenderFlee = targetFlee(target);
        double hitRate = AccuracyFormulaService.hitRate(attackerHit, defenderFlee);
        if (attacker.getRandom().nextDouble() < targetPerfectDodge(target)) {
            return new CombatResolution(target.getId(), CombatHitResultType.DODGE, 0.0D, 0.0D, false, 0.0D);
        }

        double critChance = Math.max(0.0D,
                AccuracyFormulaService.criticalChance(luk, 0.0D) - targetCritShield(target) / 100.0D);
        boolean critical = attacker.getRandom().nextDouble() <= critChance;
        boolean hit = critical || attacker.getRandom().nextDouble() <= hitRate;
        if (!hit) {
            return new CombatResolution(target.getId(), CombatHitResultType.MISS, 0.0D, 0.0D, false, hitRate);
        }

        java.util.Random formulaRng = new java.util.Random(attacker.getRandom().nextLong());
        double statusAtk = DamageFormulaService.statusAtk(str, dex, luk, ranged);
        double sizePenalty = CombatMath.getWeaponSizePenalty(weapon, CombatPropertyResolver.getEntitySize(target));
        double weaponAtk = ranged
                ? DamageFormulaService.bowWeaponAtkRoll(weaponAttack(attacker, weapon), arrowAttack(weapon), dex,
                        weaponLevel(weapon), critical, formulaRng)
                : DamageFormulaService.meleeWeaponAtkRoll(weaponAttack(attacker, weapon), dex, weaponLevel(weapon),
                        critical, formulaRng);
        double damageBeforeDefense = (statusAtk + weaponAtk * sizePenalty)
                * RoCombatStatusService.physicalAttackMultiplier(attacker);
        if (critical) {
            damageBeforeDefense *= DamageFormulaService.critDamageMultiplier();
        }

        double afterDefense = DefenseFormulaService.applyPhysicalDefense(
                damageBeforeDefense,
                adjustedTargetSoftDefense(target, formulaRng),
                adjustedTargetHardDefense(target),
                critical);
        afterDefense += SwordmanSkillFormulaService.weaponMasteryBonus(attacker, weapon);
        afterDefense += AcolyteSkillFormulaService.demonBaneBonus(attacker, target);
        ElementType attackElement = CombatPropertyResolver.getAttackElement(weapon);
        ElementProperty targetElement = CombatPropertyResolver.getDefensiveElementProperty(target);
        double outgoingMultiplier = CombatPropertyModifierService.outgoingDamageMultiplier(attacker,
                CombatPropertyResolver.getRace(target),
                targetElement.type(),
                CombatPropertyResolver.getEntitySize(target));
        double finalDamage = afterDefense
                * DamageFormulaService.elementMultiplier(attackElement, targetElement.type(), targetElement.level())
                * outgoingMultiplier;
        finalDamage *= doubleAttack.hitCount();
        double magnumBonus = RoCombatStatusService.magnumBreakFireBonusRatio(attacker);
        if (magnumBonus > 0.0D) {
            finalDamage += damageBeforeDefense
                    * magnumBonus
                    * DamageFormulaService.elementMultiplier(ElementType.FIRE, targetElement.type(), targetElement.level())
                    * outgoingMultiplier;
        }

        return new CombatResolution(target.getId(), critical ? CombatHitResultType.CRIT : CombatHitResultType.HIT,
                damageBeforeDefense, positiveDamageOrZero(finalDamage), critical, hitRate);
    }

    private void apply(ServerPlayer attacker, LivingEntity target, CombatResolution resolution) {
        if (!resolution.dealsDamage() || resolution.finalDamage() <= 0.0D) {
            RoKillCreditService.clearPlayerContribution(attacker, target);
            feedbackService.sendBasicAttackFeedback(attacker, target, resolution);
            return;
        }
        var damageSource = attacker.damageSources().playerAttack(attacker);
        RoKillCreditService.recordPlayerContribution(attacker, target, resolution);
        target.invulnerableTime = 0;
        boolean damaged = target.hurt(damageSource, (float) resolution.finalDamage());
        if (damaged) {
            target.invulnerableTime = 0;
        } else {
            RoKillCreditService.clearPlayerContribution(attacker, target);
        }
        feedbackService.sendBasicAttackFeedback(attacker, target, resolution);
    }

    private static int totalStat(ServerPlayer player, IPlayerStats stats, StatKeys key) {
        double value = StatAttributes.getTotal(player, key);
        if (value > 0.0D) {
            return Math.max(1, (int) Math.round(value)
                    + AcolyteSkillFormulaService.statusStatModifier(player, key)
                    + ArcherSkillFormulaService.statusStatModifier(player, key));
        }
        return Math.max(1, stats.get(key));
    }

    private static double weaponAttack(ServerPlayer player, ItemStack weapon) {
        double configuredAttack = WeaponStatHelper.getConfiguredPhysicalAttackBase(weapon);
        if (configuredAttack > 0.0D) {
            return configuredAttack;
        }
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

    private static double arrowAttack(ItemStack weapon) {
        if (weapon != null && weapon.getItem() instanceof BowItem) {
            return 25.0D;
        }
        return 0.0D;
    }

    private static DoubleAttackRoll rollDoubleAttack(ServerPlayer player, ItemStack weapon) {
        if (player == null || weapon == null
                || !weapon.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("ragnarmmo", "daggers")))) {
            return DoubleAttackRoll.inactive();
        }
        int level = PlayerJobSkillsProvider.get(player)
                .map(skills -> skills.getSkillLevel(ResourceLocation.fromNamespaceAndPath("ragnarmmo", "double_attack")))
                .orElse(0);
        if (level <= 0 || player.getRandom().nextDouble() >= ThiefSkillFormulaService.doubleAttackChance(level)) {
            return DoubleAttackRoll.inactive();
        }
        return new DoubleAttackRoll(ThiefSkillFormulaService.doubleAttackHitBonus(level), 2);
    }

    private record DoubleAttackRoll(int hitBonus, int hitCount) {
        static DoubleAttackRoll inactive() {
            return new DoubleAttackRoll(0, 1);
        }
    }

    private static int weaponLevel(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty() || weapon.getTag() == null) {
            return 1;
        }
        var tag = weapon.getTag();
        if (tag.contains("ragnarmmo_weapon_level")) {
            return Math.max(1, Math.min(4, tag.getInt("ragnarmmo_weapon_level")));
        }
        if (tag.contains("ro_weapon_level")) {
            return Math.max(1, Math.min(4, tag.getInt("ro_weapon_level")));
        }
        return 1;
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
        return !stack.isEmpty() && (stack.getItem() instanceof ProjectileWeaponItem
                || RangedWeaponStatsHelper.hasManualProfile(stack));
    }

    private static double targetFlee(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return RagnarCoreAPI.get(player)
                    .map(stats -> AccuracyFormulaService.flee(
                            Math.max(1, (int) Math.round(StatAttributes.getTotal(player, StatKeys.AGI))
                                    + AcolyteSkillFormulaService.statusStatModifier(player, StatKeys.AGI)
                                    + ArcherSkillFormulaService.statusStatModifier(player, StatKeys.AGI)),
                            Math.max(1, stats.getLevel()),
                            0.0D))
                    .orElseGet(() -> Math.max(0.0D, StatAttributes.getTotal(player, StatKeys.AGI)));
        }
        var mobFlee = MobCombatProfileResolver.tryGetResolvedMobFlee(target);
        if (mobFlee.isPresent()) {
            return Math.max(0.0D, mobFlee.getAsInt() - RoCombatStatusService.agiPenalty(target));
        }
        var movement = target.getAttribute(Attributes.MOVEMENT_SPEED);
        double speed = movement != null ? movement.getValue() : 0.2D;
        return Math.max(0.0D, speed * 100.0D);
    }

    private static double targetSoftDefense(LivingEntity target, java.util.Random rng) {
        if (target instanceof ServerPlayer player) {
            return DefenseFormulaService.playerSoftDefRoll(
                    Math.max(1, (int) Math.round(StatAttributes.getTotal(player, StatKeys.VIT))
                            + AcolyteSkillFormulaService.statusStatModifier(player, StatKeys.VIT)
                            + ArcherSkillFormulaService.statusStatModifier(player, StatKeys.VIT)),
                    rng);
        }
        var mobVit = MobCombatProfileResolver.tryGetResolvedMobSoftDefense(target);
        if (mobVit.isPresent()) {
            return DefenseFormulaService.mobSoftDefRoll(mobVit.getAsInt(), rng);
        }
        return Math.max(0.0D, target.getArmorValue() * 0.5D);
    }

    private static double adjustedTargetSoftDefense(LivingEntity target, java.util.Random rng) {
        double softDefense = targetSoftDefense(target, rng);
        if (target instanceof ServerPlayer) {
            return softDefense * RoCombatStatusService.physicalDefenseMultiplier(target);
        }
        return softDefense * RoCombatStatusService.physicalDefenseMultiplier(target);
    }

    private static double targetHardDefense(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return RagnarCoreAPI.get(player)
                    .map(stats -> DerivedStatsService.compute(player, stats)
                            .orElseGet(() -> CoreDerivedStatsCalculator.compute(player, stats))
                            .hardDefense)
                    .orElseGet(() -> Math.max(0.0D, target.getArmorValue()));
        }
        var mobHardDef = MobCombatProfileResolver.tryGetResolvedMobHardDefense(target);
        if (mobHardDef.isPresent()) {
            return mobHardDef.getAsInt();
        }
        return Math.max(0.0D, target.getArmorValue());
    }

    private static double adjustedTargetHardDefense(LivingEntity target) {
        double hardDefense = targetHardDefense(target);
        if (target instanceof ServerPlayer) {
            return hardDefense;
        }
        return hardDefense * RoCombatStatusService.physicalDefenseMultiplier(target);
    }

    private static double targetPerfectDodge(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return AccuracyFormulaService.perfectDodge(Math.max(1,
                    (int) Math.round(StatAttributes.getTotal(player, StatKeys.LUK))
                            + AcolyteSkillFormulaService.statusStatModifier(player, StatKeys.LUK)
                            + ArcherSkillFormulaService.statusStatModifier(player, StatKeys.LUK)));
        }
        return 0.0D;
    }

    private static double targetCritShield(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            int luk = Math.max(1, (int) Math.round(StatAttributes.getTotal(player, StatKeys.LUK))
                    + AcolyteSkillFormulaService.statusStatModifier(player, StatKeys.LUK)
                    + ArcherSkillFormulaService.statusStatModifier(player, StatKeys.LUK));
            return Math.floor(Math.max(0, luk) / 5.0D);
        }
        return 0.0D;
    }

    private static double positiveDamageOrZero(double damage) {
        return damage > 0.0D ? Math.max(1.0D, damage) : 0.0D;
    }
}
