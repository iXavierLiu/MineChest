package io.xavierliu.mc.minechest;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class App extends JavaPlugin implements Listener {
    private static final String pluginName = "MineChest";
    private static Plugin plugin;

    public App() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new ChestArranger(), this);

        getLogger().info(pluginName + " loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info(pluginName + " unloaded!");
    }

    public static Plugin getPlugin() {
        return plugin;
    }
}
