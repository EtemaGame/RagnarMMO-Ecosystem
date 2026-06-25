package com.etema.ragnarmmo.combat.formula;

import com.etema.ragnarmmo.player.stats.compute.RoPreRenewalFormulaService;

public final class AspdFormulaService {
    private AspdFormulaService() {
    }

    public static int aspdRo(int baseWeaponAspd, boolean hasShield, int agi, int dex, double bonus) {
        return RoPreRenewalFormulaService.aspdRo(baseWeaponAspd, hasShield, agi, dex, bonus);
    }

    public static int aspdRo(int baseWeaponAspd, boolean hasShield, int agi, int dex, double speedModifier,
            double flatBonus) {
        return RoPreRenewalFormulaService.aspdRo(baseWeaponAspd, hasShield, agi, dex, speedModifier, flatBonus);
    }

    public static double attacksPerSecond(int aspdRo) {
        return RoPreRenewalFormulaService.aspdToAttacksPerSecond(aspdRo);
    }
}
