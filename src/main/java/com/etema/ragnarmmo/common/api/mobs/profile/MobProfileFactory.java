package com.etema.ragnarmmo.common.api.mobs.profile;

import com.etema.ragnarmmo.common.api.mobs.MobRank;
import com.etema.ragnarmmo.common.api.mobs.data.MobDirectStatsBlock;
import com.etema.ragnarmmo.common.api.mobs.data.MobRoStatsBlock;
import com.etema.ragnarmmo.common.api.mobs.data.ResolvedMobDefinition;
import com.etema.ragnarmmo.common.api.stats.RoBaseStats;
import com.etema.ragnarmmo.player.stats.compute.RoPreRenewalFormulaService;

import java.util.Optional;

public final class MobProfileFactory {
    private static final Defaults DEFAULTS = new Defaults(20, 2, 5, 0, 0, 5, 2, 10, 5, 1, 150, 0.23D,
            "brute", "neutral", 1, "medium", 2);

    public MobProfile create(int level, MobRank rank, Optional<ResolvedMobDefinition> definition) {
        ResolvedMobDefinition authored = definition.orElse(null);
        int runtimeLevel = Math.max(1, authored != null && authored.level() != null ? authored.level() : level);
        MobRank runtimeRank = normalizeRank(authored != null && authored.rank() != null ? authored.rank() : rank);
        RankModifiers modifiers = RankModifiers.forRank(runtimeRank);
        RoBaseStats baseStats = resolveBaseStats(authored, runtimeLevel, runtimeRank);
        MobDirectStatsBlock direct = authored == null ? null : authored.directStats();

        int maxHp = directInt(direct == null ? null : direct.maxHp(),
                modifiers.applyInt(DEFAULTS.maxHp() + scaled(runtimeLevel, 14.0D), ScaledStat.HP));
        int atkMin = directInt(direct == null ? null : direct.atkMin(),
                modifiers.applyInt(DEFAULTS.atkMin() + scaled(runtimeLevel, 1.2D), ScaledStat.ATK));
        int atkMax = Math.max(atkMin, directInt(direct == null ? null : direct.atkMax(),
                modifiers.applyInt(DEFAULTS.atkMax() + scaled(runtimeLevel, 1.8D), ScaledStat.ATK)));
        int matkMin = directInt(direct == null ? null : direct.matkMin(), Math.max(0, (int) Math.round(atkMin * 0.75D)));
        int matkMax = Math.max(matkMin, directInt(direct == null ? null : direct.matkMax(),
                Math.max(matkMin, (int) Math.round(atkMax * 0.85D))));
        int def = directInt(direct == null ? null : direct.def(),
                modifiers.applyInt(DEFAULTS.def() + scaled(runtimeLevel, 0.35D), ScaledStat.DEF));
        int mdef = directInt(direct == null ? null : direct.mdef(),
                modifiers.applyInt(DEFAULTS.mdef() + scaled(runtimeLevel, 0.25D), ScaledStat.DEF));
        int hit = directInt(direct == null ? null : direct.hit(),
                modifiers.applyInt((int) Math.round(RoPreRenewalFormulaService.hit(baseStats.dex(), runtimeLevel, DEFAULTS.hit())),
                        ScaledStat.ACCURACY));
        int flee = directInt(direct == null ? null : direct.flee(),
                modifiers.applyInt((int) Math.round(RoPreRenewalFormulaService.flee(baseStats.agi(), runtimeLevel, DEFAULTS.flee())),
                        ScaledStat.ACCURACY));
        int crit = directInt(direct == null ? null : direct.crit(),
                modifiers.applyInt((int) Math.round(RoPreRenewalFormulaService.criticalChance(baseStats.luk(),
                        DEFAULTS.crit() / 100.0D) * 100.0D), ScaledStat.CRIT));
        int aspd = directInt(direct == null ? null : direct.aspd(),
                modifiers.applyInt(DEFAULTS.aspd() + scaled(runtimeLevel, 0.08D), ScaledStat.ASPD));
        double moveSpeed = direct == null || direct.moveSpeed() == null
                ? Math.min(0.45D, DEFAULTS.moveSpeed() + runtimeLevel * 0.001D)
                : Math.max(0.01D, Math.min(0.45D, direct.moveSpeed()));
        int baseExp = authored != null && authored.baseExp() != null ? authored.baseExp() : MobRewardFormula.baseExp(runtimeLevel, runtimeRank);
        int jobExp = authored != null && authored.jobExp() != null ? authored.jobExp() : MobRewardFormula.jobExp(runtimeLevel, runtimeRank);

        return new MobProfile(
                runtimeLevel,
                runtimeRank,
                baseStats,
                maxHp,
                atkMin,
                atkMax,
                matkMin,
                matkMax,
                def,
                mdef,
                hit,
                flee,
                crit,
                aspd,
                moveSpeed,
                baseExp,
                jobExp,
                stringOr(authored == null ? null : authored.race(), DEFAULTS.race()),
                stringOr(authored == null ? null : authored.element(), DEFAULTS.element()),
                elementLevelOr(authored == null ? null : authored.elementLevel(), DEFAULTS.elementLevel()),
                stringOr(authored == null ? null : authored.size(), DEFAULTS.size()),
                nonNegativeOr(authored == null ? null : authored.attackRange(), DEFAULTS.attackRange()));
    }

