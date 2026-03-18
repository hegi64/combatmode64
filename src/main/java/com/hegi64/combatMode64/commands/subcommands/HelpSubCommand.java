package com.hegi64.combatMode64.commands.subcommands;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.commands.SubCommand;
import com.hegi64.combatMode64.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class HelpSubCommand implements SubCommand {

    public static String name = "help";

    public String getName() { return name; }

    public String getDescription() { return "Show help to this command"; }

    @Override
    public boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args) {
        sendSubCommandHelp(sender);
        return true;
    }

    @Override
    public boolean hasRequiredPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(Permissions.COMMAND_PERMISSION);
    }

    /**
     * Sends a help message listing all available subcommands and their descriptions.
     */
    private void sendSubCommandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Available subcommands:");
        for (SubCommand sub : subCommands.values()) {
            if (sub.hasRequiredPermission(sender)) {
                sender.sendMessage(ChatColor.YELLOW + "- " + sub.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + sub.getDescription());
            }
        }

        sender.sendMessage(ChatColor.GOLD + "Usage: " + ChatColor.GREEN + "/" + Main.getInstance().getCommand("combatmode").getName() + " <subcommand>");
    }

    private final Map<String, SubCommand> subCommands;

    public HelpSubCommand(Map<String, SubCommand> subCommands) {
        this.subCommands = subCommands;
    }
}
