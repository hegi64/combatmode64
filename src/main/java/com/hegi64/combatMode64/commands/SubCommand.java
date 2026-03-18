package com.hegi64.combatMode64.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

/**
 * Represents a subcommand for the plugin's command system.
 * Implementations must provide a non-empty command name or code via getName().
 */
public interface SubCommand {

    /**
     * Executes the subcommand logic.
     */
    boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args);

    /**
     * Returns the name or code of the subcommand. Must not be an empty string.
     *
     * @return non-empty command name or code
     */
    String getName();

    /**
     * Returns a short description of the subcommand for help messages.
     *
     * @return description of the subcommand
     */
    String getDescription();

    /**
     * Checks if the required permissions are set for the sender.
     * @param sender the command sender
     * @return true if the sender has the required permissions, false otherwise
     */
    boolean hasRequiredPermission(@NonNull CommandSender sender);
}
