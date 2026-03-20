package com.hegi64.combatMode64.display;

import com.hegi64.combatMode64.utils.CombatModeUtil;
import com.hegi64.combatMode64.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

/**
 * Manages the combat status indicator next to player nametags
 * using the Bukkit Scoreboard Team suffix/prefix API.
 * <p>
 * Each player is placed in a team whose suffix (or prefix) displays
 * their combat mode status. This does NOT hide the player's nametag
 * and requires no entities or tick tasks.
 */
public final class CombatStatusDisplay {

    private static final String TEAM_PREFIX = "cm64_";

    /**
     * Updates the combat status indicator for the given player.
     */
    public static void updateDisplay(Player player) {
        if (!ConfigUtil.isCombatStatusIndicatorEnabled()) {
            removeDisplay(player);
            return;
        }

        boolean inCombat = CombatModeUtil.isInCombatMode(player);

        if (inCombat && !ConfigUtil.shouldShowInCombatIndicator()) {
            removeDisplay(player);
            return;
        }

        if (!inCombat && !ConfigUtil.shouldShowNotInCombatIndicator()) {
            removeDisplay(player);
            return;
        }

        String text = inCombat
                ? ConfigUtil.getInCombatText()
                : ConfigUtil.getNotInCombatText();

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) {
            Bukkit.getLogger().warning("Could not get ScoreboardManager to update combat status display for player " + player.getName());
            return;
        }

        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Team team = getOrCreateTeam(scoreboard, player);

        team.setSuffix(text);

        // Always re-add entry to force a client-side nametag refresh
        String entry = player.getName();
        if (team.hasEntry(entry)) {
            team.removeEntry(entry);
        }
        team.addEntry(entry);
    }

    /**
     * Removes the indicator for the given player.
     */
    public static void removeDisplay(Player player) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) {
            Bukkit.getLogger().warning("Could not get ScoreboardManager to remove combat status display for player " + player.getName());
            return;
        }

        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Team team = scoreboard.getTeam(getTeamName(player));
        if (team != null) {
            team.unregister();
        }
    }

    /**
     * Removes all managed teams.
     */
    public static void removeAllDisplays() {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) {
            Bukkit.getLogger().warning("Could not get ScoreboardManager to remove all Displays.");
            return;
        }

        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        // Copy to avoid ConcurrentModificationException when unregistering during iteration
        for (Team team : new java.util.ArrayList<>(scoreboard.getTeams())) {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                team.unregister();
            }
        }
    }

    /**
     * Refreshes all displays (e.g. after config reload).
     */
    public static void refreshAllDisplays() {
        removeAllDisplays();
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateDisplay(player);
        }
    }

    /**
     * Called when a player joins.
     */
    public static void onPlayerJoin(Player player) {
        updateDisplay(player);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static Team getOrCreateTeam(Scoreboard scoreboard, Player player) {
        String teamName = getTeamName(player);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        return team;
    }

    /**
     * Returns a unique team name for the player. Team names are limited to
     * 16 characters, so we use a prefix + a truncated/hashed player name.
     */
    private static String getTeamName(Player player) {
        String name = TEAM_PREFIX + player.getName();
        // Team names max 16 chars in some versions; use hash if too long
        if (name.length() > 16) {
            name = TEAM_PREFIX + Integer.toHexString(player.getUniqueId().hashCode());
        }
        return name;
    }
}


