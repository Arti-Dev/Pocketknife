package com.articreep.pocketknife;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    /**
     * Returns whether the provided ItemStack is a piece of diamond armor.
     * @param item ItemStack to check
     * @return true if diamond armor - false otherwise
     */
    public static boolean isDiamondArmor(@Nullable ItemStack item) {
        if (item == null) return false;
        Material material = item.getType();
        return material == Material.DIAMOND_BOOTS ||
                material == Material.DIAMOND_LEGGINGS ||
                material == Material.DIAMOND_CHESTPLATE ||
                material == Material.DIAMOND_HELMET;
    }

    /**
     * Takes two entities and converts their locations to vectors and creates a resultant.
     * The vector is then normalized to length 1 and multiplied by the provided factor.
     * @param start Starting entity, vector points away from this entity
     * @param end Ending entity, vector points towards this entity
     * @param factor Magnitude of the final vector
     * @return The final vector
     */
    public static Vector entitiesToNormalizedVector(Entity start, Entity end, double factor) {
        if (start.getWorld() != end.getWorld()) throw new IllegalArgumentException("Entities' worlds do not match!");

        Location startLoc = start.getLocation();
        Location endLoc = end.getLocation();

        // Generate vector, player - tnt = vector
        Vector vector = endLoc.toVector().subtract(startLoc.toVector());
        vector.normalize();
        vector.multiply(factor);

        return vector;
    }

    /**
     * Takes two entities and converts their locations to vectors and creates a resultant.
     * The resultant is then stripped of its y-value.
     * The vector is then normalized to length 1 and multiplied by the provided factor.
     * @param start Starting entity, vector points away from this entity
     * @param end Ending entity, vector points towards this entity
     * @param factor Magnitude of the final vector
     * @return The final vector
     */
    public static Vector entitiesToHorizontalNormalizedVector(Entity start, Entity end, double factor) {
        if (start.getWorld() != end.getWorld()) throw new IllegalArgumentException("Entities' worlds do not match!");

        Location startLoc = start.getLocation();
        Location endLoc = end.getLocation();

        // Generate vector, player - tnt = vector
        Vector vector = endLoc.toVector().subtract(startLoc.toVector());
        vector.setY(0);
        vector.normalize();
        vector.multiply(factor);

        return vector;
    }

    /**
     * Takes two locations, converts them to vectors, and creates a resultant.
     * The vector is then normalized to length 1 and multiplied by the provided factor.
     * @param start Starting location, vector points away from here
     * @param end Ending location, vector points towards here
     * @param factor Magnitude of the final vector
     * @return The final vector
     */
    public static Vector locationsToNormalizedVector(Location start, Location end, double factor) {
        if (start.getWorld() != end.getWorld()) throw new IllegalArgumentException("Locations' worlds do not match!");

        // Generate vector, player - tnt = vector
        Vector vector = end.toVector().subtract(start.toVector());
        vector.normalize();
        vector.multiply(factor);

        return vector;
    }

    /**
     * Gets a runCommand method object from the specified class name. Make sure hasCommandMethod is checked!
     * @param className class name
     * @param classLoader class loader from Bukkit
     * @return The method, null if not found
     */
    public static Method getCommandMethod(String className, ClassLoader classLoader) {
        Class<?> targetClass;
        try {
            targetClass = Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Method m;
        try {
            m = targetClass.getMethod("runCommand", CommandSender.class, Command.class, String.class, String[].class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }

        return m;

    }

    /**
     * Gets a runCommand method object from the specified class. Make sure hasCommandMethod is checked!
     * @param targetClass class to check
     * @return method object, null if not found
     */
    public static Method getCommandMethod(Class<?> targetClass) {
        Method m;
        try {
            m = targetClass.getMethod("runCommand", CommandSender.class, Command.class, String.class, String[].class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        return m;
    }

    /**
     * Note to self: This does not work if the method is protected!!
     * Takes a class object and checks to see if there is a static runCommand(CommandSender sender, Command command, String label, String[] args) method.
     * @param targetClass Any class
     * @return Whether it contains the method
     */
    public static boolean hasCommandMethod(Class<?> targetClass) {
        try {
            targetClass.getMethod("runCommand", CommandSender.class, Command.class, String.class, String[].class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Removes the first argument from an argument array.
     * @param args arguments
     * @return Arguments array with the first argument removed
     */
    public static String[] removeFirstArg(String[] args) {
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        // TODO I don't know what I'm doing here
        String[] strings = new String[0];
        return argsList.toArray(strings);
    }

    /**
     * Takes a list of type String and a string.
     * Checks if the string is in the list, regardless of case.
     * @param list List of strings
     * @param check Element to check against List
     * @return Whether the element is in the list, regardless of case
     */
    public static boolean containsIgnoreCase(List<String> list, String check) {
        for (String str : list) {
            if (str.equalsIgnoreCase(check)) return true;
        }
        return false;
    }

    /**
     * Removes all matches from the first argument list that are in the second list.
     * @param list First list (items will be removed from this list)
     * @param remove Second list (items will be unchanged)
     */
    public static void removeAllIgnoreCase(List<String> list, List<String> remove) {
        for (String str : remove) {
            if (containsIgnoreCase(list, str)) list.remove(str);
        }
    }

    public static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
