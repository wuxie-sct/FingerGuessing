package me.wuxie.fingerguessing;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.MessageFormat;
import java.util.*;

/**
 * 猜拳实例
 */
public class FGInstance implements InventoryHolder {
    public static final ItemStack STONE = new ItemStack(Material.STONE);
    public static final ItemStack SHEARS = new ItemStack(Material.SHEARS);
    public static final ItemStack LEATHER = new ItemStack(Material.LEATHER);
    public static final ItemStack DIAMOND = new ItemStack(Material.DIAMOND);
    static {
        ItemMeta meta = STONE.getItemMeta();
        meta.setDisplayName("§a石头");
        STONE.setItemMeta(meta);
        meta = SHEARS.getItemMeta();
        meta.setDisplayName("§a剪刀");
        SHEARS.setItemMeta(meta);
        meta = LEATHER.getItemMeta();
        meta.setDisplayName("§a布");
        LEATHER.setItemMeta(meta);
        meta = DIAMOND.getItemMeta();
        meta.setDisplayName("§a石头?§b剪刀?§d布?");
        meta.setLore(Arrays.asList(
                "§e------------------",
                "§b你能猜到ta出了啥吗?",
                "§7你出拳后就能知晓~",
                "§e------------------"
        ));
        DIAMOND.setItemMeta(meta);
    }
    // 赌注(金币)
    @Getter
    private final int aBet;
    // 拳头 - [ 0 石头 1 剪刀 2 布 ]
    @Getter
    private final int id;
    // 发布猜拳的玩家
    @Getter
    private final UUID uuid;
    @Getter
    private final String name;
    @Getter
    private final long time;

    @Getter
    @Setter
    private boolean valid = true;

    public FGInstance(Player player, int id, int aBet,long time){
        this(player.getUniqueId(),player.getName(),id,aBet,time);
    }

    @Getter
    private final ItemStack itemStack;
    public FGInstance(UUID uuid, String name, int id, int aBet, long time){
        this.aBet=aBet;
        this.id=id;
        this.uuid=uuid;
        this.name=name;
        this.time=time;
        this.itemStack = new ItemStack(Material.DIAMOND,1);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(String.format(FingerGuessing.getPlugin().getConfig().getString("showName"),name));
        double tax = FingerGuessing.getPlugin().getConfig().getDouble("tax");
        meta.setLore(format(FingerGuessing.getPlugin().getConfig().getStringList("showInfo"),name,aBet,aBet*tax));
        this.itemStack.setItemMeta(meta);
    }

    private static List<String> format(List<String> list,Object...o){
        List<String> format = new ArrayList<>();
        for (String s:list){
            format.add(MessageFormat.format(s,o));
        }
        return format;
    }

    /**
     * Get the object's inventory.
     *
     * @return The inventory.
     */
    @Override
    public Inventory getInventory() {
        return null;
    }

    public void open(Player player){
        Inventory inventory = Bukkit.createInventory(this,54,String.format(FingerGuessing.getPlugin().getConfig().getString("showName"),name));
        for (int a=0;a<54;){
            inventory.setItem(a,MainUI.zs);
            a+=1;
        }
        inventory.setItem(13,DIAMOND);
        inventory.setItem(22,itemStack);
        ItemStack stack = STONE.clone();
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(Collections.singletonList("§a我是 §e石头 §c>>> §b选我?"));
        stack.setItemMeta(meta);
        inventory.setItem(38,stack);
        stack = SHEARS.clone();
        meta = stack.getItemMeta();
        meta.setLore(Collections.singletonList("§a我是 §e剪刀 §c>>> §b选我?"));
        stack.setItemMeta(meta);
        inventory.setItem(40,stack);
        stack = LEATHER.clone();
        meta = stack.getItemMeta();
        meta.setLore(Collections.singletonList("§a我是 §e布 §c>>> §b选我?"));
        stack.setItemMeta(meta);
        inventory.setItem(42,stack);
        player.openInventory(inventory);
    }

    public void onClick(Player player,Inventory inventory,int slot){
        int id = -1;
        if(slot == 38){// 0
            id = 0;
        } else if(slot == 40) {// 1
            id = 1;
        } else if(slot == 42) {// 2
            id = 2;
        }
        if(id!=-1){
            if(check(id,player)){
                ItemStack i = inventory.getItem(slot);
                ItemMeta meta = i.getItemMeta();
                meta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                i.setItemMeta(meta);
                inventory.setItem(slot,i);
                ItemStack stack;
                if(this.id==0){
                    stack = STONE.clone();
                } else if(this.id==1){
                    stack = SHEARS.clone();
                } else {
                    stack = LEATHER.clone();
                }
                meta = stack.getItemMeta();
                meta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                stack.setItemMeta(meta);
                inventory.setItem(13,stack);
                valid = false;
                FingerGuessing.getManager().getList().remove(this);
            }
        }
    }

    public boolean check(int id,Player player){
        if(!valid){
            player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("invalid"));
            return false;
        }
        if(MoneyUtil.has(player,aBet)) {
            if (id == this.id) {
                // 平局
                player.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("tied"), "你", name));
                Player player1 = Bukkit.getPlayer(uuid);
                if(player1 != null){
                    MoneyUtil.give(player1,aBet);
                }
                if (player1 != null && player1.isOnline()) {
                    player1.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("tied"), player.getName(), "你"));
                }
            } else if (id == 0 && this.id == 1 || id == 1 && this.id == 2 || id == 2 && this.id == 0) {
                // 赢局
                MoneyUtil.give(player, aBet * FingerGuessing.getPlugin().getConfig().getDouble("tax"));
                player.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("win"), "你", name));
                Player player1 = Bukkit.getPlayer(uuid);
                if (player1 != null && player1.isOnline()) {
                    player1.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("lose"), player.getName(), "你"));
                }
            } else {
                // 输局
                MoneyUtil.take(player, aBet);
                player.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("lose"), "你", name));
                Player player1 = Bukkit.getPlayer(uuid);
                if (player1 != null && player1.isOnline()) {
                    player1.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("win"), player.getName(), "你"));
                }
            }
            return true;
        } else {
            player.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("vault1"),name,aBet));
            return false;
        }
    }
}
