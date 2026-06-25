package com.etema.ragnarmmo.player.stats.compute;

import com.etema.ragnarmmo.common.api.attributes.RagnarAttributes;
import com.etema.ragnarmmo.common.api.compute.DerivedStats;
import com.etema.ragnarmmo.common.api.events.StatComputeEvent;
import com.etema.ragnarmmo.common.api.stats.IPlayerStats;
import com.etema.ragnarmmo.common.api.stats.StatAttributes;
import com.etema.ragnarmmo.common.api.stats.StatKeys;
import com.etema.ragnarmmo.combat.formula.AcolyteSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.ArcherSkillFormulaService;
import com.etema.ragnarmmo.combat.formula.WeaponAspdTableService;
import com.etema.ragnarmmo.combat.status.RoCombatStatusService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.MinecraftForge;

public final class CoreDerivedStatsCalculator {
    private static final int DEFAULT_WEAPON_ASPD = 156;

    private CoreDerivedStatsCalculator() {
    }

    public static DerivedStats compute(ServerPlayer player, IPlayerStats stats) {
        int str = totalStat(player, stats, StatKeys.STR);
        int agi = totalStat(player, stats, StatKeys.AGI);
        int vit = totalStat(player, stats, StatKeys.VIT);
        int intel = totalStat(player, stats, StatKeys.INT);
        int dex = totalStat(player, stats, StatKeys.DEX);
        int luk = totalStat(player, stats, StatKeys.LUK);
        int level = Math.max(1, stats.getLevel());

        DerivedStats derived = new DerivedStats();
        applyPhysicalOffense(player, derived, str, dex, luk, level);
        applyMagicalOffense(derived, intel);
        applyDefense(player, derived, vit, agi, intel, level);
        applyResources(derived, vit, intel, level, stats);
        applyTiming(player, derived, agi, dex);
        applyExtendedAttributes(player, derived);

        MinecraftForge.EVENT_BUS.post(new StatComputeEvent(player, stats, derived));
        return derived;
    }

    private static int totalStat(ServerPlayer player, IPlayerStats stats, StatKeys key) {
        if (player != null) {
            double total = StatAttributes.getTotal(player, key);
            if (total > 0.0D) {
                return Math.max(1, (int) Math.round(total)
                        + AcolyteSkillFormulaService.statusStatModifier(player, key)
                        + ArcherSkillFormulaService.statusStatModifier(player, key));
            }
        }
        return Math.max(1, stats.get(key));
    }

    private static void applyPhysicalOffense(ServerPlayer player, DerivedStats derived, int str, int dex, int luk, int level) {
        boolean ranged = player != null && player.getMainHandItem().getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem;
        double statusAtk = statusAtk(str, dex, luk, ranged);
        derived.physicalAttack = statusAtk;
        derived.physicalAttackMin = Math.max(0.0D, damageVarianceFloor(statusAtk, dex, luk));
        derived.physicalAttackMax = statusAtk;
        derived.accuracy = RoPreRenewalFormulaService.hit(dex, level, 0.0D)
                * RoCombatStatusService.hitMultiplier(player);
        derived.criticalChance = RoPreRenewalFormulaService.criticalChance(luk, critChanceBonus(player));
        derived.criticalDamageMultiplier = 1.4D + critDamageBonus(player);
        derived.perfectDodge = RoPreRenewalFormulaService.perfectDodge(luk);
    }

    private static void applyMagicalOffense(DerivedStats derived, int intel) {
        derived.magicAttackMin = intel + Math.pow(Math.floor(intel / 7.0D), 2.0D);
        derived.magicAttackMax = intel + Math.pow(Math.floor(intel / 5.0D), 2.0D);
        derived.magicAttack = (derived.magicAttackMin + derived.magicAttackMax) * 0.5D;
    }

    private static void applyDefense(ServerPlayer player, DerivedStats derived, int vit, int agi, int intel, int level) {
        derived.softDefense = Math.max(0.0D, Math.floor(vit * 0.5D)
                + Math.max(Math.floor(vit * 0.3D), Math.floor((vit * vit) / 150.0D) - 1.0D));
        derived.softDefense *= RoCombatStatusService.angelusSoftDefenseMultiplier(player);
        derived.hardDefense = 0.0D;
        derived.defense = derived.softDefense;
        derived.physicalDamageReduction = 0.0D;

        derived.softMagicDefense = Math.max(0.0D, intel + Math.floor(vit / 2.0D));
        derived.hardMagicDefense = RoCombatStatusService.endureMdefBonus(player);
        derived.magicDefense = derived.softMagicDefense;
        derived.magicDamageReduction = 0.0D;
        derived.flee = RoPreRenewalFormulaService.flee(agi, level, 0.0D)
                * RoCombatStatusService.fleeMultiplier(player);
    }

