package com.redspeaks.minecraftiaeconomy.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Logs {

    private File file = null;
    private FileConfiguration config = null;
    private JavaPlugin plugin;

    public Logs(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        file = new File(plugin.getDataFolder(), "logs.yml");
        if(!file.exists()) {
            try {
                file.createNewFile();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return config;
    }

    public void set(String path, Object value) {
        config.set(path, value);
        save();
    }

    public void save() {
        try{
            config.save(file);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<String> getLogs(String name) {
        return new LinkedList<>(config.getStringList(name));
    }

    public void addLog(String path, String log) {
        if(getLogs(path).size() >= 5) {
            getLogs(path).removeFirst();
            getLogs(path).add(log);
            set(path, getLogs(path));
        } else {
            getLogs(path).add(log);
            set(path, getLogs(path));
        }

    }
}
