package com.articreep.pocketknife;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RegularityReworks extends PocketknifeSubcommand implements Listener {
    /**
     * This value is in seconds.
     */
    private double cooldown = 3;
    private final static HashSet<Player> playersOnCooldown = new HashSet<>();
    @Override
    public String getDescription() {
        return "Regularity reworks in practice, except on latest version. Choose an option (1-3) to obtain a pair of pants.";
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Player damager;

        // Melee hit?
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else {
            return;
        }

        // Read their pants.
        // The conditions to make this work: If the name starts with "Regularity Testing Pants" and ends in the correct option.

        ItemStack pants = damager.getInventory().getLeggings();
        if (pants == null || !pants.hasItemMeta()) return;
        ItemMeta meta = pants.getItemMeta();
        if (!meta.hasDisplayName()) return;
        String name = pants.getItemMeta().getDisplayName();

        if (name.startsWith(ChatColor.DARK_RED + "Regularity Testing Pants")) {
            int option = Utils.parseInt(name.substring(name.length() - 1));
            double bonusDamage = event.getFinalDamage() * 0.75;

            // Cooldown only
            if (option == 1) {
                if (!playersOnCooldown.contains(damager)) {
                    putOnCooldown(damager, cooldown);
                    Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> applyExtraHit(damager, victim, bonusDamage), 2);
                }
            }
        }
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendDescriptionMessage(sender);
            sendSyntaxMessage(sender);
        } else {
            // cooldown
            if (args[0].equalsIgnoreCase("cooldown")) {
                if (args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "Current cooldown is " + cooldown + " seconds");
                } else {
                    // Must be positive and actually a number
                    double value = Utils.parseDouble(args[1]);
                    if (value > 0) {
                        cooldown = value;
                        sender.sendMessage(ChatColor.GREEN + "Cooldown set to " + value + " seconds");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Cooldown must be non-negative and actually a number");
                    }
                }
            // option select
            } else {
                int option;
                try {
                    option = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "That's not an integer.");
                    return true;
                }
                if (option < 1 || option > 4) {
                    sender.sendMessage(ChatColor.RED + "Available options are 1, 2, 3, or 4");
                    return true;
                }
                Player player = (Player) sender;
                player.getInventory().addItem(createPants(option));
                sender.sendMessage(ChatColor.DARK_RED + "Regularity - Option " + option + " added to your inventory!");
            }
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
            strings.add("4");
            strings.add("cooldown");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife RegularityReworks <type/cooldown> <cooldown-amount>";
    }

    private static void putOnCooldown(Player player, double cooldown) {
        playersOnCooldown.add(player);
        // Add actionbar text
        new BukkitRunnable() {
            double cooldownLeft = cooldown * 20;
            @Override
            public void run() {
                if (cooldownLeft <= 0) {
                    playersOnCooldown.remove(player);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
                    this.cancel();
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Regularity CD: "
                        + String.format("%.2f", cooldownLeft/20) + "s"));
                cooldownLeft--;
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }

    private void applyExtraHit(Player damager, Player victim, double damage) {
        damager.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "REGS! " + ChatColor.RESET + "Your second hit dealt " + damage + " damage!");
        victim.setNoDamageTicks(0);
        // Horizontal
        Vector kb = Utils.entitiesToHorizontalNormalizedVector(damager, victim, 0.4);
        // Vertical
        kb.add(new Vector(0, 0.4, 0));
        victim.setVelocity(kb);
        victim.damage(damage);
    }

    private static ItemStack createPants(int option) {
        final ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(ChatColor.DARK_RED + "Regularity Testing Pants - Option " + option);

        if (option == 1) {
            // Set the lore of the item
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Regularity but just with a longer cooldown",
                    "",
                    ChatColor.RED + "If your hit deals less than 3 damage,",
                    ChatColor.RED + "strike again in " + ChatColor.YELLOW + "0.1s" + ChatColor.RED + " for 75% damage.",
                    ChatColor.GRAY + "Cooldown varies, check /pocketknife RegularityReworks cooldown"));
        } else if (option == 2) {
            // Set the lore of the item
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Regularity but your next hit just deals more damage",
                    "",
                    ChatColor.RED + "If your hit deals less than 3 damage,",
                    ChatColor.RED + "charge your next hit for " + ChatColor.DARK_RED + "+75%" + ChatColor.RED + " damage."));
        } else if (option == 3) {
            // Set the lore of the item
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Regularity but with explosives attached",
                    "",
                    ChatColor.RED + "Every hit attaches a bomb to your opponent, dealing knockback to",
                    ChatColor.RED + "surrounding players and 1 very true damage to your opponent after 1.5s/2s/2.5s."));
        } else if (option == 4) {
            // Set the lore of the item
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Regularity but with a longer cooldown AND extra kb",
                    "",
                    ChatColor.RED + "If your hit deals less than 3 damage,",
                    ChatColor.RED + "strike again in " + ChatColor.YELLOW + "0.1s" + ChatColor.RED + " for 75% damage.",
                    ChatColor.RED + "The second hit deals more knockback.",
                    ChatColor.GRAY + "Cooldown varies, check /pocketknife RegularityReworks cooldown"));
        } else {
            return null;
        }

        item.setItemMeta(meta);
        return item;
    }
}
