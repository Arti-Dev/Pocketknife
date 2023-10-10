package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeFeature;
import org.bukkit.*;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class AllayBottle implements PocketknifeFeature, Listener {
    private static final Set<Player> cooldowns = new HashSet<>();
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
        Player player = event.getPlayer();
        cooldowns.add(player);
        Bukkit.getScheduler().runTask(Pocketknife.getInstance(), () -> cooldowns.remove(player));
        if (event.getRightClicked() instanceof Allay allay) {
            PlayerInventory inv = player.getInventory();
            ItemStack handItem = inv.getItem(event.getHand());
            if (handItem != null && handItem.getType() == Material.GLASS_BOTTLE) {
                event.setCancelled(true);
                try {
                    handItem.setAmount(handItem.getAmount()-1);
                    inv.addItem(captureAllay(allay));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void onAllayRelease(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (cooldowns.remove(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Location location = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item == null || item.getItemMeta() == null) return;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(healthKey, PersistentDataType.DOUBLE)) {
            try {
                releaseAllay(item, location);
                player.getInventory().setItem(event.getHand(), new ItemStack(Material.GLASS_BOTTLE));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private static NamespacedKey itemKey = new NamespacedKey(Pocketknife.getInstance(), "allayItem");
    private static NamespacedKey nameKey = new NamespacedKey(Pocketknife.getInstance(), "allayName");
    private static NamespacedKey dupeKey = new NamespacedKey(Pocketknife.getInstance(), "allayDupeCooldown");
    private static NamespacedKey healthKey = new NamespacedKey(Pocketknife.getInstance(), "allayHealthKey");

    private ItemStack captureAllay(Allay allay) throws IOException {
        // Obtain data about the Allay
        ItemStack itemCarrying = allay.getEquipment().getItemInMainHand();
        String name = allay.getCustomName();
        if (name == null) name = "";
        long dupeCooldown = allay.getDuplicationCooldown();
        double health = allay.getHealth();

        ItemStack bottle = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) bottle.getItemMeta();
        meta.setColor(Color.AQUA);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION, 30*20, 0), true);
        meta.setDisplayName(ChatColor.AQUA + "Bottle of Allay");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Name: " + name,
                ChatColor.GRAY + "Item Carrying:" + itemCarrying.getType()));

        // Persistent data containers
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(nameKey, PersistentDataType.STRING, name);
        container.set(dupeKey, PersistentDataType.LONG, dupeCooldown);
        container.set(healthKey, PersistentDataType.DOUBLE, health);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(out);
        bukkitStream.writeObject(itemCarrying);
        container.set(itemKey, PersistentDataType.BYTE_ARRAY, out.toByteArray());

        bottle.setItemMeta(meta);
        allay.remove();
        return bottle;
    }

    private void releaseAllay(ItemStack item, Location location) throws IOException, ClassNotFoundException {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String name = container.get(nameKey, PersistentDataType.STRING);
        Long dupeCooldown = container.get(dupeKey, PersistentDataType.LONG);
        Double health = container.get(healthKey, PersistentDataType.DOUBLE);
        byte[] itemBytes = container.get(itemKey, PersistentDataType.BYTE_ARRAY);

        if (itemBytes == null) throw new IllegalStateException("how is this null");
        ByteArrayInputStream in = new ByteArrayInputStream(itemBytes);
        BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(in);
        ItemStack heldItem = (ItemStack) bukkitStream.readObject();

        location.getWorld().spawn(location, Allay.class, allay -> {
            allay.setHealth(health);
            allay.setCustomName(name);
            allay.setDuplicationCooldown(dupeCooldown);
            allay.getEquipment().setItemInMainHand(heldItem);
        });

    }



}
