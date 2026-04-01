package com.hegi64.combatMode64.commands.subcommands;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.commands.SubCommand;
import com.hegi64.combatMode64.stats.StatsService;
import com.hegi64.combatMode64.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class VsSubCommand implements SubCommand {

    public static String name = "vs";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Show how often one player killed another";
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " vs <killer> <victim>");
            return false;
        }

        StatsService statsService = Main.getInstance().getStatsService();
        if (statsService == null || !statsService.isActive()) {
            sender.sendMessage(ChatColor.RED + "Stats are currently unavailable.");
            return false;
        }

        Player killer = Main.getInstance().getServer().getPlayerExact(args[0]);
        Player victim = Main.getInstance().getServer().getPlayerExact(args[1]);

        if (killer == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found.");
            return false;
        }

        if (victim == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
            return false;
        }

        int killCount = statsService.getKillCountBetween(killer.getUniqueId(), victim.getUniqueId());
        sender.sendMessage(
            ChatColor.AQUA + killer.getName()
                + ChatColor.GRAY + " has killed "
                + ChatColor.AQUA + victim.getName()
                + ChatColor.GRAY + " "
                + ChatColor.GOLD + killCount
                + ChatColor.GRAY + " times."
        );
        return true;
    }

    @Override
    public boolean hasRequiredPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(Permissions.VS_COMMAND_PERMISSION);
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 && args.length != 2) {
            return List.of();
        }

        String prefix = args[args.length - 1].toLowerCase();
        return Main.getInstance().getServer().getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(prefix))
            .toList();
    }
}

