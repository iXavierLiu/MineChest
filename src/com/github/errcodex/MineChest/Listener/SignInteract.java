package com.github.errcodex.MineChest.Listener;

import com.github.errcodex.MineChest.MineChest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class SignInteract implements Listener {
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        // 棍子触发
        if (event.getItem() == null || event.getItem().getType() != Material.STICK)
            return;

        // 触发附着在物体上的告示牌
        Block targetBlock = event.getClickedBlock();
        if (null == targetBlock || targetBlock.getType() != Material.OAK_WALL_SIGN)
            return;

        Sign sign = (Sign) targetBlock.getState();
        ArrayList<String> text = new ArrayList<String>(Arrays.asList(sign.getLines()));

        // empty sign
        for (String t : text) {
            if (!t.isEmpty()) return;
        }

        // 附着在箱子上
        Chest chest = getChest(sign);
        if (null == chest) return;

        Inventory inventory = chest.getInventory();
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
        tip=tip.substring(3);
        event.getPlayer().sendMessage("[MineChest] 你向告示牌上写了\"" + tip + "\"");
        MineChest.getPlugin().getLogger().info(event.getPlayer().getName() + "向告示牌上写了\"" + tip + "\"");
        sign.update();
    }

    // 获取sign在哪个箱子上
    private Chest getChest(Sign sign) {
        if (null == sign) return null;
        BlockFace face = ((Directional) sign.getBlockData()).getFacing();

        Block block = sign.getBlock().getRelative(face.getOppositeFace());
        if (block.getType() != Material.CHEST)
            return null;

        return (Chest) block.getState();
    }
}
