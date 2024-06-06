//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.articreep.pocketknife.features.freeze;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import com.articreep.pocketknife.features.Parametric;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Freeze extends PocketknifeSubcommand implements Listener {
    protected static final HashMap<Player, FreezeTask> frozenPlayers = new HashMap<>();

    public String getDescription() {
        return null;
    }

    protected void onDisable() {

    }

    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command");
                return true;
            } else {
                player.getInventory().addItem(iceSprayWand());
                player.sendMessage("Gave you an ice spray wand");
            }
            return true;
        } else if (args.length == 1) {
            Player toFreeze = Bukkit.getPlayer(args[0]);
            if (toFreeze == null) {
                sender.sendMessage("Player not found");
            } else {
                freeze(toFreeze, 160);
            }
            return true;
        }
        return true;
    }

    private void freeze(Player toFreeze, int ticks) {
        if (frozenPlayers.containsKey(toFreeze)) return;
        HashMap<Attribute, Double> originalValues = new HashMap<>();
        ArrayList<Attribute> attributes = new ArrayList<>(
                Arrays.asList(Attribute.GENERIC_MOVEMENT_SPEED,
                        Attribute.GENERIC_JUMP_STRENGTH,
                        Attribute.PLAYER_BLOCK_INTERACTION_RANGE,
                        Attribute.PLAYER_ENTITY_INTERACTION_RANGE,
                        Attribute.GENERIC_GRAVITY,
                        Attribute.GENERIC_STEP_HEIGHT));

        for (Attribute attribute : attributes) {
            try {
                originalValues.put(attribute, toFreeze.getAttribute(attribute).getBaseValue());
                toFreeze.getAttribute(attribute).setBaseValue(0.0);
            } catch (NullPointerException e) {
                Bukkit.getLogger().info("Attribute not found on mob: " + attribute.toString());
            }
        }

        toFreeze.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(0.1);
        toFreeze.getAttribute(Attribute.GENERIC_GRAVITY).setBaseValue(1.0);
        FreezeTask task = new FreezeTask(toFreeze, originalValues, ticks);

        task.run();

        toFreeze.playSound(toFreeze.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 5, 0.5f);
        toFreeze.getWorld().spawnParticle(Particle.SNOWFLAKE, toFreeze.getLocation().add(0, 1, 0),
                100, 0, 0, 0, 0.3);

        frozenPlayers.put(toFreeze, task);
    }

    protected static void resetAttributes(Player player, HashMap<Attribute, Double> originalAttributes) {
        for (Attribute attribute : originalAttributes.keySet()) {
            player.getAttribute(attribute).setBaseValue(originalAttributes.get(attribute));
        }

        player.setFreezeTicks(0);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (frozenPlayers.containsKey(event.getPlayer())) {
            frozenPlayers.get(event.getPlayer()).cancel();
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (frozenPlayers.containsKey(event.getPlayer())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to == null || from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
                return;
            }

            Player player = event.getPlayer();
            player.getWorld().spawnParticle(Particle.BLOCK,
                    player.getLocation().add(0.0, 1.0, 0.0), 20,
                    0.5, 1.0, 0.5, 0.1, Material.ICE.createBlockData());
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.3f, 1.5f);
            frozenPlayers.get(player).decreaseTicks(5);
        }

    }

    @EventHandler
    public void onPlayerRightClickBlock(PlayerInteractEvent event) {
        // If player is holding an ice spray wand
        if (event.getItem() != null && Utils.hasItemID(event.getItem())
                && Utils.getItemID(event.getItem()).equals("ice_spray_wand")
                && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Location location = event.getClickedBlock().getLocation()
                    .add(event.getClickedPosition());
            bouncyIceWithHitboxes(location, player);

        }
    }

    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    public String getSyntax() {
        return null;
    }

    public ItemStack iceSprayWand() {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("Not Ice Spray Wand");
        meta.setLore(Arrays.asList("Right click a block to spawn a barrage of ice and freeze people"));
        wand.setItemMeta(meta);
        wand.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        Utils.setItemID(wand, "ice_spray_wand");
        return wand;
    }

    private void bouncyIceWithHitboxes(Location initialLocation, Player player) {
        Vector direction = player.getLocation().getDirection().setY(0);
        if (!direction.isZero()) direction.normalize();
        int maxTicks = 100;

        ArrayList<BlockDisplay> trail = new ArrayList<>();
        new BukkitRunnable() {
            int elapsed = 0;
            double t = Math.PI / 2;
            @Override
            public void run() {
                if (elapsed < 60 && elapsed % 2 == 0) {
                    trail.add(Parametric.iceDisplay(initialLocation, Material.ICE, 0.5f));
                }

                Random random = new Random();

                double offset = 2;

                // collision detection
                for (BlockDisplay block : trail) {
                    if (block.isDead()) continue;
                    List<Entity> entitiesNearby = block.getNearbyEntities(1, 1.2, 1);
                    boolean shouldRemove = false;
                    for (Entity entity : entitiesNearby) {
                        if (entity instanceof Player toFreeze && entity != player) {
                            freeze(toFreeze, 160);
                            shouldRemove = true;
                        } else if (entity instanceof LivingEntity livingEntity && entity != player) {
                            // knockback
                            livingEntity.setVelocity(direction.clone().multiply(0.5).setY(0.1));
                            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.3f, 1.5f);
                            livingEntity.getWorld().spawnParticle(Particle.BLOCK, livingEntity.getLocation().add(0, 1, 0),
                                    10, 0, 0, 0, 0.1, Material.ICE.createBlockData());
                            livingEntity.damage(1);
                            shouldRemove = true;
                        }
                    }
                    if (shouldRemove) {
                        block.remove();
                    }

                }

                for (BlockDisplay block : trail) {
                    double x = (t - offset) - Math.sin(t - offset);
                    double y = 0.5 - Math.cos(t - offset)/2;
                    block.teleport(initialLocation.clone().add(0, y, 0)
                            .add(direction.clone().multiply(x)));
                    block.setRotation(random.nextInt(0, 360), random.nextInt(-90, 90));
                    offset += 2;
                }

                if (elapsed > maxTicks) {
                    for (BlockDisplay block : trail) {
                        if (block.isDead()) continue;
                        block.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, block.getLocation(), 75, 0, 0, 1, 75);
                        block.remove();
                    }
                    cancel();
                }
                elapsed++;
                t += 1;
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }
}