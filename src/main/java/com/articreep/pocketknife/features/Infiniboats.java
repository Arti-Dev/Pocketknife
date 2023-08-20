package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeFeature;
import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class Infiniboats extends PocketknifeSubcommand implements PocketknifeFeature, Listener {

    @Override
    public String getDescription() {
        return "Infinitely place boats that expire after a few seconds";
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        player.getInventory().addItem(createBoat(1));
        player.sendMessage(ChatColor.GREEN + "Added an Infiniboat to your inventory!");
        return true;
    }

    @EventHandler
    public void onBoatPlace(EntityPlaceEvent event) {
        if (event.getEntity() instanceof Boat boat && event.getPlayer() != null) {
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand().clone();

            // messy, gotta fix!
            if (Utils.getItemID(itemStack).equals(Utils.getItemID(createBoat(1)))) {

                Player player = event.getPlayer();
//                Boat.Type boatType = boat.getBoatType();
//                Boat newBoat = (Boat) event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation().add(0, 0.7, 0), EntityType.BOAT);
//                newBoat.setBoatType(boatType);
//                newBoat.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());


                Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), boat::remove, 100);
                // todo make this work for any inventory slot
                Bukkit.getScheduler().runTask(Pocketknife.getInstance(), () -> player.getInventory().addItem(itemStack));
                Bukkit.getScheduler().runTask(Pocketknife.getInstance(), () -> boat.addPassenger(player));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife Infiniboats";
    }

    public ItemStack createBoat(int amount) {
        ItemStack item = new ItemStack(Material.OAK_BOAT);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Infiniboat");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Hold right-click to continuously " +
                        "place, enter, and place more boats as a method of transportation!",
                ChatColor.GRAY + "They will disappear after a few seconds"));
        Utils.setItemID(meta, "INFINIBOAT");

        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }
}
