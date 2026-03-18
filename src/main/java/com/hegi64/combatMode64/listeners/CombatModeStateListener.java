package com.hegi64.combatMode64.listeners;

import com.hegi64.combatMode64.utils.CombatModeUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatModeStateListener implements Listener {
    private final Map<UUID, CommandSender> pendingGamemodeInitiators = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (!CombatModeUtil.isInCombatMode(player)) {
            return;
        }

        if (event.getNewGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (CombatModeUtil.isInCombatMode(player)) {
            CombatModeUtil.disableCombatMode(player, false);
            player.sendMessage(ChatColor.YELLOW + "Combat mode disabled because you rejoined the server.");
        }
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
