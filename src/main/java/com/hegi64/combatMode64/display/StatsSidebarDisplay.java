package com.hegi64.combatMode64.display;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.stats.KillEventDetails;
import com.hegi64.combatMode64.stats.PlayerStats;
import com.hegi64.combatMode64.stats.StatsService;
import com.hegi64.combatMode64.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StatsSidebarDisplay {

    private static final String OBJECTIVE_NAME = "cm64_stats";
    private static final String TEAM_PREFIX = "cm64sb";
    private static final List<String> ENTRIES = List.of(
        ChatColor.RED.toString(),
        ChatColor.BLUE.toString(),
        ChatColor.GRAY.toString(),
        ChatColor.GREEN.toString(),
        ChatColor.YELLOW.toString(),
        ChatColor.DARK_AQUA.toString(),
        ChatColor.DARK_PURPLE.toString(),
        ChatColor.GOLD.toString(),
        ChatColor.AQUA.toString(),
        ChatColor.WHITE.toString()
    );

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String LINE_SEPARATOR = ChatColor.DARK_GRAY + "────────";

    private final Main plugin;
    private final StatsService statsService;
    private final NamespacedKey modeKey;
    private final Map<UUID, Scoreboard> previousScoreboards = new HashMap<>();
    private final Set<UUID> activeSidebarPlayers = ConcurrentHashMap.newKeySet();

    public StatsSidebarDisplay(Main plugin, StatsService statsService) {
        this.plugin = plugin;
        this.statsService = statsService;
        this.modeKey = new NamespacedKey(plugin, "stats_sidebar_mode");
    }

    public void handleJoin(Player player) {
        if (!isFeatureAvailable()) {
            return;
        }

        StatsSidebarMode mode = getModeForPlayer(player);
        if (mode != StatsSidebarMode.OFF) {
            refreshPlayer(player);
        }
    }

    public void handleQuit(Player player) {
        activeSidebarPlayers.remove(player.getUniqueId());
        previousScoreboards.remove(player.getUniqueId());
    }

    public StatsSidebarMode getModeForPlayer(Player player) {
        String raw = player.getPersistentDataContainer().get(modeKey, PersistentDataType.STRING);
        if (raw == null) {
            return StatsSidebarMode.fromString(ConfigUtil.getStatsScoreboardDefaultMode());
        }
        return StatsSidebarMode.fromString(raw);
    }

    public boolean setModeForPlayer(Player player, StatsSidebarMode mode) {
        player.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, mode.name().toLowerCase(Locale.ROOT));
        if (mode == StatsSidebarMode.OFF) {
            removeSidebar(player);
            return true;
        }
        return refreshPlayer(player);
    }

    public boolean refreshPlayer(Player player) {
        if (!isFeatureAvailable()) {
            return false;
        }

        StatsSidebarMode mode = getModeForPlayer(player);
        if (mode == StatsSidebarMode.OFF) {
            removeSidebar(player);
            return false;
        }

        Scoreboard scoreboard = getOrCreatePlayerScoreboard(player);
        if (scoreboard == null) {
            return false;
        }

        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, ConfigUtil.getStatsScoreboardTitle());
        }
        objective.setDisplayName(ConfigUtil.getStatsScoreboardTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Optional<PlayerStats> optionalStats = statsService.getPlayerStats(player.getUniqueId());
        int kills = optionalStats.map(PlayerStats::kills).orElse(0);
        int deaths = optionalStats.map(PlayerStats::deaths).orElse(0);
        double kd = deaths == 0 ? kills : (double) kills / deaths;

        List<String> lines = new ArrayList<>();
        lines.add(LINE_SEPARATOR);
        lines.add(ChatColor.GREEN + "⚔ Kills: " + ChatColor.WHITE + kills);
        lines.add(ChatColor.BLACK + " ");
        lines.add(ChatColor.RED + "☠ Deaths: " + ChatColor.WHITE + deaths);
        lines.add(ChatColor.BLACK + "  ");
        lines.add(ChatColor.AQUA + "◈ K/D: " + ChatColor.WHITE + String.format(Locale.US, "%.2f", kd));

        if (mode == StatsSidebarMode.EXTENDED) {
            lines.add(LINE_SEPARATOR);
            appendExtendedLines(lines, player.getUniqueId());
        }

        applyLines(scoreboard, objective, lines);

        if (player.getScoreboard() != scoreboard) {
            player.setScoreboard(scoreboard);
        }

        return true;
    }

    public void refreshAfterKill(Player killer, Player victim) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (killer.isOnline()) {
                refreshPlayer(killer);
            }
            if (victim.isOnline()) {
                refreshPlayer(victim);
            }
        }, 20L);
    }

    public void refreshAllEnabledPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getModeForPlayer(player) != StatsSidebarMode.OFF) {
                refreshPlayer(player);
            }
        }
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeSidebar(player);
        }
    }

    private void appendExtendedLines(List<String> lines, UUID playerUuid) {
        Optional<KillEventDetails> optionalLastKill = statsService.getLastKillByKiller(playerUuid);

        if (ConfigUtil.shouldShowStatsScoreboardLastVictim()) {
            String victim = optionalLastKill.map(KillEventDetails::victimName).orElse("-");
            lines.add(ChatColor.YELLOW + "☞ Victim: " + ChatColor.WHITE + victim);
        }

        if (ConfigUtil.shouldShowStatsScoreboardWeapon()) {
            String weapon = optionalLastKill.map(KillEventDetails::weaponType).orElse("-");
            lines.add(ChatColor.GOLD + "⚒ Weapon: " + ChatColor.WHITE + weapon);
        }

        if (ConfigUtil.shouldShowStatsScoreboardDistance()) {
            String distance = optionalLastKill
                .map(details -> String.format(Locale.US, "%.1fm", details.distance()))
                .orElse("-");
            lines.add(ChatColor.DARK_AQUA + "➤ Dist: " + ChatColor.WHITE + distance);
        }

        if (ConfigUtil.shouldShowStatsScoreboardWorld()) {
            String world = optionalLastKill.map(KillEventDetails::worldName).orElse("-");
            lines.add(ChatColor.LIGHT_PURPLE + "✦ World: " + ChatColor.WHITE + world);
        }

        if (ConfigUtil.shouldShowStatsScoreboardKillTime()) {
            String time = optionalLastKill
                .map(details -> TIME_FORMAT.format(Instant.ofEpochSecond(details.killedAt()).atZone(ZoneId.systemDefault())))
                .orElse("-");
            lines.add(ChatColor.GRAY + "⌚ At: " + ChatColor.WHITE + time);
        }
    }

    private boolean isFeatureAvailable() {
        return statsService != null && statsService.isActive() && ConfigUtil.isStatsScoreboardEnabled();
    }

    private Scoreboard getOrCreatePlayerScoreboard(Player player) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) {
            return null;
        }

        UUID playerId = player.getUniqueId();
        Scoreboard current = player.getScoreboard();
        Scoreboard main = scoreboardManager.getMainScoreboard();

        if (!activeSidebarPlayers.contains(playerId)) {
            if (!ConfigUtil.shouldStatsScoreboardTakeOverSidebar() && current != main) {
                return null;
            }

            previousScoreboards.put(playerId, current);
            if (current == main) {
                current = scoreboardManager.getNewScoreboard();
            }
            activeSidebarPlayers.add(playerId);
        }

        return current;
    }

    private void removeSidebar(Player player) {
        UUID playerId = player.getUniqueId();
        if (!activeSidebarPlayers.contains(playerId)) {
            return;
        }

        Scoreboard current = player.getScoreboard();
        Objective objective = current.getObjective(OBJECTIVE_NAME);
        if (objective != null) {
            objective.unregister();
        }

        for (int i = 0; i < ENTRIES.size(); i++) {
            Team team = current.getTeam(teamName(i));
            if (team != null) {
                team.unregister();
            }
        }

        Scoreboard restore = previousScoreboards.remove(playerId);
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null && restore == null) {
            restore = manager.getMainScoreboard();
        }

        if (restore != null && player.isOnline()) {
            player.setScoreboard(restore);
        }

        activeSidebarPlayers.remove(playerId);
    }

    private void applyLines(Scoreboard scoreboard, Objective objective, List<String> lines) {
        int maxLines = Math.min(lines.size(), ENTRIES.size());

        for (int i = 0; i < ENTRIES.size(); i++) {
            String entry = ENTRIES.get(i);
            if (i < maxLines) {
                Team team = scoreboard.getTeam(teamName(i));
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName(i));
                }
                if (!team.hasEntry(entry)) {
                    team.addEntry(entry);
                }
                team.setPrefix(lines.get(i));
                objective.getScore(entry).setScore(maxLines - i);
            } else {
                scoreboard.resetScores(entry);
                Team team = scoreboard.getTeam(teamName(i));
                if (team != null) {
                    team.unregister();
                }
            }
        }
    }

    private String teamName(int index) {
        return TEAM_PREFIX + index;
    }
}
