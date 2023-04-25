package com.articreep.pocketknife;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class HoeOfNoTilling implements PocketknifeSubcommand, Listener {

	@Override
	public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player player) {
			player.getInventory().addItem(createHoe());
			player.sendMessage(ChatColor.GOLD + "Hoe of No Tilling added to your inventory.");
			return true;
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}

	@EventHandler
	public void onDirtTill(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		ItemMeta itemmeta = item.getItemMeta();
		if (item.getType().equals(Material.AIR)) return;
		if (!itemmeta.hasDisplayName()) return;
		if (itemmeta.getDisplayName().equals(ChatColor.GREEN + "Hoe of No Tilling")) {
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				if (event.getClickedBlock().getType().equals(Material.FARMLAND)) {
					event.getClickedBlock().setType(Material.DIRT);
					player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 0.5F, 0.5F);
				}
			}
		}
	}

	private static ItemStack createHoe() {
		final ItemStack item = new ItemStack(Material.DIAMOND_HOE);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(ChatColor.GREEN + "Hoe of No Tilling");

        // Set the lore of the item
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Untills farmland!", "",
        		ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Mathematically proven to", 
        		ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "be more efficient than",
        		ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "jumping on it!",
        		"",
        		ChatColor.DARK_GRAY + "This item can be reforged!",
        		ChatColor.GREEN + "" + ChatColor.BOLD + "UNCOMMON HOE"));
        

        item.setItemMeta(meta);

        return item;
	}
}
