package com.hegi64.combatMode64.listeners;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.utils.CombatModeUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity victimEntity = event.getEntity();
        if (!(damagerEntity instanceof Player damagerPlayer) || !(victimEntity instanceof Player victimPlayer)) {
            return;
        }

        String worldName = victimPlayer.getWorld().getName();

        if (CombatModeUtil.isCombatModeEnabledForWorld(worldName) && CombatModeUtil.isPvpDisabledByDefault()
            && !(CombatModeUtil.isInCombatMode(damagerPlayer) && CombatModeUtil.isInCombatMode(victimPlayer))) {
            event.setCancelled(true);
        }
    }

}
