package com.hegi64.combatMode64.listeners;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.display.StatsSidebarDisplay;
import com.hegi64.combatMode64.stats.StatsService;
import com.hegi64.combatMode64.utils.CombatModeUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;

public class StatsCollectionListener implements Listener {

    private static final NamespacedKey PROJECTILE_WEAPON_KEY = new NamespacedKey(Main.getInstance(), "projectile_weapon");

    private final StatsService statsService;
    private final StatsSidebarDisplay statsSidebarDisplay;

    public StatsCollectionListener(StatsService statsService, StatsSidebarDisplay statsSidebarDisplay) {
        this.statsService = statsService;
        this.statsSidebarDisplay = statsSidebarDisplay;
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getBow() == null || event.getProjectile() == null) {
            return;
        }

        event.getProjectile().getPersistentDataContainer().set(
            PROJECTILE_WEAPON_KEY,
            PersistentDataType.STRING,
            event.getBow().getType().name()
        );
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
        String weaponType = resolveWeaponType(lastDamageEvent, killer);

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

        if (statsSidebarDisplay != null) {
            statsSidebarDisplay.refreshAfterKill(killer, victim);
        }
    }

    private String resolveWeaponType(EntityDamageEvent lastDamageEvent, Player killer) {
        if (lastDamageEvent instanceof EntityDamageByEntityEvent byEntityEvent) {
            Entity damager = byEntityEvent.getDamager();
            if (damager instanceof Projectile projectile) {
                String taggedWeapon = projectile.getPersistentDataContainer().get(PROJECTILE_WEAPON_KEY, PersistentDataType.STRING);
                if (taggedWeapon != null && !taggedWeapon.isBlank()) {
                    return taggedWeapon;
                }

                if (projectile.getType() == EntityType.TRIDENT) {
                    return Material.TRIDENT.name();
                }

                return projectile.getType().name();
            }

            if (damager instanceof Player playerDamager) {
                return playerDamager.getInventory().getItemInMainHand().getType().name();
            }
        }

        return killer.getInventory().getItemInMainHand().getType().name();
    }
}
