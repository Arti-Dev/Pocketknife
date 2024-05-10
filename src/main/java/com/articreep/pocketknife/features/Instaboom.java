package com.articreep.pocketknife.features;

import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
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
import java.util.Objects;

public class Instaboom extends PocketknifeSubcommand implements Listener {

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
        ItemStack item = p.getInventory().getItemInMainHand();
        World w = p.getWorld();
        int factor = 3;

        if (b.getType() != Material.TNT) return;
        if (!Objects.equals(Utils.getItemID(item), "INSTABOOM_TNT")) return;
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
        w.spawnParticle(Particle.EXPLOSION_EMITTER, exploLoc, 1);

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

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife Instaboom";
    }

    private static ItemStack createInstaboom() {
        ItemStack tnt = new ItemStack(Material.TNT);
        ItemMeta meta = tnt.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Instaboom TNT");
        Utils.setItemID(meta, "INSTABOOM_TNT");
        tnt.setItemMeta(meta);
        tnt.addUnsafeEnchantment(Enchantment.INFINITY, 1);
        return tnt;
    }

    @Override
    public String getDescription() {
        return "Recreation of Instaboom from the Hypixel Pit";
    }

    @Override
    protected void onDisable() {
        // nothing
    }
}
