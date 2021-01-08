package com.github.errcodex.MineChest.Listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.errcodex.MineChest.Tools.*;

public class ChestInteract implements Listener {
    private static final String pluginName = "MineChest";
    static final String classificationTag = "分类";
    static final String classificationTagEN = "classify";

    private static Plugin plugin;

    public ChestInteract(Plugin plugin) {
        ChestInteract.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        // 棍子触发
        if (event.getItem() == null || event.getItem().getType() != Material.STICK)
            return;

        // 触发箱子
        Block targetBlock = event.getClickedBlock();
        if (null == targetBlock || !isChest(targetBlock))
            return;

        Inventory inventory = getBlockInventory(targetBlock, event);

        // 防止重复寻找箱子，当扫描到大箱子时，把另一半箱子的坐标加入进来
        ArrayList<Location> visited = new ArrayList<Location>();

        // 将触发箱子放入无效箱子队列中
        visited.add(targetBlock.getLocation());

        // 普通箱子则检测是否为双箱
        Block another = null;
        if (targetBlock.getType() == Material.CHEST) {
            Chest chest = (Chest) targetBlock.getState();
            // 获得触发箱子的另一半（如果是大箱子）
            another = getAnotherChestBlock(chest);
            if (null != another)
                visited.add(another.getLocation());
        }

        int yRange = 0;
        int xzRange = 0;

        // 箱子上是否有符合的告示牌，"[分类]\n[yRange,xzRange]"
        ArrayList<String> signText = getSignText(getWallSign(targetBlock));
        if (null == signText || signText.size() < 2 || !checkClassifyTag(signText.get(0))) {
            signText = getSignText(getWallSign(another));
            if (null == signText || signText.size() < 2 || !checkClassifyTag(signText.get(0)))
                return;
        }

        //第二行是范围，需满足"[yRange,xzRange]"形式
        Pattern pattern = Pattern.compile("(\\d+),(\\d+)");
        Matcher m = pattern.matcher(signText.get(1));
        if (!m.find()) {
            event.getPlayer().sendMessage("[" + pluginName + "] error parameters，The format should be \"[yRange,xzRange]\"");
            event.setCancelled(true);
            return;
        }
        yRange = Math.abs(Integer.parseInt(m.group(1)));
        xzRange = Math.abs(Integer.parseInt(m.group(2)));
        // 扫描范围过大
        if (yRange > 25 || xzRange > 50 || yRange * xzRange * xzRange > 32767) {
            event.getPlayer().sendMessage("[" + pluginName + "] error parameters！");
            event.getPlayer().sendMessage("[" + pluginName + "] yRange<=25, xzRange<=50, volume<=10000");
            event.setCancelled(true);
            return;
        }

        //检测通过，进入自动分类
        event.getPlayer().sendMessage("[" + pluginName + "] (yRange:" + yRange + ", xzRange:" + xzRange + ")classifying...");
        event.setCancelled(true);
        int count = 0; // 分类物品的数量

        long startTime = System.currentTimeMillis();
        Location inventoryLocation = targetBlock.getLocation();
        World world = event.getPlayer().getWorld();
        for (int y = inventoryLocation.getBlockY() - yRange; y <= inventoryLocation.getBlockY() + yRange; ++y)
            for (int x = inventoryLocation.getBlockX() - xzRange; x <= inventoryLocation.getBlockX() + xzRange; ++x)
                for (int z = inventoryLocation.getBlockZ() - xzRange; z <= inventoryLocation.getBlockZ() + xzRange; ++z) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.isEmpty())
                        continue;
                    if (!isChest(block))
                        continue;
                    if (visited.contains(block.getLocation()))
                        continue;

                    // 普通箱子则检测是否为双箱
                    Block chestAnother = null;
                    if (block.getType() == Material.CHEST) {
                        Chest itChest = (Chest) block.getState();
                        chestAnother = getAnotherChestBlock(itChest);
                        // 双箱则只检测一个箱子
                        if (null != chestAnother)
                            visited.add(chestAnother.getLocation());
                    }
                    Inventory itInventory = getBlockInventory(block, event);

                    //箱子上的告示牌
                    Sign wall_sign = getWallSign(block);
                    if (null == wall_sign) {
                        wall_sign = getWallSign(chestAnother);
                        if (null == wall_sign) continue;
                    }

