package com.articreep.pocketknife.features.fireworkqueue;

import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import com.articreep.pocketknife.features.combo.Combo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FireworkQueue extends PocketknifeSubcommand implements Listener {
    // todo clarify when to use ItemQueue.pop() and not removeFromQueue
    // todo this code is too long smh
    HashMap<UUID, ItemQueue> enabledPlayers = new HashMap<>();

    @EventHandler
    public void onFireworkExplode(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Firework firework)) return;
        if (!(firework.getShooter() instanceof Player player)) return;
        Combo.getComboCounter(player).incrementCombo();

        // disable players being able to hit themselves with their own fireworks
        if (event.getEntity().equals(player)) event.setCancelled(true);

        if (firework.getPersistentDataContainer().has(CustomQueueItems.damageKey)) {
            PersistentDataContainer container = firework.getPersistentDataContainer();
            double damage = container.get(CustomQueueItems.damageKey, PersistentDataType.DOUBLE);
            event.setDamage(damage);
        }
    }

    @EventHandler
    public void onFireworkFire(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Firework firework)) return;
        if (!(firework.getShooter() instanceof Player player)) return;
        if (enabledPlayers.containsKey(player.getUniqueId())) {
            // Apply properties of the firework fired
            ItemQueue queue = enabledPlayers.get(player.getUniqueId());
            ItemStack item = queue.getActiveItem();
            if (!item.hasItemMeta()) return;

            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            int range = container.getOrDefault(CustomQueueItems.rangeKey, PersistentDataType.INTEGER, 20);
            double damage = container.getOrDefault(CustomQueueItems.damageKey, PersistentDataType.DOUBLE, 7d);

            firework.setMaxLife(range);
            firework.getPersistentDataContainer().set(CustomQueueItems.damageKey, PersistentDataType.DOUBLE, damage);

            // Check to see if firework rocket count has run out
            if (player.getInventory().getItemInOffHand().getType() == Material.AIR) {
                removeFromQueue(player);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Enables \"queue\" mode where you can only use items in the order that they arrive.";
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        // if the player's inventory isn't empty and they're not enabled
        if (!Utils.inventoryEmptyIgnoreArmor(player.getInventory()) && !enabledPlayers.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "Your inventory needs to be empty in order to turn this on!");
        } else if (enabledPlayers.containsKey(uuid)) {
            player.sendMessage("Queue disabled");
            enabledPlayers.remove(uuid);
            player.getInventory().clear();
        } else {
            player.sendMessage("Queue enabled");
            ItemQueue queue = new ItemQueue();
            queue.add(CustomQueueItems.longRangeFirework(3));
            queue.add(CustomQueueItems.shortRangeFirework(10));
            queue.add(CustomQueueItems.longRangeFirework(3));
            queue.add(CustomQueueItems.shortRangeFirework(10));
            queue.add(CustomQueueItems.longRangeFirework(3));
            queue.add(CustomQueueItems.shortRangeFirework(3));
            enabledPlayers.put(uuid, queue);
            updateInventory(player);
        }
        return true;
    }

    /** Updates the player's inventory to match the queue state **/
    private void updateInventory(Player player) {
        UUID uuid = player.getUniqueId();
        if (!enabledPlayers.containsKey(uuid)) return;

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        ItemQueue queue = enabledPlayers.get(uuid);
        ItemStack activeItem = queue.getActiveItem();
        ItemStack holdItem = queue.getHoldItem();
        ArrayList<ItemStack> visibleQueue = queue.getVisibleQueue();

        if (activeItem.getType() == Material.FIREWORK_ROCKET && activeItem.hasItemMeta()) {
            ItemStack crossbow = new ItemStack(Material.CROSSBOW);
            PersistentDataContainer container = activeItem.getItemMeta().getPersistentDataContainer();
            if (container.has(CustomQueueItems.quickChargeKey)) {
                crossbow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, container.get(CustomQueueItems.quickChargeKey, PersistentDataType.INTEGER));
            }
            inventory.setItem(0, crossbow);
            inventory.setItemInOffHand(activeItem);
        } else inventory.setItem(0, activeItem);

        for (int i = 1; i < visibleQueue.size()+1; i++) {
            inventory.setItem(i, visibleQueue.get(i-1));
        }
        inventory.setItem(8, holdItem);
    }

    private void addToQueue(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        if (enabledPlayers.containsKey(uuid)) {
            ItemQueue queue = enabledPlayers.get(uuid);

            // save last resort durability
            if (queue.isLastResortActive()) {
                ItemStack lastResort = player.getInventory().getItem(0);
                queue.setLastResortItem(lastResort);
            }

            enabledPlayers.get(player.getUniqueId()).add(item);
            updateInventory(player);
        }
    }

    private void removeFromQueue(Player player) {
        UUID uuid = player.getUniqueId();
        if (enabledPlayers.containsKey(uuid)) {
            enabledPlayers.get(uuid).pop();
            updateInventory(player);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        removeFromQueue(event.getPlayer());
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID uuid = player.getUniqueId();
            if (enabledPlayers.containsKey(uuid)) {
                event.setCancelled(true);
                event.getItem().remove();
                player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1, 1);
                addToQueue(player, event.getItem().getItemStack());
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }
}
