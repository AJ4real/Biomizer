/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import me.aj4real.biomizer.Biomizer;
import me.aj4real.biomizer.Dist;
import me.aj4real.biomizer.KnowItAll;
import me.aj4real.biomizer.api.CustomBiome;
import me.aj4real.dataplus.api.nbt.NBTCompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class TownyImpl extends JavaPlugin implements Listener {
    Configuration configuration = null;
    Dist dist = null;
    Biomizer biomizer = null;
    public static final Map<Town, CustomBiome> biomes = new HashMap<>();
    private AddonCommand cmd = null;
    public void onLoad() {
        dist = new Dist();
        dist.onLoad(this);
        biomizer = dist.getBiomizer();
        configuration = new Configuration(this);
    }
    public void onEnable() {
        dist.onEnable(this);
        TownyAPI.getInstance().getTowns().forEach((t) -> {
            process(t, false);
        });
        KnowItAll.setProvider((p,c) -> {
            Town town = TownyAPI.getInstance().getTown(c.getBlock(0, 0, 0).getLocation());
            CustomBiome biome = biomes.get(town);
            return biome != null ? biome.getName() : null;
        });
        new Palette(); // static init
        Bukkit.getPluginManager().registerEvents(this, this);
        BiomeCommand c = new BiomeCommand(this);
        AddonCommand cmd = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWN, "biome", c);
        cmd.setTabCompleter(c);
        TownyCommandAddonAPI.addSubCommand(cmd);
    }
    public void onDisable() {
        dist.onDisable(this);
        if(this.cmd != null) TownyCommandAddonAPI.removeSubCommand(this.cmd);
    }
    @EventHandler
    public void e(NewTownEvent e) {
        process(e.getTown());
    }
    @EventHandler
    public void e(RenameTownEvent e) {
        biomizer.getKnowItAll().removeBiome(biomes.get(e.getTown()));
        process(e.getTown());
    }
    @EventHandler
    public void e(TownUnclaimEvent e) {
        Chunk c = e.getWorldCoord().getBukkitWorld().getChunkAt(e.getWorldCoord().getX(), e.getWorldCoord().getZ());
        biomizer.getNMS().sendChunkUpdate(c);
    }
    @EventHandler
    public void e(TownClaimEvent e) {
        Chunk c = e.getTownBlock().getWorldCoord().getBukkitWorld().getChunkAt(e.getTownBlock().getX(), e.getTownBlock().getZ());
        biomizer.getNMS().sendChunkUpdate(c);
    }
    @EventHandler
    public void e(TownRuinedEvent e) {
        biomizer.getKnowItAll().removeBiome(biomes.get(e.getTown()));
    }
    public void process(Town town) {
        process(town, true);
    }
    public void process(Town town, boolean update) {
        try {
            NamespacedKey key = NamespacedKey.fromString("towny:" + town.getName().toLowerCase().replace(" ", "_"));
            if(key == null) throw new IllegalArgumentException();
            CustomBiome biome = new CustomBiome(key, Biome.FOREST);
            biomizer.getKnowItAll().newBiome(key, biome);
            biomes.put(town, biome);
            if(update)
                town.getTownBlocks().forEach((tb) -> {
                    biomizer.getNMS().sendChunkUpdate(tb.getWorldCoord().getBukkitWorld().getChunkAt(tb.getX(), tb.getZ()));
                });
            update(town);
        } catch(IllegalArgumentException e) {
            getLogger().log(Level.SEVERE, "Town " + town.getName() + " cannot be converted to valid namespace key!", e);
        }
    }
    public void update(Town town) {
        CustomBiome biome = biomes.get(town);
        if(town.hasMeta("biome.grasscolor")) {
            IntegerDataField v = (IntegerDataField) town.getMetadata("biome.grasscolor");
            biome.setGrassColor(v.getValue());
        } else {
            biome.setGrassColor(null);
        }
        if(town.hasMeta("biome.skycolor")) {
            IntegerDataField v = (IntegerDataField) town.getMetadata("biome.skycolor");
            biome.setSkyColor(v.getValue());
        } else {
            biome.setSkyColor(null);
        }
        if(town.hasMeta("biome.foliagecolor")) {
            IntegerDataField v = (IntegerDataField) town.getMetadata("biome.foliagecolor");
            biome.setFoliageColor(v.getValue());
        } else {
            biome.setFoliageColor(null);
        }
        if(town.hasMeta("biome.watercolor")) {
            IntegerDataField v = (IntegerDataField) town.getMetadata("biome.watercolor");
            biome.setWaterColor(v.getValue());
        } else {
            biome.setWaterColor(null);
        }
        if(town.hasMeta("biome.fogcolor")) {
            IntegerDataField v = (IntegerDataField) town.getMetadata("biome.fogcolor");
            biome.setFogColor(v.getValue());
        } else {
            biome.setFogColor(null);
        }
        if(town.hasMeta("biome.waterfogcolor")) {
            IntegerDataField v = (IntegerDataField) town.getMetadata("biome.waterfogcolor");
            biome.setWaterFogColor(v.getValue());
        } else {
            biome.setWaterFogColor(null);
        }
        if(town.hasMeta("biome.precipitation")) {
            StringDataField v = (StringDataField) town.getMetadata("biome.precipitation");
            biome.setPrecipitation(v.getValue());
        } else {
            biome.setPrecipitation("none");
        }
        if (town.hasMeta("biome.particle")) {
            StringDataField v = (StringDataField) town.getMetadata("biome.particle");
            NBTCompoundTag tag = biome.getParticle().orElse(new NBTCompoundTag());
            tag.putFloat("probability", 0.015f);
            NBTCompoundTag options = new NBTCompoundTag();
            options.putNamespacedKey("type", NamespacedKey.fromString(v.getValue()));
            tag.putCompound("options", options);
            biome.setParticle(tag);
        } else {
            biome.setParticle(null);
        }
        biome.isMod();
    }
}
