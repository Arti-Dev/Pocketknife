package com.articreep.pocketknife;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;

public class Instaboom implements Listener, PocketknifeCommand {

    @EventHandler
    public void onInstaboom(BlockPlaceEvent event) {
        // Cancel event
        // Convert locations of player and tnt to vectors
        // Player - tnt = vector
        // Normalize vector then linearly increase it
        // Create explosion
        // Set velocity of player to vector

        Block b = event.getBlockPlaced();
        Player p = event.getPlayer();
        World w = p.getWorld();
        int factor = 3;

        if (b.getType() != Material.TNT) return;
        if (!(p.getInventory().getItemInMainHand().isSimilar(createInstaboom()))) return;
        event.setCancelled(true);

        Location playerLoc = p.getLocation();
        Location exploLoc = b.getLocation();
        // Centerize location
        exploLoc.add(0.5, 0.5, 0.5);

        // Generate vector, player - tnt = vector
        Vector vector = playerLoc.toVector().subtract(exploLoc.toVector());
        vector.normalize();
        vector.multiply(factor);

        w.playSound(exploLoc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        w.spawnParticle(Particle.EXPLOSION_HUGE, exploLoc, 1);

        p.setVelocity(vector);

    }


    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            p.getInventory().addItem(createInstaboom());
            p.sendMessage(ChatColor.RED + "Instaboom TNT added to your inventory");
            return true;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    private static ItemStack createInstaboom() {
        ItemStack tnt = new ItemStack(Material.TNT);
        ItemMeta meta = tnt.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Instaboom TNT");
        tnt.setItemMeta(meta);
        tnt.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        return tnt;
    }
}
