package com.hegi64.combatMode64.commands.subcommands;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.commands.SubCommand;
import com.hegi64.combatMode64.stats.PlayerStats;
import com.hegi64.combatMode64.stats.StatsService;
import com.hegi64.combatMode64.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class StatsSubCommand implements SubCommand {

    public static String name = "stats";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Show kill/death stats for yourself or another player";
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args) {
        StatsService statsService = Main.getInstance().getStatsService();
        if (statsService == null || !statsService.isActive()) {
            sender.sendMessage(ChatColor.RED + "Stats are currently unavailable.");
            return false;
        }

        Player target;
        if (args.length >= 1) {
            if (!sender.hasPermission(Permissions.STATS_OTHER_COMMAND_PERMISSION)) {
                sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to view other players' stats.");
                return false;
            }

            target = Main.getInstance().getServer().getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found.");
                return false;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command without specifying a player.");
                return false;
            }
            target = player;
        }

        Optional<PlayerStats> optionalStats = statsService.getPlayerStats(target.getUniqueId());
        if (optionalStats.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No combat stats found for " + ChatColor.AQUA + target.getName() + ChatColor.YELLOW + ".");
            return true;
        }

        PlayerStats stats = optionalStats.get();
        double kd = stats.deaths() == 0 ? stats.kills() : (double) stats.kills() / stats.deaths();

        sender.sendMessage(ChatColor.GOLD + "Stats for " + ChatColor.AQUA + target.getName() + ChatColor.GOLD + ":");
        sender.sendMessage(ChatColor.GRAY + "Kills: " + ChatColor.GREEN + stats.kills());
        sender.sendMessage(ChatColor.GRAY + "Deaths: " + ChatColor.RED + stats.deaths());
        sender.sendMessage(ChatColor.GRAY + "K/D: " + ChatColor.WHITE + String.format(Locale.US, "%.2f", kd));
        return true;
    }

    @Override
    public boolean hasRequiredPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(Permissions.STATS_COMMAND_PERMISSION)
            || sender.hasPermission(Permissions.STATS_OTHER_COMMAND_PERMISSION);
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(Permissions.STATS_OTHER_COMMAND_PERMISSION) || args.length != 1) {
            return List.of();
        }

        String prefix = args[0].toLowerCase();
        return Main.getInstance().getServer().getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(prefix))
            .toList();
    }
}
