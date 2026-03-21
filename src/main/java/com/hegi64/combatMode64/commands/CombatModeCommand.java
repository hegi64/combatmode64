package com.hegi64.combatMode64.commands;

import com.hegi64.combatMode64.commands.subcommands.*;
import com.hegi64.combatMode64.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombatModeCommand implements TabExecutor {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CombatModeCommand() {
        // Register subcommands here
        subCommands.put(ReloadSubCommand.name, new ReloadSubCommand());
        subCommands.put(ToggleSubCommand.name, new ToggleSubCommand());
        subCommands.put(InfoSubCommand.name, new InfoSubCommand());

        subCommands.put(HelpSubCommand.name, new HelpSubCommand(subCommands));
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_PERMISSION)) {
            sender.sendMessage(ChatColor.DARK_RED + "You don't have the Permission to do this.");
            return false;
        }

        if (args.length > 0) {
            String subName = args[0].toLowerCase();
            SubCommand subCommand = subCommands.get(subName);
            if (subCommand != null) {
                if (!subCommand.hasRequiredPermission(sender)) {
                    sender.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this subcommand.");
                    return false;
                }
                // Pass the rest of the args to the subcommand
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                return subCommand.execute(sender, command, label, subArgs);
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
                subCommands.get(HelpSubCommand.name).execute(sender, command, label, new String[]{});
                return false;
            }
        }

        subCommands.get(ToggleSubCommand.name).execute(sender, command, label, new String[]{});

        return false;
    }

    @Override
    public @NonNull List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            for (SubCommand subCommand : subCommands.values()) {
                String name = subCommand.getName();
                if (subCommand.hasRequiredPermission(sender) && name.startsWith(prefix)) {
                    completions.add(name);
                }
            }

            return completions;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null || !subCommand.hasRequiredPermission(sender)) {
            return List.of();
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        return subCommand.tabComplete(sender, command, alias, subArgs);
    }
}
