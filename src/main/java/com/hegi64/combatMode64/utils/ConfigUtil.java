package com.hegi64.combatMode64.utils;

import com.hegi64.combatMode64.Main;
import org.bukkit.ChatColor;

import java.util.List;

public final class ConfigUtil {

    private static final String INDICATOR_ROOT = "combat_status_indicator";
    private static final String STATS_ROOT = "stats";

    public static List<String> getAllowedWorlds() {
        return Main.getInstance().getConfig().getStringList("allowed_worlds");
    }

    public static boolean isPvpDisabledByDefault() {
        return Main.getInstance().getConfig().getBoolean("disable_pvp_by_default", false);
    }

    public static boolean isGamemodeSwitchingAllowed() {
        return Main.getInstance().getConfig().getBoolean("allow_gamemode_switching", true);
    }

    // -------------------------------------------------------------------------
    // Combat status indicator (Team suffix)
    // -------------------------------------------------------------------------

    public static boolean isCombatStatusIndicatorEnabled() {
        return Main.getInstance().getConfig().getBoolean(INDICATOR_ROOT + ".enabled", true);
    }

    public static boolean shouldShowInCombatIndicator() {
        return Main.getInstance().getConfig().getBoolean(INDICATOR_ROOT + ".in_combat.show", true);
    }

    public static boolean shouldShowNotInCombatIndicator() {
        return Main.getInstance().getConfig().getBoolean(INDICATOR_ROOT + ".not_in_combat.show", false);
    }

    public static String getInCombatText() {
        String text = Main.getInstance().getConfig().getString(INDICATOR_ROOT + ".in_combat.text", " &c⚔ Combat Mode");
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String getNotInCombatText() {
        String text = Main.getInstance().getConfig().getString(INDICATOR_ROOT + ".not_in_combat.text", " &a☮ Peaceful");
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static boolean isStatsEnabled() {
        return Main.getInstance().getConfig().getBoolean(STATS_ROOT + ".enabled", true);
    }

    public static String getStatsSqliteFileName() {
        return Main.getInstance().getConfig().getString(STATS_ROOT + ".sqlite.file", "stats.db");
    }
}
