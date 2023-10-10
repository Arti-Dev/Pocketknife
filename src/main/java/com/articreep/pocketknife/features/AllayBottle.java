package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeFeature;
import com.articreep.pocketknife.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
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
                player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_GIVEN, 1, 1);
                try {
                    if (handItem.getAmount() == 1) {
                        inv.setItem(event.getHand(), captureAllay(allay));
                    } else {
                        handItem.setAmount(handItem.getAmount() - 1);
                        inv.addItem(captureAllay(allay));
                    }
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
        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item == null || item.getItemMeta() == null) return;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(healthKey, PersistentDataType.DOUBLE)) {
            try {
                releaseAllay(item, event.getClickedBlock(), event.getBlockFace());
                player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_GIVEN, 1, 1);
                player.getInventory().setItem(event.getHand(), new ItemStack(Material.GLASS_BOTTLE));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    @EventHandler
    public void onAllayDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item == null || item.getItemMeta() == null) return;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(itemKey, PersistentDataType.BYTE_ARRAY)) {
            ItemStack heldItem = new ItemStack(Material.AIR);
            try {
                heldItem = retrieveItem(container.get(itemKey, PersistentDataType.BYTE_ARRAY));
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
            ItemStack finalHeldItem = heldItem;

            player.getWorld().spawn(player.getLocation(), Item.class, droppedItem -> {
                droppedItem.setPickupDelay(20);
                droppedItem.setItemStack(finalHeldItem);
                droppedItem.setVelocity(player.getLocation().getDirection().multiply(0.5));
            });
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
        }

    }

    private static NamespacedKey itemKey = new NamespacedKey(Pocketknife.getInstance(), "allayItem");
    private static NamespacedKey nameKey = new NamespacedKey(Pocketknife.getInstance(), "allayName");
    private static NamespacedKey dupeKey = new NamespacedKey(Pocketknife.getInstance(), "allayDupeCooldown");
    private static NamespacedKey healthKey = new NamespacedKey(Pocketknife.getInstance(), "allayHealthKey");

    private ItemStack captureAllay(Allay allay) throws IOException {
        // Obtain data about the Allay
        ItemStack itemCarrying = allay.getEquipment().getItemInMainHand();
        String itemDisplayName = getName(itemCarrying);
        String allayName = allay.getCustomName();
        if (allayName == null) allayName = "";
        long dupeCooldown = allay.getDuplicationCooldown();
        double health = allay.getHealth();

        ItemStack bottle = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) bottle.getItemMeta();
        meta.setColor(Color.AQUA);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION, 30*20, 0), true);
        meta.setDisplayName(ChatColor.AQUA + "Bottle of Allay");

        List<String> lore = new ArrayList<>();
        if (!allayName.isEmpty()) lore.add(ChatColor.GRAY + "Name: " + allayName);
        lore.add(ChatColor.GRAY + "Item Carrying: " + itemDisplayName);
        meta.setLore(lore);

        // Persistent data containers
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(nameKey, PersistentDataType.STRING, allayName);
        container.set(dupeKey, PersistentDataType.LONG, dupeCooldown);
        container.set(healthKey, PersistentDataType.DOUBLE, health);
        container.set(itemKey, PersistentDataType.BYTE_ARRAY, encodeItem(itemCarrying));

        bottle.setItemMeta(meta);
        allay.remove();
        return bottle;
    }

    private void releaseAllay(ItemStack item, Block block, BlockFace face) throws IOException, ClassNotFoundException {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String name = container.get(nameKey, PersistentDataType.STRING);
        Long dupeCooldown = container.get(dupeKey, PersistentDataType.LONG);
        Double health = container.get(healthKey, PersistentDataType.DOUBLE);

        byte[] itemBytes = container.get(itemKey, PersistentDataType.BYTE_ARRAY);
        ItemStack heldItem = retrieveItem(itemBytes);

        block.getWorld().spawn(block.getLocation(), Allay.class, allay -> {
            Utils.alignToFace(block, face, allay);
            allay.setHealth(health);
            allay.setCustomName(name);
            allay.setDuplicationCooldown(dupeCooldown);
            allay.getEquipment().setItemInMainHand(heldItem);
        });

    }

    private static String getName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        } else if (item.getType() != Material.AIR) {
            return item.getType().toString();
        } else return ChatColor.DARK_GRAY + "none";
    }

    private static byte[] encodeItem(ItemStack item) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(out);
        bukkitStream.writeObject(item);
        return out.toByteArray();
    }

    private static ItemStack retrieveItem(byte[] itemBytes) throws IOException, ClassNotFoundException {
        if (itemBytes == null) throw new IllegalStateException("bruh itembytes is null");
        ByteArrayInputStream in = new ByteArrayInputStream(itemBytes);
        BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(in);
        return (ItemStack) bukkitStream.readObject();
    }



}
