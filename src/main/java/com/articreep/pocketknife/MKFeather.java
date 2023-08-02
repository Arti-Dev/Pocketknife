package com.articreep.pocketknife;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MKFeather extends PocketknifeSubcommand implements PocketknifeFeature, Listener {
    @Override
    public String getDescription() {
        return "Replica of Feathers from Mario Kart 8";
    }

    @Override
    boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendDescriptionMessage(sender);
            sendSyntaxMessage(sender);
        } else {
            int amount;
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Please input a valid number");
                return true;
            }
            Player player = (Player) sender;
            player.getInventory().addItem(generateFeather(amount));
            player.sendMessage(ChatColor.YELLOW + "Added to your inventory!");
        }
        return true;
    }

    public static ItemStack generateFeather(int amount) {
        ItemStack feather = new ItemStack(Material.FEATHER, amount);
        ItemMeta meta = feather.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Mario Kart Feather");
        Utils.setItemID(meta, "MK_FEATHER");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "Grants:",
                ChatColor.AQUA + "Jump Boost VII" + ChatColor.YELLOW + " for 1s",
                ChatColor.WHITE + "Speed II " + ChatColor.YELLOW + "for 10s",
                ChatColor.DARK_GRAY + "15s cooldown",
                ChatColor.DARK_GRAY + String.valueOf(ChatColor.ITALIC) + "Kept on death"));
        feather.setItemMeta(meta);
        return feather;
    }

    private final Set<Player> playersOnCooldown = new HashSet<>();
    @EventHandler
    public void onFeather(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FEATHER) return;
        if (Utils.getItemID(item).equalsIgnoreCase("MK_FEATHER")) {
            event.setCancelled(true);
            if (!playersOnCooldown.contains(player)) {
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20, 7-1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 2-1));
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_JUMP, 1, 1);

                if (player.getGameMode() != GameMode.CREATIVE) {
                    playersOnCooldown.add(player);
                    Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(),
                            () -> playersOnCooldown.remove(player), 15 * 20);
                }
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_SNIFFER_DIGGING_STOP, 1, 1);
            }
        }
    }

    @EventHandler
    public void onDC(PlayerQuitEvent event) {
        playersOnCooldown.remove(event.getPlayer());
    }

    @Override
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    String getSyntax() {
        return "Usage: /pocketknife MKFeather <amount>";
    }
}
