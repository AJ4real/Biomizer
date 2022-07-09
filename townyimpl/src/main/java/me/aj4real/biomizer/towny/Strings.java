/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.towny;

import org.bukkit.ChatColor;

public class Strings {
    private static final ChatColor COLOR_SUCCESS = ChatColor.AQUA;
    private static final ChatColor COLOR_WARNING = ChatColor.GOLD;
    private static final ChatColor COLOR_FAIL = ChatColor.RED;
    public static String SUCCESS = COLOR_SUCCESS + "Success!";
    public static String RELOG_FOR_CHANGES = COLOR_WARNING + "To view any visual changes, you are required to reconnect to the server.";
    public static String PLAYER_ONLY = COLOR_FAIL + "Only players can execute this command.";
    public static String NO_PERMISSION = COLOR_FAIL + "You do not have permission to change biome properties.";
    public static String NOT_ENOUGH_ARGUMENTS = COLOR_FAIL + "Not enough arguments.";
    public static String NOT_A_VALID_NUMBER = COLOR_FAIL + "That is not a valid number.";
    public static String NOT_A_VALID_OPTION = COLOR_FAIL + "Not a valid option.";
    public static String NOT_IN_A_TOWN = COLOR_FAIL + "You must be in a town to use this function.";
    public static String NOT_A_VALID_PRECIPITATION = COLOR_FAIL + "Invalid precipitation type.";
    public static String NOT_A_VALID_PARTICLE = COLOR_FAIL + "Invalid particle type.";
    public static String PARTICLE_BLACKLISTED = COLOR_FAIL + "This particle effect cannot be applied to your town.";
    public static String PARTICLE_NO_PERMISSION = COLOR_FAIL + "You do not have permission to apply particles to towns.";
    public static String VALUE_ABOVE_255 = COLOR_FAIL + "Value cannot be above 255.";
    public static String VALUE_BELOW_0 = COLOR_FAIL + "Value cannot be below 0.";
    public static String PLATE = "§6.oOo.____________.[ §e%s§6 ].____________.oOo.";
}