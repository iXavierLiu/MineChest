package io.xavierliu.mc.minechest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Utils {
    public static void CapacityMerge(ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            // get a target to merge
            ItemStack target = items[i];
            if (target == null)
                continue;

            // skip if already full
            final int maxSize = target.getMaxStackSize();
            if (target.getAmount() >= maxSize)
                continue;

            // traverse items to merge
            for (int j = i + 1; j < items.length; j++) {
                // try item
                ItemStack current = items[j];
                if (current == null)
                    continue;

                // skip if its' not save type or meta
                if (current.getType() != target.getType())
                    continue;
                if (!current.getItemMeta().equals(target.getItemMeta()))
                    continue;

                // merge capcity
                int total = current.getAmount() + target.getAmount();
                if (total > maxSize) {
                    target.setAmount(maxSize);
                    current.setAmount(total - maxSize);
                } else {
                    target.setAmount(total);
                    items[j] = null;
                }
            }
        }
    }

    public static void Sort(ItemStack[] items) {
        Collections.sort(Arrays.asList(items), new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack o1, ItemStack o2) {
                if (o1 == null && o2 == null)
                    return 0;

                // empty term comes after
                if (o1 == null)
                    return 1;
                if (o2 == null)
                    return -1;

                // sort by name
                return o1.getType().compareTo(o2.getType());
            }
        });
    }

    public static boolean isChest(Block block) {
        switch (block.getType()) {
            case CHEST: // A chest inventory, with 0, 9, 18, 27, 36, 45, or 54 slots of type CONTAINER.
            case SHULKER_BOX: // A shulker box inventory, with 27 slots of type CONTAINER.
            case ENDER_CHEST: // The ender chest inventory, with 27 slots.
            case BARREL: // A barrel box inventory, with 27 slots of type CONTAINER.
                return true;
            default:
                return false;
        }
    }

    public static boolean isChest(Inventory inventory) {
        switch (inventory.getType()) {
            case CHEST: // A chest inventory, with 0, 9, 18, 27, 36, 45, or 54 slots of type CONTAINER.
            case SHULKER_BOX: // A shulker box inventory, with 27 slots of type CONTAINER.
            case ENDER_CHEST: // The ender chest inventory, with 27 slots.
            case BARREL: // A barrel box inventory, with 27 slots of type CONTAINER.
                return true;
            default:
                return false;
        }
    }
}
