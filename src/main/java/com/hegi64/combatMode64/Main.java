package com.hegi64.combatMode64;

import com.hegi64.combatMode64.commands.CombatModeCommand;
import com.hegi64.combatMode64.display.CombatStatusDisplay;
import com.hegi64.combatMode64.listeners.CombatModeStateListener;
import com.hegi64.combatMode64.listeners.PvpListener;
import com.hegi64.combatMode64.listeners.StatsCollectionListener;
import com.hegi64.combatMode64.stats.StatsService;
import com.hegi64.combatMode64.utils.ConfigMigrator;
import com.hegi64.combatMode64.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private StatsService statsService;

    public void onLoad() {
        super.onLoad();

        instance = this;

        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[CombatMode64] Loaded CombatMode64 Plugin");
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        ConfigMigrator.migrate();

        initializeStatsService();

        PluginManager manager = Bukkit.getPluginManager();
        registerEvents(manager);
        registerCommandExecutors();
    }

    @Override
    public void onDisable() {
        CombatStatusDisplay.removeAllDisplays();

        if (statsService != null) {
            statsService.stop();
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "CombatMode64 Plugin has been disabled.");
    }

    public static Main getInstance() {
        return instance;
    }

    public StatsService getStatsService() {
        return statsService;
    }

    private void initializeStatsService() {
        StatsService service = new StatsService(this);
        if (!service.start()) {
            if (ConfigUtil.isStatsEnabled()) {
                getLogger().warning("Stats service is not active.");
            }
            return;
        }

        this.statsService = service;
    }

    private void registerCommandExecutors() {
        if (this.getCommand("combatmode") != null) {
            this.getCommand("combatmode").setExecutor(new CombatModeCommand());
        }
    }

    private void registerEvents(PluginManager manager) {
        manager.registerEvents(new PvpListener(), this);
        manager.registerEvents(new CombatModeStateListener(), this);

        if (statsService != null && statsService.isActive()) {
            manager.registerEvents(new StatsCollectionListener(statsService), this);
        }
    }
}