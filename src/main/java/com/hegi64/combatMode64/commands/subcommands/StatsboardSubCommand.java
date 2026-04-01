package com.hegi64.combatMode64.commands.subcommands;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.commands.SubCommand;
import com.hegi64.combatMode64.display.StatsSidebarDisplay;
import com.hegi64.combatMode64.display.StatsSidebarMode;
import com.hegi64.combatMode64.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class StatsboardSubCommand implements SubCommand {

    public static String name = "statsboard";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Manage stats sidebar mode (off/compact/extended) and refresh";
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args) {
        StatsSidebarDisplay display = Main.getInstance().getStatsSidebarDisplay();
        if (display == null) {
            sender.sendMessage(ChatColor.RED + "Stats scoreboard is currently unavailable.");
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " statsboard <off|compact|extended|refresh> [player]");
            return false;
        }

        String action = args[0].toLowerCase();
        if ("refresh".equals(action)) {
            return handleRefresh(sender, display, args);
        }

        StatsSidebarMode mode = StatsSidebarMode.fromString(action);
        if (mode == StatsSidebarMode.OFF && !"off".equals(action)) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " statsboard <off|compact|extended|refresh> [player]");
            return false;
        }

        Player target = resolveTargetPlayer(sender, args, 1);
        if (target == null) {
            return false;
        }

        boolean success = display.setModeForPlayer(target, mode);
        if (!success && mode != StatsSidebarMode.OFF) {
            sender.sendMessage(ChatColor.YELLOW + "Could not enable stats scoreboard for " + ChatColor.AQUA + target.getName()
                + ChatColor.YELLOW + " (sidebar is controlled by another plugin).");
            return false;
        }

        String modeName = mode.name().toLowerCase();
        if (sender == target) {
            sender.sendMessage(ChatColor.GREEN + "Stats scoreboard mode set to " + modeName + ".");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Set stats scoreboard mode for " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + " to " + modeName + ".");
            target.sendMessage(ChatColor.GREEN + "Your stats scoreboard mode was set to " + modeName + ".");
        }
        return true;
    }

    @Override
    public boolean hasRequiredPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(Permissions.STATSBOARD_COMMAND_PERMISSION)
            || sender.hasPermission(Permissions.STATSBOARD_OTHER_COMMAND_PERMISSION)
            || sender.hasPermission(Permissions.STATSBOARD_REFRESH_COMMAND_PERMISSION);
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return List.of("off", "compact", "extended", "refresh").stream()
                .filter(value -> value.startsWith(prefix))
                .toList();
        }

        if (args.length == 2) {
            String action = args[0].toLowerCase();
            boolean needsTarget = "refresh".equals(action)
                || sender.hasPermission(Permissions.STATSBOARD_OTHER_COMMAND_PERMISSION);
            if (!needsTarget) {
                return List.of();
            }

            String prefix = args[1].toLowerCase();
            return Main.getInstance().getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .toList();
        }

        return List.of();
    }

    private boolean handleRefresh(CommandSender sender, StatsSidebarDisplay display, String[] args) {
        if (!sender.hasPermission(Permissions.STATSBOARD_REFRESH_COMMAND_PERMISSION)) {
            sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to refresh scoreboards.");
            return false;
        }

        if (args.length >= 2) {
            Player target = Main.getInstance().getServer().getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
                return false;
            }
            display.refreshPlayer(target);
            sender.sendMessage(ChatColor.GREEN + "Refreshed stats scoreboard for " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + ".");
            return true;
        }

        display.refreshAllEnabledPlayers();
        sender.sendMessage(ChatColor.GREEN + "Refreshed stats scoreboards for all enabled players.");
        return true;
    }

    private Player resolveTargetPlayer(CommandSender sender, String[] args, int targetArgIndex) {
        if (args.length > targetArgIndex) {
            if (!sender.hasPermission(Permissions.STATSBOARD_OTHER_COMMAND_PERMISSION)) {
                sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to change other players' scoreboards.");
                return null;
            }

            Player target = Main.getInstance().getServer().getPlayerExact(args[targetArgIndex]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[targetArgIndex] + "' not found.");
            }
            return target;
        }

        if (sender instanceof Player player) {
            return player;
        }

        sender.sendMessage(ChatColor.RED + "Console must specify a player: /combatmode statsboard <mode> <player>");
        return null;
    }
}
