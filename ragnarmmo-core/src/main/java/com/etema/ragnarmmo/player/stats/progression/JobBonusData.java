package com.etema.ragnarmmo.player.stats.progression;

import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.common.api.stats.Stats6;

import java.util.EnumMap;
import java.util.Map;

public final class JobBonusData {

    private static final int STR = 0;
    private static final int AGI = 1;
    private static final int VIT = 2;
    private static final int INT = 3;
    private static final int DEX = 4;
    private static final int LUK = 5;

    private static final Map<JobType, Stats6[]> TABLES = new EnumMap<>(JobType.class);

    static {
        TABLES.put(JobType.NOVICE, createEmptyTable(10));

        TABLES.put(JobType.SWORDSMAN, buildTable(50,
                point(4, STR), point(6, DEX), point(8, VIT), point(11, STR), point(13, AGI),
                point(21, STR), point(24, LUK), point(26, VIT), point(29, DEX), point(31, STR),
                point(34, LUK), point(36, AGI), point(38, STR), point(41, DEX), point(42, STR),
                point(45, VIT), point(46, STR), point(49, STR), point(50, STR)));

        TABLES.put(JobType.MAGE, buildTable(50,
                point(2, INT), point(6, DEX), point(10, DEX), point(14, INT), point(18, AGI),
                point(22, INT), point(26, AGI), point(30, LUK), point(33, INT), point(36, DEX),
                point(38, INT), point(40, AGI), point(42, LUK), point(44, INT), point(46, INT),
                point(47, AGI), point(49, LUK), point(50, INT)));

        TABLES.put(JobType.ARCHER, buildTable(50,
                point(2, DEX), point(6, STR), point(10, INT), point(14, DEX), point(18, DEX),
                point(22, LUK), point(26, AGI), point(30, DEX), point(33, AGI), point(36, DEX),
                point(38, STR), point(40, STR), point(42, DEX), point(44, LUK), point(46, VIT),
                point(47, INT), point(49, AGI), point(50, DEX)));

        TABLES.put(JobType.THIEF, buildTable(50,
                point(2, AGI), point(6, STR), point(10, DEX), point(14, VIT), point(18, INT),
                point(22, DEX), point(26, LUK), point(30, STR), point(33, AGI), point(36, AGI),
                point(38, STR), point(40, LUK), point(42, DEX), point(44, VIT), point(46, LUK),
                point(47, STR), point(49, DEX), point(50, AGI)));

        TABLES.put(JobType.MERCHANT, buildTable(50,
                point(2, VIT), point(6, DEX), point(10, STR), point(14, DEX), point(18, VIT),
                point(22, STR), point(26, INT), point(30, VIT), point(33, AGI), point(36, LUK),
                point(38, DEX), point(40, STR), point(42, DEX), point(44, STR), point(46, LUK),
                point(47, VIT), point(49, STR), point(50, DEX)));

        TABLES.put(JobType.ACOLYTE, buildTable(50,
                point(2, LUK), point(6, VIT), point(10, INT), point(14, DEX), point(18, LUK),
                point(22, AGI), point(26, STR), point(30, VIT), point(33, INT), point(36, DEX),
                point(38, LUK), point(40, AGI), point(42, STR), point(44, VIT), point(46, INT),
                point(47, DEX), point(49, STR), point(50, LUK)));

    }

    private JobBonusData() {
    }

    private static Stats6[] createEmptyTable(int maxLevel) {
        Stats6[] table = new Stats6[maxLevel + 1];
        for (int level = 0; level <= maxLevel; level++) {
            table[level] = Stats6.ZERO;
        }
        return table;
    }

    private static Stats6[] buildTable(int maxLevel, int[]... milestones) {
        Stats6[] table = new Stats6[maxLevel + 1];
        int[] running = new int[6];
        table[0] = Stats6.ZERO;

        int milestoneIndex = 0;
        for (int level = 1; level <= maxLevel; level++) {
            while (milestoneIndex < milestones.length && milestones[milestoneIndex][0] == level) {
                running[milestones[milestoneIndex][1]]++;
                milestoneIndex++;
            }
            table[level] = new Stats6(running[STR], running[AGI], running[VIT], running[INT], running[DEX], running[LUK]);
        }
        return table;
    }

    private static int[] point(int level, int statIndex) {
        return new int[] { level, statIndex };
    }

    public static Stats6 getBonus(JobType job, int level) {
        if (job == null) {
            return Stats6.ZERO;
        }

        Stats6[] table = TABLES.get(job);
        if (table == null) {
            return Stats6.ZERO;
        }

        int clamped = Math.max(0, Math.min(level, table.length - 1));
        return table[clamped];
    }
}
