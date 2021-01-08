package com.github.errcodex.MineChest;

import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class Tools {
    public static boolean isChest(Block block) {
        switch (block.getType()) {
            case CHEST:             // A chest inventory, with 0, 9, 18, 27, 36, 45, or 54 slots of type CONTAINER.
            case SHULKER_BOX:       // A shulker box inventory, with 27 slots of type CONTAINER.
            case ENDER_CHEST:       // The ender chest inventory, with 27 slots.
            case BARREL:            // A barrel box inventory, with 27 slots of type CONTAINER.
                return true;
        }
        return false;
    }

    public static boolean isChest(Inventory inventory) {
        switch (inventory.getType()) {
            case CHEST:             // A chest inventory, with 0, 9, 18, 27, 36, 45, or 54 slots of type CONTAINER.
            case SHULKER_BOX:       // A shulker box inventory, with 27 slots of type CONTAINER.
            case ENDER_CHEST:       // The ender chest inventory, with 27 slots.
            case BARREL:            // A barrel box inventory, with 27 slots of type CONTAINER.
                return true;
        }
        return false;
    }

    public static Inventory getBlockInventory(Block block, final PlayerInteractEvent event) {
        Material type = block.getType();
        if (type == Material.CHEST)
            return ((Chest) block.getState()).getInventory();
        else if (type == Material.SHULKER_BOX)
            return ((ShulkerBox) block.getState()).getInventory();
        else if (type == Material.ENDER_CHEST)
            return event.getPlayer().getEnderChest();
        else if (type == Material.BARREL)
            return ((Barrel) block.getState()).getInventory();

        return null;
    }
}
