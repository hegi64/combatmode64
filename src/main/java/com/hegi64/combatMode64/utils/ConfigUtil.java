package com.hegi64.combatMode64.utils;

import com.hegi64.combatMode64.Main;
import org.bukkit.ChatColor;

import java.util.List;

public final class ConfigUtil {

    private static final String INDICATOR_ROOT = "combat_status_indicator";

    public static List<String> getAllowedWorlds() {
        return Main.getInstance().getConfig().getStringList("allowed_worlds");
    }

    public static boolean isPvpDisabledByDefault() {
        return Main.getInstance().getConfig().getBoolean("disable_pvp_by_default", false);
    }

    public static boolean isGamemodeSwitchingAllowed() {
        return Main.getInstance().getConfig().getBoolean("allow_gamemode_switching", true);
    }

    public static boolean isCombatStatusIndicatorEnabled() {
        return Main.getInstance().getConfig().getBoolean(INDICATOR_ROOT + ".enabled", true);
    }

    public static boolean shouldShowInCombatIndicator() {
        return Main.getInstance().getConfig().getBoolean(INDICATOR_ROOT + ".in_combat.show", true);
    }

    public static boolean shouldShowNotInCombatIndicator() {
        return Main.getInstance().getConfig().getBoolean(INDICATOR_ROOT + ".not_in_combat.show", false);
    }

    public static int getInCombatIndicatorScore() {
        return Main.getInstance().getConfig().getInt(INDICATOR_ROOT + ".in_combat.score", 1);
    }

    public static int getNotInCombatIndicatorScore() {
        return Main.getInstance().getConfig().getInt(INDICATOR_ROOT + ".not_in_combat.score", 0);
    }

    public static String getCombatIndicatorBelowNameTitle() {
        String text = Main.getInstance().getConfig().getString(INDICATOR_ROOT + ".below_name_title", "&cCombat");
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
