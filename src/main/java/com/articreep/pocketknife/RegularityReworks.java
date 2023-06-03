package com.articreep.pocketknife;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegularityReworks extends PocketknifeSubcommand implements Listener {
    @Override
    public String getDescription() {
        return "Regularity reworks in practice, except on latest version. Choose an option (1-3) to obtain a pair of pants.";
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendDescriptionMessage(sender);
            sendSyntaxMessage(sender);
        } else {
            int option;
            try {
                option = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "That's not an integer.");
                return true;
            }
            if (option < 1 || option > 3) {
                sender.sendMessage(ChatColor.RED + "Available options are 1, 2, or 3");
                return true;
            }
            Player player = (Player) sender;
            player.getInventory().addItem(createPants(option));
            sender.sendMessage(ChatColor.DARK_RED + "Regularity - Option " + option + " added to your inventory!");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add("1");
            strings.add("2");
            strings.add("3");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife RegularityReworks <type>";
    }

    private static ItemStack createPants(int option) {
        final ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(ChatColor.DARK_RED + "Regularity Testing Pants - Option " + option);

        if (option == 1) {
            // Set the lore of the item
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Nothing here yet!"));
        } else if (option == 2) {
            // Set the lore of the item
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Nothing here yet!"));
        } else if (option == 3) {
            // Set the lore of the item
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Nothing here yet!"));
        } else {
            return null;
        }

        item.setItemMeta(meta);
        return item;
    }
}
