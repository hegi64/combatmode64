package com.hegi64.combatMode64.commands.subcommands;

import com.hegi64.combatMode64.commands.SubCommand;
import com.hegi64.combatMode64.utils.CombatModeUtil;
import com.hegi64.combatMode64.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ToggleSubCommand implements SubCommand {

    public static String name = "toggle";

    public String getName() { return name; }

    public String getDescription() { return "Toggle your combat mode on or off."; }

    @Override
    public boolean hasRequiredPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(Permissions.TOGGLE_COMMAND_PERMISSION)
            || sender.hasPermission(Permissions.TOGGLE_OTHER_COMMAND_PERMISSION)
            || sender.hasPermission(Permissions.CHANGE_OWN_COMBAT_MODE_PERMISSION)
            || sender.hasPermission(Permissions.CHANGE_OTHER_COMBAT_MODE_PERMISSION);
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return false;
        }

        if (CombatModeUtil.isInCombatMode(player)) {
            CombatModeUtil.disableCombatMode(player, true);
        } else {
            CombatModeUtil.enableCombatMode(player, true);
        }

        return true;
    }
}
