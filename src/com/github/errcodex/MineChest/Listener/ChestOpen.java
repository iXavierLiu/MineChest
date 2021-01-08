package com.github.errcodex.MineChest.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static com.github.errcodex.MineChest.Tools.isChest;

public class ChestOpen implements Listener {
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        InventoryType inventoryType = inventory.getType();
        if (!isChest(inventory))
            return;

        ItemStack[] contents = inventory.getContents();
        // combine same block
        for (int i = 0; i < contents.length; i++) {
            ItemStack target = contents[i];
            if (target == null) continue;
            for (int j = i + 1; j < contents.length; j++) {
                ItemStack current = contents[j];
                if (current == null) continue;
                if (target.getType() != current.getType()) continue;

                if (!target.getItemMeta().equals(current.getItemMeta())) continue;
                int maxStackSize = target.getMaxStackSize();
                if (target.getAmount() >= maxStackSize) continue;

                if (target.getAmount() + current.getAmount() > maxStackSize) {
                    current.setAmount(current.getAmount() - maxStackSize + target.getAmount());
                    target.setAmount(maxStackSize);
                    continue;
                }
                target.setAmount(current.getAmount() + target.getAmount());
                contents[j] = null;
            }
        }

        //sort
        Collections.sort(Arrays.asList(contents), new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack o1, ItemStack o2) {
                if (o1 == null && o2 == null) return 0;

                // empty term comes after
                if (o1 == null) return 1;
                if (o2 == null) return -1;

                // sort by name
                return o1.getType().compareTo(o2.getType());
            }
        });
        inventory.setContents(contents);

    }
}
