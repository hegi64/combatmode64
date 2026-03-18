package com.hegi64.combatMode64.commands.subcommands;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.commands.SubCommand;
import com.hegi64.combatMode64.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public class ReloadSubCommand implements SubCommand {

    public static String name = "reload";

    public String getName() { return name; }

    public String getDescription() { return "Reloads the plugin's configuration from config file."; }

    @Override
    public boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args) {
        Main plugin = Main.getInstance();
        plugin.reloadConfig();

        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully.");
        return true;
    }

    @Override
    public boolean hasRequiredPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(Permissions.RELOAD_COMMAND_PERMISSION);
    }
}
