package com.hegi64.combatMode64.utils;

import com.hegi64.combatMode64.Main;
import org.bukkit.ChatColor;

import java.util.List;

public final class ConfigUtil {

    private static final String INDICATOR_ROOT = "combat_status_indicator";
    private static final String STATS_ROOT = "stats";
    private static final String STATS_SCOREBOARD_ROOT = "stats_scoreboard";

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

    public static boolean isStatsScoreboardEnabled() {
        return Main.getInstance().getConfig().getBoolean(STATS_SCOREBOARD_ROOT + ".enabled", true);
    }

    public static boolean isStatsScoreboardDefaultEnabled() {
        return Main.getInstance().getConfig().getBoolean(STATS_SCOREBOARD_ROOT + ".default_enabled", false);
    }

    public static String getStatsScoreboardDefaultMode() {
        String configured = Main.getInstance().getConfig().getString(STATS_SCOREBOARD_ROOT + ".default_mode", null);
        if (configured != null) {
            return configured;
        }
        return isStatsScoreboardDefaultEnabled() ? "compact" : "off";
    }

    public static String getStatsScoreboardTitle() {
        String text = Main.getInstance().getConfig().getString(STATS_SCOREBOARD_ROOT + ".title", "&cCombat Stats");
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static boolean shouldStatsScoreboardTakeOverSidebar() {
        return Main.getInstance().getConfig().getBoolean(STATS_SCOREBOARD_ROOT + ".take_over_sidebar", false);
    }

    public static int getStatsScoreboardUpdateIntervalSeconds() {
        return Math.max(0, Main.getInstance().getConfig().getInt(STATS_SCOREBOARD_ROOT + ".update_interval_seconds", 45));
    }

    public static boolean shouldShowStatsScoreboardLastVictim() {
        return Main.getInstance().getConfig().getBoolean(STATS_SCOREBOARD_ROOT + ".show_last_victim", true);
    }

    public static boolean shouldShowStatsScoreboardWeapon() {
        return Main.getInstance().getConfig().getBoolean(STATS_SCOREBOARD_ROOT + ".show_weapon", true);
    }

    public static boolean shouldShowStatsScoreboardDistance() {
        return Main.getInstance().getConfig().getBoolean(STATS_SCOREBOARD_ROOT + ".show_distance", true);
    }

    public static boolean shouldShowStatsScoreboardWorld() {
        return Main.getInstance().getConfig().getBoolean(STATS_SCOREBOARD_ROOT + ".show_world", false);
    }

    public static boolean shouldShowStatsScoreboardKillTime() {
        return Main.getInstance().getConfig().getBoolean(STATS_SCOREBOARD_ROOT + ".show_kill_time", false);
    }
}
