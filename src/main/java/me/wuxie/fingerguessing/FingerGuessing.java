package me.wuxie.fingerguessing;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FingerGuessing extends JavaPlugin {
    @Getter
    private static Manager manager;
    @Getter
    private static int serverVersion;
    @Getter
    private static FingerGuessing plugin;
    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        initServerVersion();
        MoneyUtil.init();
        manager = new Manager(this);
        Bukkit.getPluginManager().registerEvents(new FGListener(),this);
        Bukkit.getPluginCommand("fg").setExecutor(this);
        Bukkit.getConsoleSender().sendMessage("§e[猜拳] §a小游戏猜拳已经成功启动!");
        Bukkit.getConsoleSender().sendMessage("§e[猜拳] §b作者>>>wuxie §cQQ835937470!");
        Bukkit.getConsoleSender().sendMessage("§e[猜拳] §b如有定制需求欢迎联系我~");
        Bukkit.getConsoleSender().sendMessage("§e[猜拳] §b此插件的获取渠道仅为作者wuxie本人");
        Bukkit.getConsoleSender().sendMessage("§e[猜拳] §b或者爱发电购买(https://afdian.net/@wuxie666)~");
        Bukkit.getConsoleSender().sendMessage("§e[猜拳] §b若你从别人购得/分发，则为倒卖行为!");
    }

    @Override
    public void onDisable() {
        manager.save();
    }

    public void initServerVersion(){
        String version = Bukkit.getVersion();
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (char c:version.toCharArray()){
            if(!flag){
                if(c=='(')flag=true;
            } else {
                if(c==')')break;
                sb.append(c);
            }
        }
        serverVersion = Integer.parseInt(sb.substring(4).split("\\.")[1]);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length==0){
            sender.sendMessage("§7/fg open [页码] §c打开猜拳界面!");
            if(sender.isOp())
            sender.sendMessage("§7/fg reload §c重载config.yml!");
            return true;
        }else {
            if(args[0].equalsIgnoreCase("open")){
                if(sender instanceof Player){
                    if(sender.hasPermission("FingerGuessing.open")){
                        int page = 1;
                        if(args.length>1){
                            try {
                                page = Integer.parseInt(args[1]);
                            }catch (NumberFormatException ignored){
                            }
                        }
                        MainUI ui = new MainUI();
                        ui.open((Player) sender,page);
                    }
                } else {
                    sender.sendMessage("§7控制台无法使用此命令，需要玩家!");
                }
            }else if(args[0].equalsIgnoreCase("reload")){
                if(sender.isOp()){
                    reloadConfig();
                    sender.sendMessage("§a重载完成!");
                }
            }
        }
        return true;
    }
}
