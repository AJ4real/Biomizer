/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.denizen;

import com.denizenscript.denizencore.DenizenCore;
import me.aj4real.biomizer.Biomizer;
import me.aj4real.biomizer.Dist;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DenizenImpl extends JavaPlugin implements Listener {
    Dist dist = null;
    Biomizer biomizer = null;
    public void onLoad() {
        dist = new Dist();
        dist.onLoad(this);
        biomizer = dist.getBiomizer();
    }
    public void onEnable() {
        dist.onEnable(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    public void onDisable() {
        dist.onDisable(this);
    }
    @EventHandler
    public void e(ServerLoadEvent e) {
        if(e.getType() == ServerLoadEvent.LoadType.STARTUP) {
            init();
        }
    }
    public void init() {
        DenizenCore.commandRegistry.register("biome", new CommandBiome());
    }
}
