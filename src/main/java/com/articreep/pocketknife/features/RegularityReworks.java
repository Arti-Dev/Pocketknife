package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
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
    /**
     * This value is in seconds.
     */
    private double explodeTime = 2.5;
    private final double multiplier = 0.75;
    private double bonusKB = 0.8;
    private final static HashSet<Player> playersOnCooldown = new HashSet<>();
    private final static HashSet<Player> playersEmpowered = new HashSet<>();
    @Override
    public String getDescription() {
        return "Regularity reworks in practice, except on latest version. Choose an option (1-5) to obtain a pair of pants. All bonus damage is +75%.";
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

        ItemStack pants = damager.getInventory().getLeggings();
        String id = Utils.getItemID(pants);
        if (id == null) return;

        if (id.startsWith("REGULARITY_")) {
            int option = Utils.parseInt(id.substring(id.length() - 1));

            // Cooldown only
            if (option == 1) {
                if (event.getFinalDamage() > 3) return;
                if (!playersOnCooldown.contains(damager)) {
                    double bonusDamage = event.getFinalDamage() * multiplier;
                    putOnCooldown(damager, cooldown);
                    Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> applyExtraHit(damager, victim, bonusDamage, 0.4), 2);
                }
            // Empower next hit for more
            } else if (option == 2) {
                if (event.getFinalDamage() > 3) return;
                if (playersEmpowered.contains(damager)) {
                    playersEmpowered.remove(damager);
                    event.setDamage(event.getDamage() * 1 + multiplier);
                    damager.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "REGS! " + ChatColor.RESET + "Your follow-up dealt " + event.getFinalDamage() + " damage!");
                } else {
                    playersEmpowered.add(damager);
                    damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_GUARDIAN_HURT, 1, 1);
                }
            // Bombs
            } else if (option == 3) {
                if (event.getFinalDamage() > 3) return;
                applyBomb(victim, explodeTime, false);
            // Cooldown with extra knockback
            } else if (option == 4) {
                if (event.getFinalDamage() > 3) return;
                if (!playersOnCooldown.contains(damager)) {
                    double bonusDamage = event.getFinalDamage() * multiplier;
                    putOnCooldown(damager, cooldown);
                    Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> applyExtraHit(damager, victim, bonusDamage, bonusKB), 2);
                }
            // Bombs + victim control
            } else if (option == 5) {
                if (event.getFinalDamage() > 3) return;
                applyBomb(victim, explodeTime, true);
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
                    if (value >= 0) {
                        cooldown = value;
                        sender.sendMessage(ChatColor.GREEN + "Cooldown set to " + value + " seconds");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Cooldown must be non-negative and actually a number");
                    }
                }
            // explode time
            } else if (args[0].equalsIgnoreCase("explodeTime")) {
                if (args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "Current explode time is " + explodeTime + " seconds");
                } else {
                    // Must be positive and actually a number
                    double value = Utils.parseDouble(args[1]);
                    if (value >= 0) {
                        explodeTime = value;
                        sender.sendMessage(ChatColor.GREEN + "Explode time set to " + value + " seconds");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Explode time must be non-negative and actually a number");
                    }
                }
            // bonus KB for option 4
            } else if (args[0].equalsIgnoreCase("bonusKB")) {
                if (args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "Current bonus knockback (option 4) is " + bonusKB + ". 0.4 is normal.");
                } else {
                    // Must be positive and actually a number
                    double value = Utils.parseDouble(args[1]);
                    if (value >= 0) {
                        bonusKB = value;
                        sender.sendMessage(ChatColor.GREEN + "Bonus knockback (option 4) set to " + value);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Bonus knockback must be non-negative and actually a number");
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
                if (option < 1 || option > 5) {
                    sender.sendMessage(ChatColor.RED + "Available options are 1, 2, 3, 4, 5");
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
            strings.add("5");
            strings.add("cooldown");
            strings.add("explodeTime");
            strings.add("bonusKB");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife RegularityReworks <type/cooldown/explodeTime/bonusKB> <amount>";
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

    private void applyExtraHit(Player damager, Player victim, double damage, double knockback) {
        damager.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "REGS! " + ChatColor.RESET + "Your second hit dealt " + damage + " damage!");
        victim.setNoDamageTicks(0);
        // Horizontal
        Vector kb = damager.getLocation().getDirection().normalize().multiply(knockback);
        // Vertical
        kb.setY(0.4);
        victim.setVelocity(kb);
        victim.damage(damage);
    }

    private void applyBomb(Player victim, double time, boolean allowControl) {
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PARROT_IMITATE_CREEPER, 1, 1);
        new BukkitRunnable() {
            double t = time * 20;
            double rads = 0;
            @Override
            public void run() {
                if (victim.isDead()) this.cancel();
                if (t <= 0) {
                    // Whether player can influence the direction of the blast
                    if (allowControl) {
                        victim.setVelocity(victim.getLocation().getDirection().multiply(0.4).setY(0.4));
                    } else {
                        victim.setVelocity(Utils.randomKB(0.4));
                    }
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                    victim.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, victim.getLocation(), 1);
                    Utils.trueDamage(victim, 1);
                    launchNearby(victim);
                    this.cancel();
                }
                Location loc = victim.getLocation().add(Math.sin(rads), 1, Math.cos(rads));
                victim.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, new Particle.DustOptions(Color.RED, 0.7F));
                rads += 0.3;
                t--;

            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }

    private void launchNearby(Player victim) {
        for (Entity entity : victim.getNearbyEntities(2, 2, 2)) {
            if (entity instanceof Player player) {
                player.setVelocity(Utils.entitiesToHorizontalNormalizedVector(victim, player, 0.4).setY(0.4));
                Utils.trueDamage(player, 1);
            }
        }
    }

    private static ItemStack createPants(int option) {
        final ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(ChatColor.DARK_RED + "Regularity Testing Pants - Option " + option);
        Utils.setItemID(meta, "REGULARITY_" + option);

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
        } else if (option == 5) {
            // Set the lore of the item
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Regularity but with explosives attached + victim control",
                    "",
                    ChatColor.RED + "Every hit attaches a bomb to your opponent, dealing knockback to",
                    ChatColor.RED + "surrounding players and 1 very true damage to your opponent after 1.5s/2s/2.5s.",
                    ChatColor.GRAY + "Victims are blasted in the direction they are looking."));
        } else {
            return null;
        }

        item.setItemMeta(meta);
        return item;
    }
}
