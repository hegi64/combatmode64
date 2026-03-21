package com.hegi64.combatMode64.listeners;

import com.hegi64.combatMode64.utils.CombatModeUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victimEntity = event.getEntity();
        if (!(victimEntity instanceof Player victimPlayer)) {
            return;
        }

        Entity damagerEntity = event.getDamager();
        Player damagerPlayer;

        if (damagerEntity instanceof Player player) {
            damagerPlayer = player;
        } else if (damagerEntity instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            damagerPlayer = shooter;
        } else {
            return;
        }

        String worldName = victimPlayer.getWorld().getName();

        if (CombatModeUtil.isCombatModeEnabledForWorld(worldName) && CombatModeUtil.isPvpDisabledByDefault()
            && !(CombatModeUtil.isInCombatMode(damagerPlayer) && CombatModeUtil.isInCombatMode(victimPlayer))) {
            event.setCancelled(true);
        }
    }

}
