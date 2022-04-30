package me.wuxie.fingerguessing;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.MessageFormat;
import java.util.*;

public class MainUI implements InventoryHolder {
    protected static final ItemStack zs;
    private static final ItemStack last;
    private static final ItemStack next;
    static {
        boolean flag = FingerGuessing.getServerVersion()>12;
        Material material = Material.getMaterial(flag?"BLACK_STAINED_GLASS_PANE":"STAINED_GLASS_PANE");
        zs = flag?new ItemStack(material,1):new ItemStack(material,1, (short) 7);
        ItemMeta meta = zs.getItemMeta();
        meta.setDisplayName(" ");
        zs.setItemMeta(meta);

        Material feather = Material.FEATHER;
        last = new ItemStack(feather,1);
        meta = last.getItemMeta();
        meta.setDisplayName("§e[§a<- 上一页§e]");
        last.setItemMeta(meta);

        next = last.clone();
        meta.setDisplayName("§e[§b下一页 ->§e]");
        next.setItemMeta(meta);
    }

    public MainUI(){
    }
    @Getter
    private int page;
    @Getter
    private Inventory inventory;
    @Getter
    private Map<Integer,FGInstance> fgInstanceMap;

    public Inventory open(Player player,int page){
        fgInstanceMap = new HashMap<>();
        this.page = page;
        inventory = Bukkit.createInventory(this,54,String.format(FingerGuessing.getPlugin().getConfig().getString("mainTitle"),page));
        List<FGInstance> instanceList = FingerGuessing.getManager().getList();
        int slotIndex = 0;
        for (int index = (page-1)*45;index<page*45;){
            if(instanceList.size()>index){
                FGInstance instance = instanceList.get(index);
                fgInstanceMap.put(slotIndex,instance);
                inventory.setItem(slotIndex,instance.getItemStack());
            }else break;
            index+=1;
            slotIndex+=1;
        }
        inventory.setItem(45,last);
        inventory.setItem(46,zs);
        inventory.setItem(47,zs);
        ItemStack stack = FGInstance.STONE.clone();
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(Arrays.asList("§a发起一场猜拳游戏!","§b我是>>>§c石头!"));
        stack.setItemMeta(meta);
        inventory.setItem(48,stack);
        stack = FGInstance.SHEARS.clone();
        meta = stack.getItemMeta();
        meta.setLore(Arrays.asList("§a发起一场猜拳游戏!","§b我是>>>§c剪刀!"));
        stack.setItemMeta(meta);
        inventory.setItem(49,stack);
        stack = FGInstance.LEATHER.clone();
        meta = stack.getItemMeta();
        meta.setLore(Arrays.asList("§a发起一场猜拳游戏!","§b我是>>>§c布!"));
        stack.setItemMeta(meta);
        inventory.setItem(50,stack);
        inventory.setItem(51,zs);
        inventory.setItem(52,zs);
        inventory.setItem(53,next);
        player.openInventory(inventory);
        return inventory;
    }
    public static final Map<UUID,Integer> UUIDS = new HashMap<>();
    public void handlerClick(Player player,int slot){
        if(slot == 45){
            if(page>1){
                open(player,page-1);
            }
        } else if(slot == 48){
            UUIDS.put(player.getUniqueId(),0);
            player.closeInventory();
            player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("launch1"));
        } else if(slot == 49){
            UUIDS.put(player.getUniqueId(),1);
            player.closeInventory();
            player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("launch1"));
        } else if(slot == 50){
            UUIDS.put(player.getUniqueId(),2);
            player.closeInventory();
            player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("launch1"));
        } else if(slot == 53){
            open(player,page+1);
        } else if(fgInstanceMap.containsKey(slot)){
            FGInstance instance = fgInstanceMap.get(slot);
            if(instance.getUuid().equals(player.getUniqueId())){
                player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("self"));
                return;
            }
            if(!MoneyUtil.has(player,instance.getABet())) {
                player.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("vault1"),instance.getName(),instance.getABet()));
                return;
            }
            if(!instance.isValid()){
                player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("invalid"));
                return;
            }
            instance.open(player);
        }
    }
}
