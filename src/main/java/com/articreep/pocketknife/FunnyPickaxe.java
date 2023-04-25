package com.articreep.pocketknife;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class FunnyPickaxe implements Listener, PocketknifeCommand {
    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            int amount = 1;
            if (args.length >= 1) {
                try {
                    amount = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "That's not an integer.");
                    return true;
                }
            }
            if (amount <= 0) {
                player.sendMessage(ChatColor.YELLOW + "Zero or less?");
                return true;
            } else if (amount == 1) {
                player.sendMessage(ChatColor.YELLOW + "Go ham!");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Stacking is definitely an intended mechanic.");
            }
            player.getInventory().addItem(goldenPickaxe(amount));
        } else {
            Bukkit.getLogger().info("You're not a player.");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @EventHandler
    public void onPickaxeBreak(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        // these if statements are horrendous, fix them later
        if (b.getType() == Material.OBSIDIAN) {
            // Verify they're holding the golden pickaxe
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType() == Material.GOLDEN_PICKAXE) {
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Golden Pickaxe")) {
                    event.setCancelled(true);
                    // Send the JSON chat message
                    TextComponent component = new TextComponent(ChatColor.GOLD + "Haha, funny pickaxe!");
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pocketknife funnypickaxe"));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click for one!")));
                    p.spigot().sendMessage(component);
                }
            }
        }
    }
    private static ItemStack goldenPickaxe(int quantity) {
        ItemStack item = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Golden Pickaxe");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Breaks a 5-high pillar of", ChatColor.GRAY + "obsidian when 2-tapping it."));

        item.setItemMeta(meta);
        item.setAmount(quantity);
        return item;
    }
}
