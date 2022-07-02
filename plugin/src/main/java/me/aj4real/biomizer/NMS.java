/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public interface NMS {
    void onEnable(Plugin plugin);
    Object newBiome(String namespace, Biome extended);
    int getBiomeId(Object nmsBiome);
    void sendChunkUpdate(Chunk chunk);
    Set<NamespacedKey> getParticleTypes();
    Set<String> getGrassColorModifiers();
    Set<String> getAvailableSounds();
}
