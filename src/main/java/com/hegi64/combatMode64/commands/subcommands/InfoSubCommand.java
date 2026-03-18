package com.hegi64.combatMode64.commands.subcommands;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.commands.SubCommand;
import com.hegi64.combatMode64.utils.CombatModeUtil;
import com.hegi64.combatMode64.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class InfoSubCommand implements SubCommand {

    public static String name = "info";

    public String getName() { return name; }

    public String getDescription() { return "Show if you are currently in combat mode"; }

    @Override
    public boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args) {

        if (args.length >= 1 && hasInfoOtherPermission(sender)) {
            String targetName = args[0];
            Player targetPlayer = Main.getInstance().getServer().getPlayerExact(targetName);

            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + targetName + "' not found.");
                return false;
            }

            sender.sendMessage(ChatColor.GRAY + "The Player " + ChatColor.DARK_AQUA + targetPlayer.getName() + ChatColor.GRAY + " is currently " + (CombatModeUtil.isInCombatMode(targetPlayer) ? ChatColor.GREEN + "in combat mode" : ChatColor.YELLOW + "not in combat mode"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command without specifying a player.");
            return false;
        }

        player.sendMessage(ChatColor.GRAY + "You are currently " + (CombatModeUtil.isInCombatMode(player) ? ChatColor.GREEN + "in combat mode" : ChatColor.YELLOW + "not in combat mode"));
        return true;
    }

    @Override
    public boolean hasRequiredPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(Permissions.INFO_COMMAND_PERMISSION)
            || sender.hasPermission(Permissions.INFO_OTHER_COMMAND_PERMISSION)
            || sender.hasPermission(Permissions.CHANGE_OWN_COMBAT_MODE_PERMISSION)
            || sender.hasPermission(Permissions.CHANGE_OTHER_COMBAT_MODE_PERMISSION);
    }

    private boolean hasInfoOtherPermission(CommandSender sender) {
        return sender.hasPermission(Permissions.INFO_OTHER_COMMAND_PERMISSION)
            || sender.hasPermission(Permissions.CHANGE_OTHER_COMBAT_MODE_PERMISSION);
    }
}
