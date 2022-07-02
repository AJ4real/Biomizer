/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer;

import me.aj4real.biomizer.api.CustomBiome;
import me.aj4real.dataplus.api.login.Biome;
import me.aj4real.dataplus.api.login.LoginPacketEditor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KnowItAll implements Listener {
    public static NamespacedKey storage = NamespacedKey.fromString("me.aj4real.biomizer:biome");
    private Map<Player, Set<NamespacedKey>> players = new HashMap<>();
    private Map<NamespacedKey, Object> biomes = new HashMap<>();
    private Map<NamespacedKey, Biome> biomes2 = new HashMap<>();
    private Map<String, Map<String, CustomBiome>> customBiomes = new HashMap<>();
    private Set<CustomBiome> customBiomes2 = new HashSet<>();
    private Map<NamespacedKey, Set<Chunk>> chunks = new HashMap<>();
    public void onEnable(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadBiomes();
    }
    public void reloadBiomes() {
        Biomizer.INSTANCE.getDataPlus().getDefaultLoginCodec();
        LoginPacketEditor editor = new LoginPacketEditor(Biomizer.INSTANCE.getDataPlus().getDefaultLoginCodec());
        editor.getBiomes().forEach((b) -> biomes2.put(b.getName(), b));
    }
    public Biome getBiome(NamespacedKey name) {
        return biomes2.get(name);
    }
    public Object getNMSBiome(NamespacedKey name) {
        return biomes.get(name);
    }
    public CustomBiome getCustomBiome(NamespacedKey name) {
        Map<String, CustomBiome> map = customBiomes.get(name.getNamespace());
        if(map != null) {
            return map.get(name.getKey());
        }
        return null;
    }
    public Object should(Player player, Chunk chunk) {
        if(chunk.getPersistentDataContainer().has(storage, PersistentDataType.STRING)) {
            String data = chunk.getPersistentDataContainer().get(storage, PersistentDataType.STRING);
            NamespacedKey key = NamespacedKey.fromString(data);
            if(players.get(player).contains(key)) return biomes.get(key);
        }
        return null;
    }

    public void add(Player player, Set<NamespacedKey> knownBiomes) {
        players.put(player, knownBiomes);
    }
    public void add(NamespacedKey name, CustomBiome biome) {
        if (!biomes.containsKey(name)) {
            Object nmsBiome = Biomizer.INSTANCE.getNMS().newBiome(name.toString(), biome.getExtended());
            biomes.put(name, nmsBiome);
        }
        biome.setId(Biomizer.INSTANCE.getNMS().getBiomeId(biomes.get(name)));
        biomes2.put(name, biome);
        customBiomes.computeIfAbsent(name.getNamespace(), (c) -> new HashMap<>()).put(name.getKey(), biome);
        customBiomes2.add(biome);
    }
    public void setBiome(Chunk chunk, NamespacedKey biome) {
        if (biome != null) {
            chunks.computeIfAbsent(biome, (c) -> new HashSet<>()).add(chunk);
            chunk.getPersistentDataContainer().set(storage, PersistentDataType.STRING, biome.toString());
        } else {
            chunks.computeIfAbsent(biome, (c) -> new HashSet<>()).remove(chunk);
            chunk.getPersistentDataContainer().remove(storage);
        }
        Biomizer.INSTANCE.getNMS().sendChunkUpdate(chunk);
    }
    public void removeBiome(CustomBiome biome) {
        String str = biome.getName().toString();
        for (CustomBiome b : customBiomes2) {
            if (b.getName().toString().equalsIgnoreCase(str)) {
                customBiomes.get(biome.getName().getNamespace()).remove(biome.getName().getKey());
                customBiomes2.remove(b);
                biomes.remove(b.getName());
                biomes2.remove(b.getName());
                chunks.get(b.getName()).forEach((c) -> {
                    Biomizer.INSTANCE.getNMS().sendChunkUpdate(c);
                });
                return;
            }
        }
    }
    public Set<Biome> getBiomes() {
        return new HashSet<>(biomes2.values());
    }
    public Set<CustomBiome> getCustomBiomes() {
        return new HashSet<>(this.customBiomes2);
    }
    @EventHandler
    public void e(PlayerQuitEvent e) {
        players.remove(e.getPlayer());
    }

}