    private static RoBaseStats resolveBaseStats(ResolvedMobDefinition authored, int level, MobRank rank) {
        if (authored != null && authored.roStats() != null) {
            MobRoStatsBlock stats = authored.roStats();
            return new RoBaseStats(
                    positiveOr(stats.str(), proceduralBaseStat(level, 0.80D, rank)),
                    positiveOr(stats.agi(), proceduralBaseStat(level, 0.70D, rank)),
                    positiveOr(stats.vit(), proceduralBaseStat(level, 0.80D, rank)),
                    positiveOr(stats.int_(), proceduralBaseStat(level, 0.50D, rank)),
                    positiveOr(stats.dex(), proceduralBaseStat(level, 0.75D, rank)),
                    positiveOr(stats.luk(), proceduralBaseStat(level, 0.20D, rank)));
        }
        return new RoBaseStats(
                proceduralBaseStat(level, 0.80D, rank),
                proceduralBaseStat(level, 0.70D, rank),
                proceduralBaseStat(level, 0.80D, rank),
                proceduralBaseStat(level, 0.50D, rank),
                proceduralBaseStat(level, 0.75D, rank),
                proceduralBaseStat(level, 0.20D, rank));
    }

    private static int proceduralBaseStat(int level, double perLevel, MobRank rank) {
        return Math.max(1, (int) Math.round(Math.max(1, level) * perLevel * baseStatRankMultiplier(rank)));
    }

    private static double baseStatRankMultiplier(MobRank rank) {
        return switch (rank == null ? MobRank.NORMAL : rank) {
            case NORMAL -> 1.0D;
            case ELITE -> 1.20D;
            case BOSS -> 1.50D;
        };
    }

    private static MobRank normalizeRank(MobRank rank) {
        return rank == null ? MobRank.NORMAL : rank;
    }

    private static int directInt(Integer authored, int fallback) {
        return authored == null ? Math.max(0, fallback) : Math.max(0, authored);
    }

    private static int positiveOr(Integer value, int fallback) {
        return value == null ? fallback : Math.max(1, value);
    }

    private static int scaled(int level, double perLevel) {
        return (int) Math.round(Math.max(0, level) * perLevel);
    }

    private static String stringOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int elementLevelOr(Integer value, int fallback) {
        return Math.max(1, Math.min(4, value == null ? fallback : value));
    }

    private static int nonNegativeOr(Integer value, int fallback) {
        return Math.max(0, value == null ? fallback : value);
    }

    private enum ScaledStat {
        HP,
        ATK,
        DEF,
        ACCURACY,
        CRIT,
        ASPD
    }

    private record RankModifiers(double hp, double atk, double def, double accuracy, double crit, double aspd) {
        static RankModifiers forRank(MobRank rank) {
            return switch (rank == null ? MobRank.NORMAL : rank) {
                case NORMAL -> new RankModifiers(1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D);
                case ELITE -> new RankModifiers(2.2D, 1.35D, 1.25D, 1.15D, 1.35D, 1.05D);
                case BOSS -> new RankModifiers(8.0D, 2.0D, 1.8D, 1.35D, 1.8D, 1.1D);
            };
        }

        int applyInt(int value, ScaledStat stat) {
            double multiplier = switch (stat) {
                case HP -> hp;
                case ATK -> atk;
                case DEF -> def;
                case ACCURACY -> accuracy;
                case CRIT -> crit;
                case ASPD -> aspd;
            };
            return Math.max(0, (int) Math.round(value * multiplier));
        }
    }

    private record Defaults(int maxHp, int atkMin, int atkMax, int matkMin, int matkMax, int def, int mdef,
            int hit, int flee, int crit, int aspd, double moveSpeed, String race, String element, int elementLevel,
            String size, int attackRange) {
    }
}
