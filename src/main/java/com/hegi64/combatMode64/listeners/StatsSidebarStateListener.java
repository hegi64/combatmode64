package com.hegi64.combatMode64.listeners;

import com.hegi64.combatMode64.display.StatsSidebarDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StatsSidebarStateListener implements Listener {

    private final StatsSidebarDisplay statsSidebarDisplay;

    public StatsSidebarStateListener(StatsSidebarDisplay statsSidebarDisplay) {
        this.statsSidebarDisplay = statsSidebarDisplay;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        statsSidebarDisplay.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        statsSidebarDisplay.handleQuit(event.getPlayer());
    }
}

