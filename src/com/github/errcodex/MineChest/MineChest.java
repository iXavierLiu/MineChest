package com.github.errcodex.MineChest;

import com.github.errcodex.MineChest.Listener.ChestInteract;
import com.github.errcodex.MineChest.Listener.ChestOpen;
import com.github.errcodex.MineChest.Listener.SignInteract;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MineChest extends JavaPlugin implements Listener {
    private static final String pluginName = "MineChest";
    private static Plugin plugin;

    public MineChest() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        PluginManager pluginManager = getServer().getPluginManager();
        ChestOpen chestOpen = new ChestOpen();
        ChestInteract chestInteract = new ChestInteract(plugin);
        SignInteract signInteract = new SignInteract(plugin);

        pluginManager.registerEvents(this, this);
        // 自动整理
        pluginManager.registerEvents(chestOpen, this);
        // 自动分类
        pluginManager.registerEvents(chestInteract, this);
        // 木牌自动填写
        pluginManager.registerEvents(signInteract, this);

        getLogger().info(pluginName + " loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info(pluginName + " disabled!");
    }

    public static Plugin getPlugin() {
        return plugin;
    }
}
