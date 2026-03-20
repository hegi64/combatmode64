package com.hegi64.combatMode64.utils;

import com.hegi64.combatMode64.Main;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class CombatModeUtil {

    private static final NamespacedKey COMBAT_MODE_KEY = new NamespacedKey(Main.getInstance(), "combat_mode");

    /**
     * Checks if the player is currently in combat mode by retrieving the value from their PersistentDataContainer.
     * If something goes wrong (like the key not existing), it catches the NullPointerException and returns false, indicating that the player is not in combat mode.
     *
     * @param player The player whose combat mode status we want to check.
     * @return true if the player is in combat mode, false otherwise.
     */
    public static boolean isInCombatMode(Player player) {
        try {
            return Boolean.TRUE.equals(player.getPersistentDataContainer().get(COMBAT_MODE_KEY, PersistentDataType.BOOLEAN));
        } catch (NullPointerException e) {
            // If the key doesn't exist, we consider the player not in combat mode
            return false;
        }
    }

    /**
     * Enables combat mode for the player by setting a boolean value in their PersistentDataContainer.
     * It also sends a message to the player confirming that combat mode has been enabled.
     *
     * @param player The player for whom we want to enable combat mode.
     */
    public static void enableCombatMode(Player player, boolean notifyPlayer) {
        String worldName = player.getWorld().getName();
        if (!isCombatModeEnabledForWorld(worldName)) {
            if (notifyPlayer) {
                player.sendMessage(ChatColor.RED + "Combat mode is not enabled in this world.");
            }
            return;
        }
        player.getPersistentDataContainer().set(COMBAT_MODE_KEY, PersistentDataType.BOOLEAN, true);

        // Combat mode always runs in survival.
        if (player.getGameMode() != GameMode.SURVIVAL) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        if (notifyPlayer) {
            player.sendMessage(ChatColor.GREEN + "Combat mode enabled.");
        }
    }

    /**
     * Disables combat mode for the player by setting a boolean value in their PersistentDataContainer.
     * It also sends a message to the player confirming that combat mode has been disabled.
     *
     * @param player The player for whom we want to disable combat mode.
     */
    public static void disableCombatMode(Player player, boolean notifyPlayer) {
        player.getPersistentDataContainer().set(COMBAT_MODE_KEY, PersistentDataType.BOOLEAN, false);

        if (notifyPlayer) {
            player.sendMessage(ChatColor.YELLOW + "Combat mode disabled.");
        }
    }

    /**
     * Checks if combat mode is enabled for a specific world by looking up the world name in the plugin's configuration list of allowed worlds.
     * @param worldName The name of the world to check for combat mode enablement.
     * @return true if combat mode is enabled for the world, false otherwise.
     */
    public static boolean isCombatModeEnabledForWorld(String worldName) {
        return ConfigUtil.getAllowedWorlds().contains(worldName);
    }

    public static boolean isPvpDisabledByDefault() {
        return ConfigUtil.isPvpDisabledByDefault();
    }

}
