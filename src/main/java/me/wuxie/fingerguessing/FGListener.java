package me.wuxie.fingerguessing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.MessageFormat;

public class FGListener implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        if(MainUI.UUIDS.containsKey(player.getUniqueId())){
            e.setCancelled(true);
            int id = MainUI.UUIDS.get(player.getUniqueId());
            String s = e.getMessage();
            try {
                int v = Integer.parseInt(s);
                if(v<1){
                    player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("vault3"));
                    return;
                }
                if(!MoneyUtil.has(player,v)){
                    player.sendMessage(MessageFormat.format(FingerGuessing.getPlugin().getConfig().getString("vault2"),v,MoneyUtil.get(player)));
                    return;
                }
                MoneyUtil.take(player,v);
                FingerGuessing.getManager().getList().add(new FGInstance(player,id,v,System.currentTimeMillis()));
                player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("launch2"));
                MainUI.UUIDS.remove(player.getUniqueId());
            }catch (NumberFormatException exception){
                player.sendMessage(FingerGuessing.getPlugin().getConfig().getString("vault3"));
            }
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(e.getClickedInventory()==null){
            return;
        }
        if(e.getSlot()<0){
            return;
        }
        if(e.getInventory().getHolder() instanceof MainUI){
            e.setCancelled(true);
            ((MainUI) e.getInventory().getHolder()).handlerClick((Player) e.getWhoClicked(),e.getSlot());
        }else if(e.getInventory().getHolder() instanceof FGInstance){
            e.setCancelled(true);
            ((FGInstance) e.getInventory().getHolder()).onClick((Player) e.getWhoClicked(),e.getInventory(),e.getSlot());
        }
    }
}
