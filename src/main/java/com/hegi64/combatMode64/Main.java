package com.hegi64.combatMode64;

import com.hegi64.combatMode64.commands.CombatModeCommand;
import com.hegi64.combatMode64.display.CombatStatusDisplay;
import com.hegi64.combatMode64.listeners.CombatModeStateListener;
import com.hegi64.combatMode64.listeners.PvpListener;
import com.hegi64.combatMode64.utils.ConfigMigrator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    public void onLoad() {
        super.onLoad();

        instance = this;

        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[CombatMode64] Loaded CombatMode64 Plugin");
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        ConfigMigrator.migrate();

        PluginManager manager = Bukkit.getPluginManager();
        registerEvents(manager);
        registerCommandExecutors();
    }

    @Override
    public void onDisable() {
        CombatStatusDisplay.removeAllDisplays();
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "CombatMode64 Plugin has been disabled.");
    }

    public static Main getInstance() {
        return instance;
    }

    private void registerCommandExecutors() {
        this.getCommand("combatmode").setExecutor(new CombatModeCommand());
    }

    private void registerEvents(PluginManager manager) {
        manager.registerEvents(new PvpListener(), this);
        manager.registerEvents(new CombatModeStateListener(), this);
    }
}