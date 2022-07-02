/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.api;

import me.aj4real.biomizer.Biomizer;
import me.aj4real.dataplus.api.login.Biome;
import org.bukkit.NamespacedKey;

public class CustomBiome extends Biome {
    private org.bukkit.block.Biome extended;
    private CustomBiome(NamespacedKey name, Biome extended) {
        super(
                name,
                extended.getTemperature(),
                extended.getDownfall(),
                extended.getPrecipitation(),
                extended.getSkyColor(),
                extended.getWaterColor(),
                extended.getFogColor(),
                extended.getWaterFogColor()
        );
        setId(extended.getId());
        setAmbientSound(extended.getAmbientSound().orElse(null));
        setCategory(extended.getCategory().orElse(null));
        setTemperatureModifier(extended.getTemperatureModifier().orElse(null));
        setParticle(extended.getParticle().orElse(null));
        setGrassColorModifier(extended.getGrassColorModifier().orElse(null));
        setSound(extended.getSound().orElse(null));
        if(extended.getTickDelay().isPresent()) setTickDelay(extended.getTickDelay().get());
        if(extended.getOffset().isPresent()) setOffset(extended.getOffset().get());
        if(extended.getGrassColor().isPresent()) setGrassColor(extended.getGrassColor().get());
        if(extended.getFoliageColor().isPresent()) setFoliageColor(extended.getFoliageColor().get());
        if(extended.getBlockSearchExtent().isPresent()) this.setBlockSearchExtent(extended.getBlockSearchExtent().get());
    }
    public CustomBiome(NamespacedKey name, org.bukkit.block.Biome extended) {
        this(name, Biomizer.INSTANCE.getKnowItAll().getBiome(extended.getKey()));
        this.extended = extended;
    }

    public org.bukkit.block.Biome getExtended() {
        return extended;
    }
}