    private static void applyResources(DerivedStats derived, int vit, int intel, int level, IPlayerStats stats) {
        derived.maxHealth = maxHp(vit, level, stats.getJobId());
        derived.healthRegenPerSecond = hpRegen(vit, derived.maxHealth);
        derived.maxSP = maxSp(intel, level, stats.getJobId());
        derived.spRegenPerSecond = spRegen(intel, derived.maxSP);
        derived.maxMana = derived.maxSP;
        derived.manaRegenPerSecond = derived.spRegenPerSecond;
    }

    private static void applyTiming(ServerPlayer player, DerivedStats derived, int agi, int dex) {
        int baseAspd = player != null ? WeaponAspdTableService.baseAspd(player, player.getMainHandItem()) : DEFAULT_WEAPON_ASPD;
        int aspdRo = RoPreRenewalFormulaService.aspdRo(baseAspd, false, agi, dex, 0.0D);
        double attacksPerSecond = RoPreRenewalFormulaService.aspdToAttacksPerSecond(aspdRo);
        derived.attackSpeed = aspdRo;
        derived.globalCooldown = attacksPerSecond > 0.0D ? 1.0D / attacksPerSecond : 0.0D;
        derived.castTime = RoPreRenewalFormulaService.variableCastSeconds(1.0D, dex, 0.0D);
    }

    private static void applyExtendedAttributes(ServerPlayer player, DerivedStats derived) {
        derived.lifeSteal = attributeValue(player, RagnarAttributes.LIFE_STEAL.get(), 0.0D);
        derived.armorPierce = attributeValue(player, RagnarAttributes.ARMOR_PIERCE.get(), 0.0D);
        derived.armorShred = attributeValue(player, RagnarAttributes.ARMOR_SHRED.get(), 0.0D);
        derived.overheal = attributeValue(player, RagnarAttributes.OVERHEAL.get(), 0.0D);
    }

    private static double statusAtk(int str, int dex, int luk, boolean ranged) {
        if (ranged) {
            return dex + Math.pow(Math.floor(dex / 10.0D), 2.0D) + Math.floor(str / 5.0D) + Math.floor(luk / 5.0D);
        }
        return str + Math.pow(Math.floor(str / 10.0D), 2.0D) + Math.floor(dex / 5.0D) + Math.floor(luk / 5.0D);
    }

    private static double damageVarianceFloor(double baseDamage, int dex, int luk) {
        double dexFactor = clamp(0.0D, 1.0D, dex / 150.0D);
        double lukBonus = luk / 300.0D;
        double floor = clamp(0.8D, 1.0D, 0.8D + 0.2D * (dexFactor + lukBonus));
        return baseDamage * floor;
    }

    private static double maxHp(int vit, int level, String jobId) {
        double hpBase = 35.0D + (level * 5.0D * hpJobMultiplier(jobId));
        return Math.floor(hpBase * (1.0D + vit / 100.0D));
    }

    private static double hpRegen(int vit, double maxHp) {
        return Math.min(1.0D + vit * 0.2D, maxHp * 0.02D);
    }

    private static double maxSp(int intel, int level, String jobId) {
        double spBase = 100.0D + ((level - 1.0D) * 3.0D * spJobMultiplier(jobId));
        return Math.floor(spBase * (1.0D + intel / 100.0D));
    }

    private static double spRegen(int intel, double maxSp) {
        return Math.min(maxSp * (0.01D + intel * 0.002D), maxSp * 0.05D);
    }

    private static double hpJobMultiplier(String jobId) {
        return switch (com.etema.ragnarmmo.common.api.jobs.JobType.fromId(jobId)) {
            case SWORDSMAN -> 1.5D;
            case THIEF, MERCHANT -> 1.2D;
            case MAGE -> 0.8D;
            default -> 1.0D;
        };
    }

    private static double spJobMultiplier(String jobId) {
        return switch (com.etema.ragnarmmo.common.api.jobs.JobType.fromId(jobId)) {
            case MAGE, ACOLYTE -> 1.5D;
            case ARCHER -> 1.2D;
            case THIEF -> 0.8D;
            case SWORDSMAN -> 0.7D;
            default -> 1.0D;
        };
    }

    private static double critChanceBonus(ServerPlayer player) {
        return attributeValue(player, RagnarAttributes.CRIT_CHANCE.get(), 0.0D);
    }

    private static double critDamageBonus(ServerPlayer player) {
        return Math.max(0.0D, attributeValue(player, RagnarAttributes.CRIT_DAMAGE.get(), 1.5D) - 1.5D);
    }

    private static double attributeValue(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute, double fallback) {
        if (player == null || attribute == null) {
            return fallback;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        return instance != null ? instance.getValue() : fallback;
    }

    private static double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }
}
