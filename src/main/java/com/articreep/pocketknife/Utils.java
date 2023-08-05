package com.articreep.pocketknife;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
        if (vector.lengthSquared() == 0) return vector;
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
        // IMPORTANT: make sure you do NOT normalize the zero vector.
        if (vector.lengthSquared() == 0) return vector;
        vector.normalize();
        vector.multiply(factor);
        vector.setY(0);

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

    // Is this a good way of doing this? No clue.
    public static void trueDamage(Player victim, Player damager, double amount) {
        if (victim.getHealth() - amount <= 0) {
            victim.damage(100000, damager);
        } else {
            victim.setHealth(victim.getHealth() - amount);
            victim.damage(0.0000000000000000000000000000000000001);
        }
    }

    public static void trueDamage(Player victim, double amount) {
        if (victim.getHealth() - amount <= 0) {
            victim.damage(100000);
        } else {
            victim.setHealth(victim.getHealth() - amount);
            victim.damage(0.0000000000000000000000000000000000001);
        }
    }

    public static Vector randomKB(double magnitude) {
        double x = (Math.random() * 2) - 1;
        double z = (Math.random() * 2) - 1;
        return new Vector(x, 0, z).normalize().multiply(magnitude).setY(0.4);
    }

    public static Vector randomVector(double magnitude) {
        double x = (Math.random() * 2) - 1;
        double y = (Math.random() * 2) - 1;
        double z = (Math.random() * 2) - 1;
        return new Vector(x, y, z).normalize().multiply(magnitude);
    }

    private static final NamespacedKey key = new NamespacedKey(Pocketknife.getInstance(), "ITEM_ID");
    /**
     * Adds an item ID to the PersistentDataContainer of the item.
     * @param item Item to add an ID to
     * @throws NullPointerException if item is null
     */
    public static void setItemID(ItemStack item, String id) {

        ItemMeta meta = item.getItemMeta();
        if (meta == null) throw new NullPointerException("Item has no ItemMeta");

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
    }

    /**
     * Adds an item ID to the PersistentDataContainer of the meta. Remember to apply the ItemMeta back to the ItemStack!
     * @param meta Meta to add an ID to
     * @throws NullPointerException if meta is null
     */
    public static void setItemID(ItemMeta meta, String id) {

        if (meta == null) throw new NullPointerException("Meta cannot be null");

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, id);
    }

    public static @Nullable String getItemID(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(key, PersistentDataType.STRING);
    }

    public static String booleanStatus(boolean boo) {
        if (boo) return "ON";
        else return "OFF";
    }

    public static float invertYaw(float yaw) {
        yaw += 180;
        if (yaw >= 360) {
            yaw -= 360;
        }
        return yaw;
    }

    public static float invertPitch(float pitch) {
        return -pitch;
    }

    /**
     * Rotates a vector by a given number of degrees; assume looking from a top-down view (around the Y axis)
     * Copied from <a href="https://www.spigotmc.org/threads/rotating-vector.119378/">...</a>
     * @param vector - The vector to rotate
     * @param degrees - The number of degrees to rotate by
     * @return - A rotated vector around the Y axis
     */
    public static Vector rotateVectorAroundY(Vector vector, double degrees) {
        double rad = Math.toRadians(degrees);

        double currentX = vector.getX();
        double currentZ = vector.getZ();

        double cosine = Math.cos(rad);
        double sine = Math.sin(rad);

        return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
    }
}
