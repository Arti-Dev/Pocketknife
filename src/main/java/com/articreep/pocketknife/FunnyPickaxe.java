package com.articreep.pocketknife;

import net.craftcitizen.imagemaps.ImageMap;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class FunnyPickaxe extends PocketknifeSubcommand implements Listener {
    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            int amount;
            if (args.length >= 1) {
                if (!args[0].equalsIgnoreCase("confirm")) {
                    // Player just wants a pickaxe
                    try {
                        amount = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "That's not an integer.");
                        return true;
                    }

                    if (amount <= 0) {
                        player.sendMessage(ChatColor.YELLOW + "Zero or less?");
                        return true;
                    } else if (amount == 1) {
                        player.sendMessage(ChatColor.YELLOW + "Gave you a " + ChatColor.GOLD + "Golden Pickaxe");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Stacking is definitely an intended mechanic.");
                    }
                    player.getInventory().addItem(goldenPickaxe(amount));
                } else {
                    // Player might have clicked a confirm button
                    if (args.length == 2) {
                        // Second one must be a UUID
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(args[1]);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Something went wrong. Impressive.");
                            return true;
                        }

                        if (confirmations.containsKey(uuid)) {
                            Location loc = confirmations.get(uuid);
                            Block block = loc.getBlock();
                            if (block.getType() == Material.OBSIDIAN) {
                                // Break the block
                                block.setType(Material.AIR);
                                player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
                                // Remove any other confirmations referencing the block
                                confirmations.keySet().removeIf(uuid1 -> confirmations.get(uuid1).equals(loc));
                            } else {
                                // Block is not obsidian
                                player.sendMessage(ChatColor.RED + "This confirmation has expired!");
                            }
                            confirmations.remove(uuid);
                        } else {
                            player.sendMessage(ChatColor.RED + "This confirmation has expired!");
                        }

                    }
                }
            }

        } else {
            Bukkit.getLogger().info("You're not a player.");
        }
        return true;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    String getSyntax() {
        return "Usage: /pocketknife FunnyPickaxe <amount>";
    }

    private final Map<UUID, Location> confirmations = new HashMap<>();
    @EventHandler
    public void onPickaxeBreak(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();

        if (b != null && b.getType() == Material.OBSIDIAN) {
            // Verify they're holding the golden pickaxe
            ItemStack item = p.getInventory().getItemInMainHand();
            if (Objects.equals(Utils.getItemID(item), "FUNNY_PICKAXE")) {
                event.setCancelled(true);

                // I'm too worried about dupe UUIDs, aren't I?
                UUID randomUUID = UUID.randomUUID();
                while (confirmations.containsKey(randomUUID)) {
                    randomUUID = UUID.randomUUID();
                }

                confirmations.put(randomUUID, b.getLocation());
                UUID finalRandomUUID = randomUUID;
                Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> confirmations.remove(finalRandomUUID), 200);

                // Send the JSON chat message
                TextComponent component = new TextComponent(ChatColor.DARK_PURPLE + String.valueOf(ChatColor.BOLD) + "BLOCK! "  +
                        ChatColor.GRAY + "Are you sure you want to break this block? ");
                TextComponent confirmComponent = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[YES]");
                confirmComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pocketknife funnypickaxe confirm" + randomUUID));
                confirmComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Click to confirm!")));
                component.addExtra(confirmComponent);
                p.spigot().sendMessage(component);
            }
        }
    }

    @EventHandler
    public void onItemFrameRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame itemFrame) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (Utils.getItemID(item).equals(Utils.getItemID(goldenPickaxe(1)))) {
                event.setCancelled(true);
                player.playSound(player, Sound.ITEM_GLOW_INK_SAC_USE, 1, 1);
                boolean glowing = itemFrame.isGlowing();
                itemFrame.setGlowing(!glowing);

            }
        }

    }

    private static ItemStack goldenPickaxe(int quantity) {
        ItemStack item = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Golden Pickaxe");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Breaks a 5-high pillar of", ChatColor.GRAY + "obsidian when 2-tapping it."));
        Utils.setItemID(meta, "FUNNY_PICKAXE");

        item.setItemMeta(meta);
        item.setAmount(quantity);
        return item;
    }

    @Override
    public String getDescription() {
        return "Meant to be a joke";
    }
}
