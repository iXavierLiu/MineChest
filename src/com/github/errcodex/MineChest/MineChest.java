package com.github.errcodex.MineChest;

import com.github.errcodex.MineChest.Listener.ChestInteract;
import com.github.errcodex.MineChest.Listener.ChestOpen;
import com.github.errcodex.MineChest.Listener.SignInteract;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        ChestInteract chestInteract = new ChestInteract();
        SignInteract signInteract = new SignInteract();

        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(chestOpen, this);
        pluginManager.registerEvents(chestInteract, this);
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
