package com.articreep.pocketknife.features.fireworkqueue;

import com.articreep.pocketknife.Pocketknife;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CustomQueueItems {
    public static NamespacedKey damageKey = new NamespacedKey(Pocketknife.getInstance(), "queue_item_damage");
    public static NamespacedKey rangeKey = new NamespacedKey(Pocketknife.getInstance(), "queue_item_range");
    public static NamespacedKey quickChargeKey = new NamespacedKey(Pocketknife.getInstance(), "queue_item_quickcharge");
    public static NamespacedKey multiShotKey = new NamespacedKey(Pocketknife.getInstance(), "queue_item_multishot");

    public static ItemStack shortRangeFirework(int count) {
        return createFirework(count, ChatColor.BLUE + "Short-range Firework", 7, 3, 5, false, Color.BLUE);
    }

    public static ItemStack longRangeFirework(int count) {
        return createFirework(count, ChatColor.RED + "Long-range Firework", 15, 13, 3, false, Color.RED);
    }

    public static ItemStack mediumRangeMultiShot(int count) {
        return createFirework(count, ChatColor.GREEN + "Medium-range Firework", 10, 7, 4, true, Color.GREEN);
    }

    public static ItemStack explosiveArrow(int count) {
        ItemStack arrow = new ItemStack(Material.ARROW, count);
        arrow.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta meta = arrow.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Explosive Arrow");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(damageKey, PersistentDataType.DOUBLE, 20d);
        arrow.setItemMeta(meta);
        return arrow;
    }
    public static ItemStack createFirework(int count, String name, double damage, int range,
                                           int quickCharge, boolean multishot, Color color) {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET, count);
        FireworkMeta meta = (FireworkMeta) firework.getItemMeta();
        meta.setDisplayName(name);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(damageKey, PersistentDataType.DOUBLE, damage);
        container.set(rangeKey, PersistentDataType.INTEGER, range);
        container.set(quickChargeKey, PersistentDataType.INTEGER, quickCharge);
        if (multishot) container.set(multiShotKey, PersistentDataType.BOOLEAN, true);
        meta.addEffect(FireworkEffect.builder().withColor(color).build());
        firework.setItemMeta(meta);
        return firework;
    }
}
