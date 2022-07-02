/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Dist {
    private final Biomizer main = new Biomizer();
    public void onLoad(Plugin plugin) {
        main.onLoad(plugin);
    }
    public void onEnable(Plugin plugin) {
        String s = Arrays.stream(Package.getPackages())
                .map(Package::getName)
                .filter(n -> n.startsWith("org.bukkit.craftbukkit.v1_"))
                .collect(Collectors.toList()).stream().findFirst().get()
                .replace("org.bukkit.craftbukkit.", "").split("\\.")[0];
        try {
            plugin.getLogger().log(Level.INFO, Dist.class.getCanonicalName() + ": Attempting to load NMS interface for " + s);
            NMS nms = Version.valueOf(s).nms.newInstance();
            main.onEnable(plugin, nms);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, Dist.class.getCanonicalName() + ": Could not initiate support for " + s + ", Is it a supported version?", e);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
    }
    public void onDisable(Plugin plugin) {
        main.onDisable(plugin);
    }

    public Biomizer getBiomizer() {
        return main;
    }

    public enum Version {
        v1_18_R1(me.aj4real.biomizer.nms.v1_18_R1.NMSImpl.class),
        v1_18_R2(me.aj4real.biomizer.nms.v1_18_R2.NMSImpl.class),
        v1_19_R1(me.aj4real.biomizer.nms.v1_19_R1.NMSImpl.class);
        private final Class<? extends NMS> nms;
        Version(Class<? extends NMS> nms) {
            this.nms = nms;
        }
    }
}
