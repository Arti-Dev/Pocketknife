package com.articreep.pocketknife;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class DiamondHit implements Listener {

    // WeakHashMaps automatically remove garbage-collected members
    private static final Set<Item> droppedXPSet = Collections.newSetFromMap(new WeakHashMap<>());

    @EventHandler
    public void onDiamondHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager && event.getEntity() instanceof Player victim)) return;

        // check if victim has diamond armor equipped

        ItemStack[] items = victim.getEquipment().getArmorContents();

        boolean hasDiamondArmor = false;
        for (ItemStack item : items) {
            if (Utils.isDiamondArmor(item)) {
                hasDiamondArmor = true;
                break;
            }
        }

        // todo add a cooldown
        if (hasDiamondArmor) dropXP(damager, victim);
    }
    private void dropXP(Player damager, Player victim) {
        Vector v = Utils.entitiesToNormalizedVector(damager, victim, 0.5);
        v.setY(0.5);
        Item bottle = victim.getWorld().dropItem(victim.getLocation().add(0, 1, 0), new ItemStack(Material.EXPERIENCE_BOTTLE));
        damager.getWorld().playSound(damager, Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 1, 1);
        bottle.setVelocity(v);
        droppedXPSet.add(bottle);
    }

    @EventHandler
    public void onXPPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (droppedXPSet.contains(event.getItem())) {
            event.setCancelled(true);
            droppedXPSet.remove(event.getItem());
            event.getItem().remove();
            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "XP!" + ChatColor.RESET + "" + ChatColor.AQUA + " +30 XP " + ChatColor.GRAY + "from opponent armor piece");
            player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        }
    }
}
