/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Dist {
    private static final Biomizer main;
    public static final Version version;
    private static final String strVersion;
    static {
        main = new Biomizer();
        String regex = "\\d+(\\.\\d+)+";
        String strVer = Bukkit.getVersion();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(strVer);
        matcher.find();
        strVer = 'v' + matcher.group();
        version = Version.valueOf(strVersion = strVer.replace('.', '_'));
    }
    public void onLoad(Plugin plugin) {
        main.onLoad(plugin);
    }
    public void onEnable(Plugin plugin) {
        try {
            plugin.getLogger().log(Level.INFO, Dist.class.getCanonicalName() + ": Attempting to load NMS interface for " + strVersion);
            NMS nms = version.nms.newInstance();
            main.onEnable(plugin, nms);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, Dist.class.getCanonicalName() + ": Could not initiate support for " + strVersion + ", Is it a supported version?", e);
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
        v1_19_2(me.aj4real.biomizer.nms.v1_19_2.NMSImpl.class),
        v1_19_3(me.aj4real.biomizer.nms.v1_19_3.NMSImpl.class);
        private final Class<? extends NMS> nms;
        private final int major, minor, micro;
        Version(Class<? extends NMS> nms) {
            this.nms = nms;
            String[] str = name().substring(1).split("_");
            major = Integer.valueOf(str[0]);
            minor = Integer.valueOf(str[1]);
            int _micro;
            try {
                _micro = Integer.valueOf(str[2]);
            } catch (Throwable e) {
                _micro = 0;
            }
            micro = _micro;
        }
    }
}
