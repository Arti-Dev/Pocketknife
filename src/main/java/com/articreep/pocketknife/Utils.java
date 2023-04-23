package com.articreep.pocketknife;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

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

        World w = start.getWorld();
        Location startLoc = start.getLocation();
        Location endLoc = end.getLocation();

        // Generate vector, player - tnt = vector
        Vector vector = endLoc.toVector().subtract(startLoc.toVector());
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

        World w = start.getWorld();

        // Generate vector, player - tnt = vector
        Vector vector = end.toVector().subtract(start.toVector());
        vector.normalize();
        vector.multiply(factor);

        return vector;
    }

}
