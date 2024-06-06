//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeSubcommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Freeze extends PocketknifeSubcommand implements Listener {
    private final HashMap<Player, BukkitTask> frozenPlayers = new HashMap<>();

    public Freeze() {

    }

    public String getDescription() {
        return null;
    }

    protected void onDisable() {

    }

    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        } else {
            Player toFreeze = Bukkit.getPlayer(args[0]);
            if (toFreeze == null) {
                sender.sendMessage("Player not found");
            } else {
                if (frozenPlayers.containsKey(toFreeze)) return true;
                toFreeze.setFreezeTicks(160);
                HashMap<Attribute, Double> originalValues = new HashMap<>();
                ArrayList<Attribute> attributes = new ArrayList<>(
                        Arrays.asList(Attribute.GENERIC_MOVEMENT_SPEED,
                        Attribute.GENERIC_JUMP_STRENGTH,
                        Attribute.PLAYER_BLOCK_BREAK_SPEED,
                        Attribute.PLAYER_BLOCK_INTERACTION_RANGE,
                        Attribute.PLAYER_ENTITY_INTERACTION_RANGE,
                        Attribute.GENERIC_GRAVITY));

                for (Attribute attribute : attributes) {
                    try {
                        originalValues.put(attribute, toFreeze.getAttribute(attribute).getBaseValue());
                        toFreeze.getAttribute(attribute).setBaseValue(0.0);
                    } catch (NullPointerException e) {
                        Bukkit.getLogger().info("Attribute not found on player: " + attribute.toString());
                    }
                }

                toFreeze.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(0.2);
                toFreeze.getAttribute(Attribute.GENERIC_GRAVITY).setBaseValue(1.0);
                BukkitTask task = new BukkitRunnable() {
                    public void run() {
                        if (toFreeze.getFreezeTicks() > 0) {
                            toFreeze.setFreezeTicks(toFreeze.getFreezeTicks() - 1);
                        } else {
                            resetAttributes(toFreeze, originalValues);
                            frozenPlayers.remove(toFreeze);
                            this.cancel();
                        }

                    }
                }.runTaskTimer(Pocketknife.getInstance(), 0, 1);

                this.frozenPlayers.put(toFreeze, task);
            }
            return true;
        }
    }

    private void resetAttributes(Player player, HashMap<Attribute, Double> originalAttributes) {
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
            if (to != null && from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
                return;
            }

            Player player = event.getPlayer();
            player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation().add(0.0, 1.0, 0.0), 20, 0.5, 1.0, 0.5, 0.1, Material.ICE.createBlockData());
        }

    }

    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    public String getSyntax() {
        return null;
    }
}