package me.wuxie.fingerguessing;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Manager {
    private final FingerGuessing plugin;
    @Getter
    private final List<FGInstance> list = new ArrayList<>();

    public Manager(FingerGuessing plugin){
        this.plugin = plugin;
        load();
    }

    public void load(){
        File file = new File(plugin.getDataFolder(),"data.yml");
        if(file.exists()){
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            for (String key:yml.getKeys(false)){
                UUID uuid = UUID.fromString(key.split("_")[0]);
                String name = key.split("_")[1];
                long time = Long.parseLong(key.split("_")[2]);
                String data = yml.getString(key);
                int id = Integer.parseInt(data.substring(0,1));
                int aBit = Integer.parseInt(data.substring(1));
                FGInstance instance = new FGInstance(uuid,name,id,aBit,time);
                list.add(instance);
            }
        }
    }
    public void save(){
        YamlConfiguration yml = new YamlConfiguration();
        for (FGInstance instance:list){
            String key = instance.getUuid()+"_"+instance.getName()+"_"+instance.getTime();
            String value = instance.getId()+""+instance.getABet();
            yml.set(key,value);
        }
        File file = new File(plugin.getDataFolder(),"data.yml");
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
