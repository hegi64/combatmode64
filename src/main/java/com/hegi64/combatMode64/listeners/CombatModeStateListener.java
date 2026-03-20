package com.hegi64.combatMode64.listeners;

import com.hegi64.combatMode64.display.CombatStatusDisplay;
import com.hegi64.combatMode64.utils.CombatModeUtil;
import com.hegi64.combatMode64.utils.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatModeStateListener implements Listener {

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode newGameMode = event.getNewGameMode();

        if (ConfigUtil.isGamemodeSwitchingAllowed()) {

            if (newGameMode == GameMode.SURVIVAL || newGameMode == GameMode.ADVENTURE) {
                return;
            } else {
                CombatModeUtil.disableCombatMode(player, true);
            }

        } else if (CombatModeUtil.isInCombatMode(player)) {

            if (newGameMode == GameMode.SURVIVAL || newGameMode == GameMode.ADVENTURE) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (CombatModeUtil.isInCombatMode(player)) {
            CombatModeUtil.disableCombatMode(player, false);
            player.sendMessage(ChatColor.YELLOW + "Combat mode disabled because you rejoined the server.");
        }

        // Initialize combat status display for the joining player
        CombatStatusDisplay.onPlayerJoin(player);
        CombatStatusDisplay.updateDisplay(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        CombatStatusDisplay.removeDisplay(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!CombatModeUtil.isInCombatMode(player)) {
            return;
        }

        CombatModeUtil.disableCombatMode(player, false);
        player.sendMessage(ChatColor.YELLOW + "Combat mode disabled because you changed worlds.");
    }

}
