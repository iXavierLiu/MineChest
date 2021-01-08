package com.github.errcodex.MineChest.Listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;

import static com.github.errcodex.MineChest.Tools.*;

public class SignInteract implements Listener {
    private static Plugin plugin;

    public SignInteract(Plugin plugin) {
        SignInteract.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        // 棍子触发
        if (event.getItem() == null || event.getItem().getType() != Material.STICK)
            return;

        // 触发附着在物体上的告示牌
        Block targetBlock = event.getClickedBlock();
        if (null == targetBlock || !(targetBlock.getState() instanceof Sign))
            return;

        Sign sign = (Sign) targetBlock.getState();
        ArrayList<String> text = new ArrayList<String>(Arrays.asList(sign.getLines()));

        // empty sign
        for (String t : text) {
            if (!t.isEmpty()) return;
        }

        // 附着在箱子上
        Inventory inventory = getWallChestInventory(sign, event);
        if (null == inventory) return;

        ItemStack[] contents = inventory.getContents();
        int lineNum = 0;
        for (ItemStack itemStack : contents) {
            if (null == itemStack || itemStack.getAmount() == 0) continue;
            String name = "[" + itemStack.getType().name().toLowerCase() + "]";

            if (text.contains(name)) continue;
            text.set(lineNum++, name);
            if (lineNum >= 4) break;
        }

        String tip = "";
        for (int i = 0; i < text.size(); ++i) {
            if (text.get(i).isEmpty()) continue;
            tip += " | " + text.get(i);
            sign.setLine(i, text.get(i));
        }
        if (tip.isEmpty()) return;
        tip = tip.substring(3);
        event.getPlayer().sendMessage("[MineChest] You wrote \"" + tip + "\" on the sign");
        plugin.getLogger().info(event.getPlayer().getName() + " wrote\"" + tip + "\" on the sign");
        sign.update();
    }

    // 获取sign在哪个箱子上
    private Inventory getWallChestInventory(Sign sign, final PlayerInteractEvent event) {
        if (null == sign) return null;
        BlockFace face = ((Directional) sign.getBlockData()).getFacing();

        Block block = sign.getBlock().getRelative(face.getOppositeFace());
        return getBlockInventory(block, event);
    }
}
