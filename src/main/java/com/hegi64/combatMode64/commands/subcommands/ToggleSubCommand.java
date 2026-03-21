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

import java.util.List;

public class ToggleSubCommand implements SubCommand {

    public static String name = "toggle";

    public String getName() { return name; }

    public String getDescription() { return "Toggle your combat mode on or off."; }

    @Override
    public boolean execute(@NonNull CommandSender sender, Command command, String label, String[] args) {

        if (args.length >= 1 && hasChangeOtherPermission(sender)) {
            String targetName = args[0];
            Player targetPlayer = Main.getInstance().getServer().getPlayerExact(targetName);

            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + ChatColor.AQUA + targetName + ChatColor.RED + "' not found.");
                return false;
            }

            if (CombatModeUtil.isInCombatMode(targetPlayer)) {
                CombatModeUtil.disableCombatMode(targetPlayer, true);
                sender.sendMessage(ChatColor.GREEN + "Combat mode disabled for " + ChatColor.AQUA + targetPlayer.getName());
            } else {
                CombatModeUtil.enableCombatMode(targetPlayer, true);
                sender.sendMessage(ChatColor.GREEN + "Combat mode enabled for " + ChatColor.AQUA + targetPlayer.getName());
            }

            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command without specifying a player.");
            return false;
        }

        if (CombatModeUtil.isInCombatMode(player)) {
            CombatModeUtil.disableCombatMode(player, true);
        } else {
            CombatModeUtil.enableCombatMode(player, true);
        }

        return true;
    }

    @Override
    public boolean hasRequiredPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(Permissions.CHANGE_OWN_COMBAT_MODE_PERMISSION)
                || sender.hasPermission(Permissions.CHANGE_OTHER_COMBAT_MODE_PERMISSION);
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, Command command, String label, String[] args) {
        if (!hasChangeOtherPermission(sender) || args.length != 1) {
            return List.of();
        }

        String prefix = args[0].toLowerCase();
        return Main.getInstance().getServer().getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(prefix))
            .toList();
    }

    private boolean hasChangeOtherPermission(CommandSender sender) {
        return sender.hasPermission(Permissions.CHANGE_OTHER_COMBAT_MODE_PERMISSION);
    }
}
