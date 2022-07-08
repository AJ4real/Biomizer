/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import me.aj4real.biomizer.Biomizer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class BiomeCommand implements CommandExecutor, TabCompleter {
    private static final List<String> options = Collections.unmodifiableList(
            Arrays.asList("grasscolor", "skycolor", "foliagecolor", "watercolor",
                    "fogcolor", "waterfogcolor", "precipitation", "particle"));
    private static final List<String> precip = Collections.unmodifiableList(Arrays.asList("SNOW", "RAIN", "NONE"));
    private static final BiPredicate<Integer, CommandSender> check = (i, s) -> {
        if(i > 255) {
            s.sendMessage(Strings.VALUE_ABOVE_255);
            return true;
        } else if(i < 0) {
            s.sendMessage(Strings.VALUE_BELOW_0);
            return true;
        }
        return false;
    };
    private final TownyImpl impl;
    public BiomeCommand(TownyImpl impl) {
        this.impl = impl;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            Resident res = TownyAPI.getInstance().getResident((Player) sender);
            Town t = res.getTown();
            if(!res.isMayor()) {
                sender.sendMessage(Strings.MAYOR_ONLY);
                return true;
            }
            if(args.length == 0) {
                ComponentBuilder cb = new ComponentBuilder(String.format(Strings.PLATE, "Biome"))
                        .append("\n");
                cb.append(new ComponentBuilder("[GrassColor]")
                        .color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome grasscolor"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change grass color.")))
                        .create()).append("\n");

                cb.append(new ComponentBuilder("[SkyColor]")
                        .color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome skycolor"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change sky color.")))
                        .create()).append("\n");

                cb.append(new ComponentBuilder("[FoliageColor]")
                        .color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome foliagecolor"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change foliage color.")))
                        .create()).append("\n");

                cb.append(new ComponentBuilder("[WaterColor]")
                        .color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome watercolor"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change water color.")))
                        .create()).append("\n");

                cb.append(new ComponentBuilder("[FogColor]")
                        .color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome fogcolor"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change fog color.")))
                        .create()).append("\n");

                cb.append(new ComponentBuilder("[WaterFogColor]")
                        .color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome waterfogcolor"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change water fog color.")))
                        .create()).append(" ");
                sender.spigot().sendMessage(cb.create());
                return true;
            }
            if(!options.contains(args[0].toLowerCase())) {
                return true;
            }
            switch(args[0].toLowerCase()) {
                case "grasscolor": {}
                case "skycolor": {}
                case "foliagecolor": {}
                case "watercolor": {}
                case "fogcolor": {}
                case "waterfogcolor": {
                    if(args.length == 1) {
                        Palette.send((Player) sender, args[0]);
                        return true;
                    } else {
                        int r = Integer.parseInt(args[1]);
                        int g = Integer.parseInt(args[2]);
                        int b = Integer.parseInt(args[3]);
                        if(check.test(r, sender)) return true;
                        if(check.test(g, sender)) return true;
                        if(check.test(b, sender)) return true;
                        t.addMetaData(new IntegerDataField("biome." + args[0], -16777216 | r << 16 | g << 8 | b));
                        impl.update(t);
                        sender.sendMessage(Strings.SUCCESS);
                    }
                    break;
                }
                case "precipitation": {
                    if(args.length == 1) {

                    } else {
                        if(!precip.contains(args[1].toUpperCase())) {
                            sender.sendMessage(Strings.NOT_A_VALID_PRECIPITATION);
                            return true;
                        }
                        t.addMetaData(new StringDataField("biome.precipitation", args[1].toUpperCase()));
                        impl.update(t);
                        sender.sendMessage(Strings.SUCCESS);
                    }
                    break;
                }
                case "particle": {
                    if(args.length == 1) {

                    } else {
                        if(!sender.hasPermission("biomizer.particles") && !sender.isOp()) {
                            sender.sendMessage(Strings.PARTICLE_NO_PERMISSION);
                            return true;
                        }
                        NamespacedKey k = NamespacedKey.minecraft(args[1].toLowerCase());
                        if(!Biomizer.INSTANCE.getNMS().getParticleTypes().contains(k)) {
                            sender.sendMessage(Strings.NOT_A_VALID_PARTICLE);
                            return true;
                        }
                        t.addMetaData(new StringDataField("biome.particle", k.toString()));
                        impl.update(t);
                        sender.sendMessage(Strings.SUCCESS);
                    }
                    break;
                }
                default: {
                    //TODO
                    break;
                }
            }
            sender.sendMessage(Strings.RELOG_FOR_CHANGES);
        } catch (ArrayIndexOutOfBoundsException e) {
            sender.sendMessage(Strings.NOT_ENOUGH_ARGUMENTS);
        } catch (NumberFormatException e) {
            sender.sendMessage(Strings.NOT_A_VALID_NUMBER);
        } catch (NotRegisteredException e) {
            sender.sendMessage(Strings.NOT_IN_A_TOWN);
        } catch (ClassCastException e) {
            sender.sendMessage(Strings.PLAYER_ONLY);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            return options;
        } else {
            if(args[0].equalsIgnoreCase("particle")) {
                return Biomizer.INSTANCE.getNMS().getParticleTypes().stream()
                        .map(NamespacedKey::getKey)
                        .collect(Collectors.toList());
            }
//            if(args[0].toLowerCase().endsWith("color")) {
//                if (args.length == 2) {
//                    return Collections.singletonList("red");
//                } else if (args.length == 3) {
//                    return Collections.singletonList("green");
//                } else if (args.length == 4) {
//                    return Collections.singletonList("blue");
//                }
//            } else if(args[0].equalsIgnoreCase("precipitation")) {
//                return precip;
//            } else if(args[0].equalsIgnoreCase("particle")) {
//                return Biomizer.INSTANCE.getNMS().getParticleTypes().stream()
//                        .map(NamespacedKey::getKey)
//                        .collect(Collectors.toList());
//            }
        }
        return null;
    }
}
