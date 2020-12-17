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

public class ChestInteract implements Listener {
    private static final String pluginName = "MineChest";
    static final String classificationTag = "分类";
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
        if (null == targetBlock || targetBlock.getType() != Material.CHEST)
            return;

        Chest chest = (Chest) targetBlock.getState();
        Inventory inventory = chest.getInventory();

        // 防止重复寻找箱子，当扫描到大箱子时，把另一半箱子的坐标加入进来
        ArrayList<Location> visited = new ArrayList<Location>();

        // 将触发箱子放入无效箱子队列中
        visited.add(chest.getLocation());
        // 获得触发箱子的另一半（如果是大箱子）
        Chest another = getAnotherChest(chest);
        if (null != another)
            visited.add(another.getLocation());

        int yRange = 0;
        int xzRange = 0;

        // 箱子上是否有符合的告示牌，"[分类]\n[yRange,xzRange]"
        ArrayList<String> signText = getSignText(getWallSign(chest));
        if (null == signText || signText.size() < 2 || !signText.get(0).equals(classificationTag)) {
            signText = getSignText(getWallSign(another));
            if (null == signText || signText.size() < 2 || !signText.get(0).equals(classificationTag))
                return;
        }

        //第二行是范围，需满足"[yRange,xzRange]"形式
        Pattern pattern = Pattern.compile("(\\d+),(\\d+)");
        Matcher m = pattern.matcher(signText.get(1));
        if (!m.find()) {
            event.getPlayer().sendMessage("[" + pluginName + "] 参数错误，格式应为\"[yRange,xzRange]\"");
            event.setCancelled(true);
            return;
        }
        yRange = Math.abs(Integer.parseInt(m.group(1)));
        xzRange = Math.abs(Integer.parseInt(m.group(2)));
        // 扫描范围过大
        if (yRange > 25 || xzRange > 50 || yRange * xzRange * xzRange > 32767) {
            event.getPlayer().sendMessage("[" + pluginName + "] 无法分类，请调小参数再试！");
            event.getPlayer().sendMessage("[" + pluginName + "] yRange<=25, xzRange<=50, volume<=10000");
            event.setCancelled(true);
            return;
        }

        //检测通过，进入自动分类
        event.getPlayer().sendMessage("[" + pluginName + "] (yRange:" + yRange + ", xzRange:" + xzRange + ")正在分类中...");
        event.setCancelled(true);
        int count = 0; // 分类物品的数量

        Location inventoryLocation = inventory.getLocation();
        World world = event.getPlayer().getWorld();
        for (int y = inventoryLocation.getBlockY() - yRange; y <= inventoryLocation.getBlockY() + yRange; ++y)
            for (int x = inventoryLocation.getBlockX() - xzRange; x <= inventoryLocation.getBlockX() + xzRange; ++x)
                for (int z = inventoryLocation.getBlockZ() - xzRange; z <= inventoryLocation.getBlockZ() + xzRange; ++z) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.isEmpty())
                        continue;
                    if (block.getType() != Material.CHEST)
                        continue;
                    if (visited.contains(block.getLocation()))
                        continue;

                    Chest itChest = (Chest) block.getState();
                    Inventory itInventory = itChest.getInventory();

                    Chest chestAnother = getAnotherChest(itChest);
                    // 双箱则只检测一个箱子
                    if (null != chestAnother)
                        visited.add(chestAnother.getLocation());

                    //箱子上的告示牌
                    Sign wall_sign = getWallSign(itChest);
                    if (null == wall_sign) {
                        wall_sign = getWallSign(chestAnother);
                        if (null == wall_sign) continue;
                    }

                    // 分类存放的箱子必须有分类标签，并且第一行不能是"[分类]"
                    ArrayList<String> names = getSignText(wall_sign);
                    if (null == names || names.isEmpty()) continue;
                    if (names.get(0).equals(classificationTag)) continue;


                    ItemStack[] contents = itInventory.getContents();
                    if (0 == contents.length) continue;
                    for (String name : names) {
                        Material material = Material.matchMaterial(name);
                        if (null == material) continue;
                        count += chestSort(inventory, itInventory, material);
                        //getLogger().info("分类: " + name);
                    }

                }

        event.getPlayer().sendMessage("[" + pluginName + "] 移动了" + count + "个物品");
        plugin.getLogger().info(event.getPlayer().getName() + "移动了" + count + "个物品");
    }

    // 获取附着在箱子正面的告示牌
    private Sign getWallSign(Chest chest) {
        if (null == chest)
            return null;
        if (!(chest.getBlockData() instanceof Directional))
            return null;

        Block block = getFacingBlock(chest.getBlock());
        if (block.getType() != Material.OAK_WALL_SIGN)
            return null;
        // 告示牌是贴在箱子上的，而不是贴在其他方块上其他方块
        if(!getOppositeFacingBlock(block).getLocation().equals(chest.getLocation()))
            return null;
        return (Sign) block.getState();
    }

    private Chest getAnotherChest(Chest chest) {
        Inventory inventory = chest.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            Chest cLeft = (Chest) doubleChest.getLeftSide();
            Chest cRight = (Chest) doubleChest.getRightSide();

            if (cLeft.getLocation().equals(chest.getLocation()))
                return cRight;
            else
                return cLeft;
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
    private int chestSort(Inventory from, Inventory to, Material material) {
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
                fromContents[j] = null;
                count += fromItemStack.getAmount();
            }
        }
        from.setContents(fromContents);
        to.setContents(toContents);
        return count;
    }

    private Block getFacingBlock(Block block){
        BlockFace face = ((Directional) block.getBlockData()).getFacing();
        return block.getRelative(face);
    }
    private Block getOppositeFacingBlock(Block block){
        BlockFace face = ((Directional) block.getBlockData()).getFacing();
        return block.getRelative(face.getOppositeFace());
    }
}
