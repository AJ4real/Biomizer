/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Dist {
    private final Biomizer main = new Biomizer();
    public void onLoad(Plugin plugin) {
        main.onLoad(plugin);
    }
    public void onEnable(Plugin plugin) {
        String regex = "\\d+(\\.\\d+)+";
        String strVer = Bukkit.getVersion();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(strVer);
        matcher.find();
        strVer = 'v' + matcher.group();
        try {
            plugin.getLogger().log(Level.INFO, Dist.class.getCanonicalName() + ": Attempting to load NMS interface for " + strVer);
            Version ver = Version.valueOf(strVer.replace('.', '_'));
            NMS nms = ver.nms.newInstance();
            main.onEnable(plugin, nms);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, Dist.class.getCanonicalName() + ": Could not initiate support for " + strVer + ", Is it a supported version?", e);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }
    public void onDisable(Plugin plugin) {
        main.onDisable(plugin);
    }

    public Biomizer getBiomizer() {
        return main;
    }

    public enum Version {
        v1_18(me.aj4real.biomizer.nms.v1_18.NMSImpl.class),
        v1_18_1(me.aj4real.biomizer.nms.v1_18_1.NMSImpl.class),
        v1_18_2(me.aj4real.biomizer.nms.v1_18_2.NMSImpl.class),
        v1_19(me.aj4real.biomizer.nms.v1_19.NMSImpl.class),
        v1_19_1(me.aj4real.biomizer.nms.v1_19_1.NMSImpl.class),
        v1_19_2(me.aj4real.biomizer.nms.v1_19_2.NMSImpl.class);
        private final Class<? extends NMS> nms;
        Version(Class<? extends NMS> nms) {
            this.nms = nms;
        }
    }
}
