/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.towny;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    ConfigurationSection config;
    File file;
    TownyImpl plugin;
    public Configuration(TownyImpl plugin) {
        this.plugin = plugin;
    }
    public void load() {
        plugin.getDataFolder().mkdir();
        if((this.file = new File(plugin.getDataFolder(), "config.yml")).exists()) {
            this.config = YamlConfiguration.loadConfiguration(file);
        } else {
            this.config = new YamlConfiguration();
        }
        this.config = copyDefaults(new YamlConfiguration());
    }
    public ConfigurationSection copyDefaults(ConfigurationSection config) {
        List<String> blacklistedWorlds = new ArrayList<>();
        blacklistedWorlds.add("example_world_nether");
        blacklistedWorlds.add("example_world_the_end");
        config.addDefault("blacklisted_worlds", blacklistedWorlds);
        return config;
    }
}
