/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.towny;

import com.palmergames.bukkit.util.ChatTools;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Palette {
    private static final BufferedImage image;
    private static final Map<String, List<BaseComponent[]>> cache = new HashMap<>();
    static {
        Color[] colors = new Color[] { Color.RED, Color.MAGENTA, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED };
        image = new BufferedImage(1600, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        float[] positions = new float[colors.length];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = (i) / (colors.length - 1f);
        }
        LinearGradientPaint linearGradient = new LinearGradientPaint(0, 0, image.getWidth(), 0, positions, colors);
        g.setPaint(linearGradient);
        g.fillRect(0, 0, 1600, 500);
        BufferedImage image2 = new BufferedImage(1600, 500, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = image2.createGraphics();
        g2.setPaint(new LinearGradientPaint(0, 0, 0, image2.getHeight(), new float[] { 0f, 0.5f, 1f }, new Color[] { new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 0f), new Color(1f, 1f, 1f, 1f) }));
        g2.fillRect(0, 0, 1600, 500);
        g2.dispose();
        g.drawImage(image2, 0, 0, null);
        String[] strs = new String[]{"grasscolor", "skycolor", "foliagecolor", "watercolor", "fogcolor", "waterfogcolor"};
        String[] strs2 = new String[]{"Grass Color", "Sky Color", "Foliage Color", "Water Color", "Fog Color", "Water Fog Color"};
        for (int i = 0; i < strs.length; i++) {
            String type = strs[i];
            List<BaseComponent[]> components = new ArrayList<>();
            components.add(new ComponentBuilder(ChatTools.formatTitle(strs2[i])).create());
            for (int j = 1; j < 14; j++) {
                ComponentBuilder message = new ComponentBuilder();
                for (int f = 0; f < 160; f++) {
                    Color c = new Color(Palette.image.getRGB(f * 10, j * 33));
                    ChatColor cc = ChatColor.of(c);
                    ComponentBuilder part = new ComponentBuilder("|")
                            .color(cc)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town biome " + type + " " + c.getRed() + " " + c.getGreen() + " " + c.getBlue()))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to change!").color(cc).create()));
                    message.append(part.create());
                }
                components.add(message.create());
            }
            cache.put(type, components);
        }
    }
    public static void send(Player player, String type) {
        cache.get(type).forEach(player.spigot()::sendMessage);
    }
}
