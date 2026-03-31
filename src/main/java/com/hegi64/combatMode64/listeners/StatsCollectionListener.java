package com.hegi64.combatMode64.listeners;

import com.hegi64.combatMode64.stats.StatsService;
import com.hegi64.combatMode64.utils.CombatModeUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class StatsCollectionListener implements Listener {

    private final StatsService statsService;

    public StatsCollectionListener(StatsService statsService) {
        this.statsService = statsService;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }

        String worldName = victim.getWorld().getName();
        if (!CombatModeUtil.isCombatModeEnabledForWorld(worldName)) {
            return;
        }

        if (!CombatModeUtil.isInCombatMode(killer) || !CombatModeUtil.isInCombatMode(victim)) {
            return;
        }

        EntityDamageEvent lastDamageEvent = victim.getLastDamageCause();
        String deathCause = lastDamageEvent == null ? "UNKNOWN" : lastDamageEvent.getCause().name();
        String weaponType = killer.getInventory().getItemInMainHand().getType().name();

        Location killerLocation = killer.getLocation();
        Location deathLocation = victim.getLocation();
        double distance = killerLocation.distance(deathLocation);

        statsService.recordKill(
            killer.getUniqueId(),
            killer.getName(),
            victim.getUniqueId(),
            victim.getName(),
            worldName,
            deathCause,
            weaponType,
            killerLocation.getX(),
            killerLocation.getY(),
            killerLocation.getZ(),
            deathLocation.getX(),
            deathLocation.getY(),
            deathLocation.getZ(),
            distance
        );
    }
}