                    // 分类存放的箱子必须有分类标签，并且第一行不能是"[分类]"
                    ArrayList<String> names = getSignText(wall_sign);
                    if (null == names || names.isEmpty()) continue;
                    if (checkClassifyTag(names.get(0))) continue;


                    ItemStack[] contents = itInventory.getContents();
                    if (0 == contents.length) continue;
                    for (String name : names) {
                        Material material = Material.matchMaterial(name);
                        if (null == material) continue;
                        count += chestClassify(inventory, itInventory, material);
                    }
                }
        long usedTime = System.currentTimeMillis() - startTime;
        event.getPlayer().sendMessage("[" + pluginName + "] you moved " + count + " items, used " + usedTime + "ms");
        plugin.getLogger().info(event.getPlayer().getName() + " moved " + count + " items, used " + usedTime + "ms");
    }

    // 获取附着在箱子正面的告示牌
    private Sign getWallSign(Block block) {

        if (null == block)
            return null;
        if (!(block.getBlockData() instanceof Directional))
            return null;

        Block facingBlock = getFacingBlock(block);
        if (!(facingBlock.getState() instanceof Sign))
            return null;
        // 告示牌是贴在箱子上的，而不是贴在其他方块上其他方块
        if (!getOppositeFacingBlock(facingBlock).getLocation().equals(block.getLocation()))
            return null;
        return (Sign) facingBlock.getState();
    }

    private Block getAnotherChestBlock(Chest chest) {
        Inventory inventory = chest.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            Chest cLeft = (Chest) doubleChest.getLeftSide();
            Chest cRight = (Chest) doubleChest.getRightSide();

            if (cLeft.getLocation().equals(chest.getLocation()))
                return cRight.getBlock();
            else
                return cLeft.getBlock();
        }
        return null;
    }

    static Pattern textPattern = Pattern.compile("\\[(.*)\\]");

    // 获取告示牌文本，文本需要符合"[text]"格式
    private ArrayList<String> getSignText(Sign sign) {
        ArrayList<String> result = new ArrayList<String>();
        if (null == sign)
            return null;
        String[] string = sign.getLines();
        for (String text : string) {
            Matcher matcher = textPattern.matcher(text);
            if (!matcher.find())
                continue;
            result.add(matcher.group(1));
        }
        return result;
    }

    // 自动分类
    private int chestClassify(Inventory from, Inventory to, Material material) {
        int count = 0;
        ItemStack[] fromContents = from.getContents();
        ItemStack[] toContents = to.getContents();

        for (int i = 0; i < fromContents.length; i++) {
            ItemStack fromItemStack = fromContents[i];
            if (fromItemStack == null) continue;
            if (fromItemStack.getType() != material) continue;
            int maxStackSize = fromItemStack.getMaxStackSize();

            for (int j = 0; j < toContents.length; j++) {
                ItemStack toItemStack = toContents[j];
                if (toItemStack == null) {
                    toContents[j] = fromItemStack;
                    fromContents[i] = null;

                    count += fromItemStack.getAmount();
                    break;
                }

                if (fromItemStack.getType() != toItemStack.getType()) continue;
                if (!fromItemStack.getItemMeta().equals(toItemStack.getItemMeta())) continue;
                if (toItemStack.getAmount() >= maxStackSize) continue;

                if (fromItemStack.getAmount() + toItemStack.getAmount() > maxStackSize) {
                    count += maxStackSize - toItemStack.getAmount();

                    fromItemStack.setAmount(fromItemStack.getAmount() - maxStackSize + toItemStack.getAmount());
                    toItemStack.setAmount(maxStackSize);
                    continue;
                }

                toItemStack.setAmount(fromItemStack.getAmount() + toItemStack.getAmount());
                fromContents[i] = null;
                count += fromItemStack.getAmount();
                break;
            }
        }
        from.setContents(fromContents);
        to.setContents(toContents);
        return count;
    }

    private Block getFacingBlock(Block block) {
        BlockFace face = ((Directional) block.getBlockData()).getFacing();
        return block.getRelative(face);
    }

    private Block getOppositeFacingBlock(Block block) {
        BlockFace face = ((Directional) block.getBlockData()).getFacing();
        return block.getRelative(face.getOppositeFace());
    }

    private Boolean checkClassifyTag(String text) {
        if (!text.equals(classificationTag) && !text.equals(classificationTagEN))
            return false;
        return true;
    }
}
