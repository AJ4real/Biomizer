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
        this.setId(extended.getId());
        this.setAmbientSound(extended.getAmbientSound().orElse(null));
        this.setCategory(extended.getCategory().orElse(null));
        this.setTemperatureModifier(extended.getTemperatureModifier().orElse(null));
        this.setParticle(extended.getParticle().orElse(null));
        this.setGrassColorModifier(extended.getGrassColorModifier().orElse(null));
        this.setAmbientSound(extended.getSound().orElse(null));
        if(extended.getTickDelay().isPresent())
            this.setTickDelay(extended.getTickDelay().get());
        if(extended.getOffset().isPresent())
            this.setOffset(extended.getOffset().get());
        if(extended.getGrassColor().isPresent())
            this.setGrassColor(extended.getGrassColor().get());
        if(extended.getFoliageColor().isPresent())
            this.setFoliageColor(extended.getFoliageColor().get());
        if(extended.getBlockSearchExtent().isPresent())
            this.setBlockSearchExtent(extended.getBlockSearchExtent().get());
    }
    public CustomBiome(NamespacedKey name, org.bukkit.block.Biome extended) {
        this(name, Biomizer.INSTANCE.getKnowItAll().getBiome(extended.getKey()));
        this.extended = extended;
    }

    public org.bukkit.block.Biome getExtended() {
        return extended;
    }
}
