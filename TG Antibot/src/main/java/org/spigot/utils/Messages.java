package org.spigot.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spigot.Main;

import java.io.File;
import java.io.IOException;

public class Messages {

    private final Main plugin;
    private FileConfiguration config;
    private File file;

    public Messages(Main plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
    }

    public String get(String path) {
        return config.getString(path, "Missing message: " + path)
                .replace("&", "§");
    }

    public String get(String path, String def) {
        return config.getString(path, def).replace("&", "§");
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
