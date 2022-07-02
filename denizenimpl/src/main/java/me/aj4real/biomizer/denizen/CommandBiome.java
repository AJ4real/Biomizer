/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.denizen;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import me.aj4real.biomizer.Biomizer;
import me.aj4real.biomizer.api.CustomBiome;
import me.aj4real.dataplus.api.login.Biome;
import me.aj4real.dataplus.api.nbt.NBTCompoundTag;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandBiome extends AbstractCommand {
    public CommandBiome() {
        setName("biome");
        setSyntax("biome [id:<ElementTag>] " +
                "(create/modify/remove/show/dump) " +
                "(extends:<BiomeTag>) " +
                "(chunk:<ChunkTag>) " +
                "(grass_color:<ColorTag>) " +
                "(sky_color:<ColorTag>) " +
                "(water_color:<ColorTag>) " +
                "(foliage_color:<ColorTag>) " +
                "(water_fog_color:<ColorTag>) " +
                "(fog_color:<ColorTag>) " +
                "(precipitation:<ElementTag>) " +
                "(temperature:<ElementTag>) " +
                "(downfall:<ElementTag>) " +
                "(sound:<ElementTag>) " +
                "(ambient_sound:<ElementTag>) " +
                "(particle:<ElementTag>) " +
                "(particle_probability:<ElementTag>) " +
                "(grass_color_modifier:<ElementTag>) " +
                "(cancel)");
        setRequiredArguments(2, 13);
    }

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("id:", Biomizer.INSTANCE.getKnowItAll()
                .getCustomBiomes()
                .stream()
                .map(Biome::getName)
                .map(NamespacedKey::toString)
                .collect(Collectors.toSet()));
        tab.addWithPrefix("extends:", org.bukkit.block.Biome.values());
        Set<String> p = new HashSet<>();
        p.add("NONE"); p.add("RAIN"); p.add("SNOW");
        tab.addWithPrefix("precipitation:", p);
        tab.addWithPrefix("particle:", Biomizer.INSTANCE.getNMS().getParticleTypes().stream().map(NamespacedKey::toString).collect(Collectors.toSet()));
        tab.addWithPrefix("sound:", Biomizer.INSTANCE.getNMS().getAvailableSounds());
        tab.addWithPrefix("ambient_sound:", Biomizer.INSTANCE.getNMS().getAvailableSounds());
        tab.addWithPrefix("grass_color_modifier:", Biomizer.INSTANCE.getNMS().getGrassColorModifiers());
    }

    @Override
    public void parseArgs(ScriptEntry e) throws InvalidArgumentsException {
        for (Argument arg : e) {
            if (arg.matchesEnum(Action.class)) {
                e.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
            } else if (arg.matchesPrefix("grass_color", "sky_color", "water_color", "foliage_color", "fog_color", "water_fog_color")) {
                if (arg.matchesArgumentType(ColorTag.class)) e.addObject(arg.prefix, arg.asType(ColorTag.class));
                else Debug.echoError(e, "Argument '" + arg.prefix + "' requires a value of type ColorTag.");
            } else if (arg.matchesPrefix("precipitation")) {
                if (arg.matches("NONE", "RAIN", "SNOW")) e.addObject(arg.prefix, arg.asElement());
                else Debug.echoError(e, "Argument '" + arg.prefix + "' requires a value of NONE, RAIN, or SNOW.");
            } else if (arg.matchesPrefix("temperature", "downfall", "particle_probability")) {
                if (arg.asElement().isDouble()) e.addObject(arg.prefix, arg.asElement());
                else Debug.echoError(e, "Argument '" + arg.prefix + "' requires a Decimal value.");
            } else if (arg.matchesPrefix("chunk")) {
                if (arg.matchesArgumentType(ChunkTag.class)) e.addObject(arg.prefix, arg.asType(ChunkTag.class));
                else Debug.echoError(e, "Argument '" + arg.prefix + "' requires a value of type ChunkTag.");
            } else if (arg.matchesPrefix("extends")) {
                try {
                    org.bukkit.block.Biome biome = org.bukkit.block.Biome.valueOf(arg.asElement().asString().toUpperCase());
                    if (biome != org.bukkit.block.Biome.CUSTOM) e.addObject(arg.prefix, biome);
                } catch (Exception err) {
                }
                if (!e.hasObject(arg.prefix))
                    Debug.echoError(e, "Argument '" + arg.prefix + "' must be the name of a vanilla biome.");
            } else if (arg.matchesPrefix("grass_color_modifier")) {
                if (arg.matchesArgumentType(ElementTag.class)) {
                    e.addObject(arg.prefix, arg.asElement());
                }
            } else if (arg.matchesPrefix("id", "particle", "sound", "ambient_sound")) {
                if (arg.matchesArgumentType(ElementTag.class)) {
                    NamespacedKey name = NamespacedKey.fromString(arg.asElement().asString());
                    if(name != null) e.addObject(arg.prefix, name);
                    else Debug.echoError(e, "Argument '" + arg.prefix + "' must be in format 'namespace:key'.");
                }
                else Debug.echoError(e, "Argument '" + arg.prefix + "' requires a value of type ElementTag.");
            } else if (arg.matches("cancel")) {
                e.addObject("cancel", true);
            }
        }
        if(!e.hasObject("action")) {
            throw new InvalidArgumentsException("Missing action, Must use CREATE, MODIFY, REMOVE, DUMP, or SHOW.");
        }
        if(!e.hasObject("id")) {
            throw new InvalidArgumentsException("Missing 'id' argument.");
        }
        Action action = (Action) e.getObject("action");
        if(action == Action.CREATE) {
            if(!e.hasObject("extends")) {
                throw new InvalidArgumentsException("Missing 'extends' argument.");
            }
            if(Biomizer.INSTANCE.getKnowItAll().getCustomBiome((NamespacedKey) e.getObject("id")) != null) {
                throw new InvalidArgumentsException("Cannot CREATE biome '" + e.getElement("id").asString() + "', Already exists.");
            }
        }
        if(action == Action.SHOW && !e.hasObject("chunk")) {
            throw new InvalidArgumentsException("Missing 'chunk' argument.");
        }
    }

    @Override
    public void execute(ScriptEntry e) {
        Action action = (Action) e.getObject("action");
        NamespacedKey key = (NamespacedKey) e.getObject("id");
        if (action == Action.CREATE || action == Action.MODIFY) {
            CustomBiome biome;
            if (action == Action.CREATE) {
                org.bukkit.block.Biome extended = (org.bukkit.block.Biome) e.getObject("extends");
                biome = new CustomBiome(key, extended);
                Biomizer.INSTANCE.getKnowItAll().add(key, biome);
            } else {
                biome = Biomizer.INSTANCE.getKnowItAll().getCustomBiome((NamespacedKey) e.getObject("id"));
            }
            if (e.hasObject("grass_color")) {
                Color value = ((ColorTag) e.getObjectTag("grass_color")).getColor();
                biome.setGrassColor(value.getRed(), value.getGreen(), value.getBlue());
            }
            if (e.hasObject("sky_color")) {
                Color value = ((ColorTag) e.getObjectTag("sky_color")).getColor();
                biome.setSkyColor(value.getRed(), value.getGreen(), value.getBlue());
            }
            if (e.hasObject("water_color")) {
                Color value = ((ColorTag) e.getObjectTag("water_color")).getColor();
                biome.setWaterColor(value.getRed(), value.getGreen(), value.getBlue());
            }
            if (e.hasObject("foliage_color")) {
                Color value = ((ColorTag) e.getObjectTag("foliage_color")).getColor();
                biome.setFoliageColor(value.getRed(), value.getGreen(), value.getBlue());
            }
            if (e.hasObject("water_fog_color")) {
                Color value = ((ColorTag) e.getObjectTag("water_fog_color")).getColor();
                biome.setWaterFogColor(value.getRed(), value.getGreen(), value.getBlue());
            }
            if (e.hasObject("fog_color")) {
                Color value = ((ColorTag) e.getObjectTag("fog_color")).getColor();
                biome.setFogColor(value.getRed(), value.getGreen(), value.getBlue());
            }
            if (e.hasObject("precipitation")) {
                biome.setPrecipitation(e.getElement("precipitation").asString());
            }
            if (e.hasObject("temperature")) {
                biome.setTemperature(e.getElement("temperature").asDouble());
            }
            if (e.hasObject("downfall")) {
                biome.setDownfall(e.getElement("downfall").asDouble());
            }
            if (e.hasObject("ambient_sound")) {
                biome.setSound(((NamespacedKey)e.getObject("ambient_sound")).toString());
            }
            if (e.hasObject("sound")) {
                biome.setSound(((NamespacedKey)e.getObject("sound")).toString());
            }
            if (e.hasObject("grass_color_modifier")) {
                biome.setGrassColorModifier(e.getElement("grass_color_modifier").asString());
            }
            if (e.hasObject("particle")) {
                NBTCompoundTag tag = biome.getParticle().orElse(new NBTCompoundTag());
                NBTCompoundTag options = new NBTCompoundTag();
                options.putNamespacedKey("type", (NamespacedKey) e.getObject("particle"));
                tag.putCompound("options", options);
                biome.setParticle(tag);
            }
            if (e.hasObject("particle_probability")) {
                NBTCompoundTag tag = biome.getParticle().orElse(new NBTCompoundTag());
                tag.putFloat("probability", e.getElement("particle_probability").asFloat());
                biome.setParticle(tag);
            }
        } else if(action == Action.SHOW) {
            ChunkTag chunk = e.getObjectTag("chunk");
            if(e.hasObject("cancel")) Biomizer.INSTANCE.getKnowItAll().setBiome(chunk.getChunk(), null);
            else Biomizer.INSTANCE.getKnowItAll().setBiome(chunk.getChunk(), key);
        } else if(action == Action.REMOVE) {
            Biomizer.INSTANCE.getKnowItAll().removeBiome(Biomizer.INSTANCE.getKnowItAll().getCustomBiome(key));
        } else if(action == Action.DUMP) {

        }
        e.setFinished(true);
    }

    public enum Action {
        CREATE,
        MODIFY,
        SHOW,
        REMOVE,
        DUMP
    }
}
