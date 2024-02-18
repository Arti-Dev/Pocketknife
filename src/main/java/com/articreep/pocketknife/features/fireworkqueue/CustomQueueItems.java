package com.articreep.pocketknife.features.fireworkqueue;

import com.articreep.pocketknife.Pocketknife;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CustomQueueItems {
    public static NamespacedKey damageKey = new NamespacedKey(Pocketknife.getInstance(), "queue_item_damage");
    public static NamespacedKey rangeKey = new NamespacedKey(Pocketknife.getInstance(), "queue_item_range");
    public static NamespacedKey quickChargeKey = new NamespacedKey(Pocketknife.getInstance(), "queue_item_quick_charge");

    public static ItemStack shortRangeFirework(int count) {
        return createFirework(count, ChatColor.BLUE + "Short-range Firework", 7, 3, 5, Color.BLUE);
    }

    public static ItemStack longRangeFirework(int count) {
        return createFirework(count, ChatColor.RED + "Long-range Firework", 15, 13, 2, Color.RED);
    }
    public static ItemStack createFirework(int count, String name, double damage, int range, int quickCharge, Color color) {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET, count);
        FireworkMeta meta = (FireworkMeta) firework.getItemMeta();
        meta.setDisplayName(name);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(damageKey, PersistentDataType.DOUBLE, damage);
        container.set(rangeKey, PersistentDataType.INTEGER, range);
        container.set(quickChargeKey, PersistentDataType.INTEGER, quickCharge);
        meta.addEffect(FireworkEffect.builder().withColor(color).build());
        firework.setItemMeta(meta);
        return firework;
    }
}
