/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer;

import me.aj4real.dataplus.DataPlusNMS;
import org.bukkit.plugin.Plugin;

public class Biomizer {
    private NMS nms = null;
    private DataPlusNMS dataplus = null;
    private Plugin plugin = null;
    private KnowItAll knowItAll = null;
    public static Biomizer INSTANCE = null;
    public void onLoad(Plugin plugin) {
        Biomizer.INSTANCE = this;
        this.plugin = plugin;
    }

    public void onEnable(Plugin plugin, NMS nms) {
        this.nms = nms;
        this.nms.onEnable(plugin);
        this.dataplus = me.aj4real.dataplus.Dist.init(plugin);
        me.aj4real.simplepackets.Dist.init(plugin, "biomizer");
        this.knowItAll = new KnowItAll();
        this.knowItAll.onEnable(plugin);
        this.knowItAll.getBiomes().forEach((b) -> {
            if(b.getTemperatureModifier().isPresent()) System.out.println(b.getTemperatureModifier().get());
        });
    }

    public void onDisable(Plugin plugin) {
    }

    public KnowItAll getKnowItAll() {
        return this.knowItAll;
    }

    public DataPlusNMS getDataPlus() {
        return this.dataplus;
    }

    public NMS getNMS() {
        return this.nms;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
