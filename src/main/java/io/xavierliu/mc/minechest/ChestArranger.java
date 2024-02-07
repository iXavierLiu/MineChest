package io.xavierliu.mc.minechest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestArranger implements Listener{
    
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        Inventory inventory=event.getInventory();

        // skip items that are not boxes
        if(!Utils.isChest(inventory))return;

        ItemStack[] items=inventory.getContents();
        Utils.CapacityMerge(items);
        Utils.Sort(items);

        //save
        inventory.setContents(items);
    }
}
