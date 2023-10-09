package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeFeature;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.Map;

public class AllayBottle implements PocketknifeFeature, Listener {
    @Override
    public String getDescription() {
        return "Allows you to place allays in a bottle";

        /*
        - Allays can be shift-right clicked to trap them into a glass bottle and called "Bottle of Allay"
        - The item they are carrying will stay in the bottle
        - Their name will be carried over, and their duplication delay, and their health
        - Effects do not carry over (for now)
        - The Allay can be released by right-clicking a block
        - You can drink the Bottle of Allay which will cause you to levitate for 30 seconds
            - The item that the Allay was carrying, if any, will drop (as if you burped it)
         */
    }

    @EventHandler
    public void onAllayRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Allay allay) {
            Player player = event.getPlayer();
            PlayerInventory inv = player.getInventory();
            if (inv.getItemInMainHand().getType() == Material.GLASS_BOTTLE) {
                event.setCancelled(true);
                captureAllay(allay);
            } else if (inv.getItemInOffHand().getType() == Material.GLASS_BOTTLE) {
                event.setCancelled(true);
                captureAllay(allay);
            }
        }
    }

    private static NamespacedKey itemKey = new NamespacedKey(Pocketknife.getInstance(), "allayItem");
    private static NamespacedKey nameKey = new NamespacedKey(Pocketknife.getInstance(), "allayName");
    private static NamespacedKey dupeKey = new NamespacedKey(Pocketknife.getInstance(), "allayDupeCooldown");
    private static NamespacedKey healthKey = new NamespacedKey(Pocketknife.getInstance(), "allayHealthKey");

    private ItemStack captureAllay(Allay allay) {
        // Obtain data about the Allay
        ItemStack itemCarrying = allay.getInventory().getItem(0);
        if (itemCarrying == null) itemCarrying = new ItemStack(Material.AIR);
        String name = allay.getCustomName();
        if (name == null) name = "";
        long dupeCooldown = allay.getDuplicationCooldown();
        double health = allay.getHealth();

        ItemStack bottle = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) bottle.getItemMeta();
        meta.setColor(Color.AQUA);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION, 30*20, 0), true);

        // Persistent data containers
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(nameKey, PersistentDataType.STRING, name);
        container.set(dupeKey, PersistentDataType.LONG, dupeCooldown);
        container.set(healthKey, PersistentDataType.DOUBLE, health);
        // todo figure out how to put an itemstack into a persistent data container
        bottle.setItemMeta(meta);
        return bottle;
    }



}
