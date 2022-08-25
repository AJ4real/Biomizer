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
import com.palmergames.bukkit.util.ChatTools;
import me.aj4real.biomizer.Biomizer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
    private static final BaseComponent[] menu;
    private static final BaseComponent[] precipMenu;
    private static final List<String> options;
    private static final List<String> precip;
    private static final BiPredicate<Integer, CommandSender> check;
    static {
        ComponentBuilder cb = new ComponentBuilder(ChatTools.formatTitle("Biome"))
                .append("\n  ");
        cb.append(new ComponentBuilder("[X]")
                .color(ChatColor.RED)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome grasscolor none"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to reset grass color.")))
                .create()).append(" ");
        cb.append(new ComponentBuilder("[GrassColor]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome grasscolor"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change grass color.")))
                .create()).append("\n  ");
        cb.append(new ComponentBuilder("[X]")
                .color(ChatColor.RED)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome skycolor none"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to reset sky color.")))
                .create()).append(" ");
        cb.append(new ComponentBuilder("[SkyColor]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome skycolor"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change sky color.")))
                .create()).append("\n  ");
        cb.append(new ComponentBuilder("[X]")
                .color(ChatColor.RED)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome foliagecolor none"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to reset foliage color.")))
                .create()).append(" ");
        cb.append(new ComponentBuilder("[FoliageColor]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome foliagecolor"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change foliage color.")))
                .create()).append("\n  ");
        cb.append(new ComponentBuilder("[X]")
                .color(ChatColor.RED)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome watercolor none"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to reset water color.")))
                .create()).append(" ");
        cb.append(new ComponentBuilder("[WaterColor]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome watercolor"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change water color.")))
                .create()).append("\n  ");
        cb.append(new ComponentBuilder("[X]")
                .color(ChatColor.RED)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome fogcolor none"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to reset fog color.")))
                .create()).append(" ");
        cb.append(new ComponentBuilder("[FogColor]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome fogcolor"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change fog color.")))
                .create()).append("\n  ");
        cb.append(new ComponentBuilder("[X]")
                .color(ChatColor.RED)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome waterfogcolor none"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to reset water fog color.")))
                .create()).append(" ");
        cb.append(new ComponentBuilder("[WaterFogColor]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome waterfogcolor"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change water fog color.")))
                .create()).append("\n  ");
        cb.append(new ComponentBuilder("[Precipitation]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome precipitation"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to change natural precipitation type.")))
                .create());
        menu = cb.create();
        ComponentBuilder cb2 = new ComponentBuilder(ChatTools.formatTitle("Precipitation"))
                .append("\n                 ");
        cb2.append(new ComponentBuilder("[None]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome precipitation none"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("No precipitation will occur in your town.")))
                .create()).append("     ");
        cb2.append(new ComponentBuilder("[Rain]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome precipitation rain"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Rain can occur in your town.")))
                .create()).append("     ");
        cb2.append(new ComponentBuilder("[Snow]")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome precipitation snow"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Snow can occur in your town.")))
                .create()).append(" ");
        precipMenu = cb2.create();
        check = (i, s) -> {
            if(i > 255) {
                s.sendMessage(Strings.VALUE_ABOVE_255);
                return true;
            } else if(i < 0) {
                s.sendMessage(Strings.VALUE_BELOW_0);
                return true;
            }
            return false;
        };
        options = Collections.unmodifiableList(
                Arrays.asList("grasscolor", "skycolor", "foliagecolor", "watercolor",
                        "fogcolor", "waterfogcolor", "precipitation", "particle"));
        precip = Collections.unmodifiableList(Arrays.asList("snow", "rain", "none"));
    }
    private final TownyImpl impl;
    public BiomeCommand(TownyImpl impl) {
        this.impl = impl;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            Resident res = TownyAPI.getInstance().getResident((Player) sender);
            Town t = res.getTown();
            if(!res.hasPermissionNode("towny.biome")) {
                sender.sendMessage(Strings.NO_PERMISSION);
                return true;
            }
            if(args.length == 0) {
                sender.spigot().sendMessage(menu);
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
                        if(args[1].equalsIgnoreCase("none")) {
                            t.removeMetaData(new IntegerDataField("biome." + args[0]));
                            impl.update(t);
                            sender.sendMessage(Strings.SUCCESS);
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
                    }
                    break;
                }
                case "precipitation": {
                    if(args.length == 1) {
                        sender.spigot().sendMessage(precipMenu);
                        return true;
                    } else {
                        String type = args[1].toLowerCase();
                        if(!precip.contains(type)) {
                            sender.sendMessage(Strings.NOT_A_VALID_PRECIPITATION);
                            return true;
                        }
                        t.addMetaData(new StringDataField("biome." + args[0], type));
                        impl.update(t);
                        sender.sendMessage(Strings.SUCCESS);
                    }
                    break;
                }
                case "particle": {
                    if(args.length == 1) {
                    } else {
                        if(!sender.hasPermission("biomizer.admin") && !sender.isOp()) {
                            sender.sendMessage(Strings.PARTICLE_NO_PERMISSION);
                            return true;
                        }
                        if(args[1].equalsIgnoreCase("none")) {
                            t.removeMetaData(new StringDataField("biome." + args[0]));
                            impl.update(t);
                            sender.sendMessage(Strings.SUCCESS);
                        } else {
                            NamespacedKey k = NamespacedKey.minecraft(args[1].toLowerCase());
                            if(!Biomizer.INSTANCE.getNMS().getParticleTypes().contains(k)) {
                                sender.sendMessage(Strings.NOT_A_VALID_PARTICLE);
                                return true;
                            }
                            t.addMetaData(new StringDataField("biome." + args[0], k.toString()));
                            impl.update(t);
                            sender.sendMessage(Strings.SUCCESS);
                        }
                    }
                    break;
                }
                default: {
                    //TODO
                    return true;
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
        if(!sender.hasPermission("biomizer.admin") && !sender.isOp()) return null;
        if(args.length == 1)
            return options;
        else {
            if(args[0].equalsIgnoreCase("particle"))
                return Biomizer.INSTANCE.getNMS().getParticleTypes().stream()
                        .map(NamespacedKey::getKey)
                        .collect(Collectors.toList());
            if(args[0].toLowerCase().endsWith("color"))
                if (args.length == 2) return Collections.singletonList("red");
                else if (args.length == 3) return Collections.singletonList("green");
                else if (args.length == 4) return Collections.singletonList("blue");
            else if(args[0].equalsIgnoreCase("precipitation")) return precip;
            else if(args[0].equalsIgnoreCase("particle"))
                return Biomizer.INSTANCE.getNMS().getParticleTypes().stream()
                        .map(NamespacedKey::getKey)
                        .collect(Collectors.toList());
        }
        return null;
    }
}
