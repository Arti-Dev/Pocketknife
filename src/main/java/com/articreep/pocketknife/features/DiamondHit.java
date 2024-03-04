package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeConfigurable;
import com.articreep.pocketknife.PocketknifeFeature;
import com.articreep.pocketknife.Utils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class DiamondHit extends PocketknifeFeature implements Listener, PocketknifeConfigurable {

    // WeakHashMaps automatically remove garbage-collected members
    private static final Set<Item> droppedXPSet = Collections.newSetFromMap(new WeakHashMap<>());
    private boolean enabled = false;
    private static final NamespacedKey cooldownKey = new NamespacedKey(Pocketknife.getInstance(), "DIAMONDHIT_COOLDOWN");

    @EventHandler
    public void onDiamondHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!enabled) return;

        if (!(event.getDamager() instanceof Player damager && event.getEntity() instanceof Player victim)) return;

        PersistentDataContainer container = victim.getPersistentDataContainer();
        // something feels off about this
        if (container.has(cooldownKey, PersistentDataType.BOOLEAN)) return;

        ItemStack[] items = victim.getEquipment().getArmorContents();

        boolean hasDiamondArmor = false;
        for (ItemStack item : items) {
            if (Utils.isDiamondArmor(item)) {
                hasDiamondArmor = true;
                break;
            }
        }

        if (hasDiamondArmor) dropXP(damager, victim);
        container.set(cooldownKey, PersistentDataType.BOOLEAN, true);

        Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> container.remove(cooldownKey), 10 * 20);
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
        if (event.isCancelled()) return;
        if (!enabled) return;

        if (!(event.getEntity() instanceof Player player)) return;
        if (droppedXPSet.contains(event.getItem())) {
            event.setCancelled(true);
            droppedXPSet.remove(event.getItem());
            event.getItem().remove();
            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "XP!" + ChatColor.RESET + ChatColor.AQUA + " +30 XP " + ChatColor.GRAY + "from opponent armor piece");
            player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        }
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        enabled = config.getBoolean("diamondhit");
        config.set("diamondhit", enabled);
    }

    @Override
    public String getDescription() {
        return ChatColor.AQUA + "Players drop XP when struck if wearing diamond armor.";
    }

    @Override
    protected void onDisable() {
        droppedXPSet.clear();
    }
}
