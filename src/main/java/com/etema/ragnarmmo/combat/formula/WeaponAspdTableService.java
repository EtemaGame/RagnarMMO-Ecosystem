package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.common.api.RagnarCoreAPI;
import com.etema.ragnarmmo.common.api.jobs.JobType;
import com.etema.ragnarmmo.items.runtime.RangedWeaponStatsHelper;
import com.etema.ragnarmmo.items.runtime.WeaponStatHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;

import java.util.EnumMap;
import java.util.Map;

public final class WeaponAspdTableService {
    private static final int DEFAULT_EMPTY_ASPD = 156;
    private static final Map<JobType, Map<WeaponFamily, Integer>> TABLE = buildTable();

    private WeaponAspdTableService() {
    }

    public static int baseAspd(Player player, ItemStack weapon) {
        int configured = WeaponStatHelper.getConfiguredAspd(weapon);
        if (configured > 0) {
            return configured;
        }
        JobType job = player == null
                ? JobType.NOVICE
                : RagnarCoreAPI.get(player)
                .map(stats -> JobType.fromId(stats.getJobId()))
                .orElse(JobType.NOVICE);
        return TABLE.getOrDefault(job, TABLE.get(JobType.NOVICE))
                .getOrDefault(resolveFamily(weapon), DEFAULT_EMPTY_ASPD);
    }

    public static WeaponFamily resolveFamily(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) {
            return WeaponFamily.UNARMED;
        }
        if (RangedWeaponStatsHelper.supports(weapon) || weapon.getItem() instanceof BowItem) {
            return WeaponFamily.BOW;
        }
        if (weapon.getItem() instanceof CrossbowItem) {
            return WeaponFamily.BOW;
        }
        if (weapon.getItem() instanceof TridentItem
                || hasTagPath(weapon, "spears")) {
            return WeaponFamily.SPEAR;
        }
        if (hasTagPath(weapon, "daggers")) {
            return WeaponFamily.DAGGER;
        }
        if (hasTagPath(weapon, "katars")) {
            return WeaponFamily.KATAR;
        }
        if (hasTagPath(weapon, "maces")) {
            return WeaponFamily.MACE;
        }
        if (hasTagPath(weapon, "staves") || hasTagPath(weapon, "wands")) {
            return WeaponFamily.ROD;
        }
        if (weapon.getItem() instanceof AxeItem || hasTagPath(weapon, "axes")) {
            return WeaponFamily.AXE;
        }
        if (weapon.getItem() instanceof SwordItem) {
            return hasTagPath(weapon, "two_handed") ? WeaponFamily.TWO_HANDED_SWORD : WeaponFamily.ONE_HANDED_SWORD;
        }
        if (weapon.getItem() instanceof ProjectileWeaponItem) {
            return WeaponFamily.BOW;
        }
        if (weapon.getItem() instanceof TieredItem) {
            return WeaponFamily.ONE_HANDED_SWORD;
        }
        return WeaponFamily.UNARMED;
    }

    private static Map<JobType, Map<WeaponFamily, Integer>> buildTable() {
        EnumMap<JobType, Map<WeaponFamily, Integer>> table = new EnumMap<>(JobType.class);
        for (JobType job : JobType.values()) {
            table.put(job, row(156, 156, 156, 150, 150, 150, 156, 145, 156, 156));
        }
        table.put(JobType.ARCHER, row(156, 156, 154, 148, 150, 148, 154, 170, 156, 156));
        table.put(JobType.THIEF, row(156, 160, 156, 150, 150, 150, 154, 160, 156, 156));
        table.put(JobType.SWORDSMAN, row(156, 156, 158, 154, 148, 152, 150, 145, 156, 156));
        table.put(JobType.MERCHANT, row(156, 154, 154, 150, 152, 154, 150, 145, 156, 156));
        table.put(JobType.ACOLYTE, row(156, 154, 154, 148, 148, 156, 154, 145, 156, 156));
        table.put(JobType.MAGE, row(156, 152, 150, 145, 145, 148, 158, 145, 156, 156));
        return table;
    }

    private static Map<WeaponFamily, Integer> row(int unarmed, int dagger, int sword1h, int sword2h, int spear,
            int axe, int mace, int bow, int rod, int katar) {
        EnumMap<WeaponFamily, Integer> row = new EnumMap<>(WeaponFamily.class);
        row.put(WeaponFamily.UNARMED, unarmed);
        row.put(WeaponFamily.DAGGER, dagger);
        row.put(WeaponFamily.ONE_HANDED_SWORD, sword1h);
        row.put(WeaponFamily.TWO_HANDED_SWORD, sword2h);
        row.put(WeaponFamily.SPEAR, spear);
        row.put(WeaponFamily.AXE, axe);
        row.put(WeaponFamily.MACE, mace);
        row.put(WeaponFamily.BOW, bow);
        row.put(WeaponFamily.ROD, rod);
        row.put(WeaponFamily.KATAR, katar);
        return Map.copyOf(row);
    }

    private static boolean hasTagPath(ItemStack weapon, String pathPart) {
        return weapon != null && !weapon.isEmpty()
                && weapon.getTags().map(tag -> tag.location()).map(ResourceLocation::getPath)
                .anyMatch(path -> path.contains(pathPart));
    }

    public enum WeaponFamily {
        UNARMED,
        DAGGER,
        ONE_HANDED_SWORD,
        TWO_HANDED_SWORD,
        SPEAR,
        AXE,
        MACE,
        BOW,
        ROD,
        KATAR
    }
}
