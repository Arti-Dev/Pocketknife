package com.articreep.pocketknife;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Speedtech extends PocketknifeSubcommand implements PocketknifeFeature, Listener {
    private boolean enabled = false;
    private boolean lines = false;
    private int taskID = -1;
    private final Map<Player, Float> initialSpeeds = new HashMap<>();
    private final Map<Player, Integer> speedBonuses = new HashMap<>();
    private final Map<Player, Double> previousAngles = new HashMap<>();
    // PlayerMoveEvent.getFrom() is not being reliable, so...
    // Honestly this is pretty bad either way
    private final Map<Player, Location> previousLocations = new HashMap<>();


    @Override
    public String getDescription() {
        return "Weird ways to go fast!";
    }

    @Override
    boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                sendDescriptionMessage(sender);
                sendSyntaxMessage(sender);
            } else {
                switch (args[0]) {
                    case "toggle" -> {
                        enabled = !enabled;
                        sender.sendMessage(ChatColor.GREEN + "Speedtech toggled " + Utils.booleanStatus(enabled));
                        if (enabled) {
                            taskID = trackLocations();
                        } else {
                            Bukkit.getScheduler().cancelTask(taskID);
                            taskID = -1;
                        }
                    }

                    case "lines" -> {
                        lines = !lines;
                        sender.sendMessage(ChatColor.GREEN + "Lines toggled " + Utils.booleanStatus(lines));
                        if (lines) {
                            player.sendMessage(ChatColor.YELLOW + "Speedtech was toggled ON for you");
                            taskID = trackLocations();
                            enabled = true;
                            player.sendMessage(ChatColor.GREEN + "GREEN is the direction where you are looking at");
                            player.sendMessage(ChatColor.RED + "RED is the direction where you are moving");
                        }
                    }

                    default -> sendSyntaxMessage(sender);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add("toggle");
            strings.add("lines");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @Override
    String getSyntax() {
        return "Usage: /pocketknife Speedtech toggle";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (lines) {
            drawLines(player, previousLocations.get(player), player.getLocation());
        }
    }

    private void drawLines(Player player, Location prevLoc, Location currentLoc) {
        // Clone before we do anything
        prevLoc = prevLoc.clone();
        currentLoc = currentLoc.clone();

        World w = player.getWorld();
        boolean ignoreMovement = false;

        if (currentLoc.getWorld() != w || prevLoc.getWorld() != w) return;

        // Ignore y coordinates here
        Vector movementDir = currentLoc.subtract(prevLoc).toVector().setY(0);
        Vector playerDir = player.getLocation().getDirection().setY(0);

        if (movementDir.isZero()) {
            player.sendMessage("Movement dir is zero");
            ignoreMovement = true;
        }

        double inbetween = 0.2;
        double distance = 10;

        playerDir.normalize().multiply(inbetween);
        if (!ignoreMovement) movementDir.normalize().multiply(inbetween);

        Location loc1 = player.getLocation();
        Location loc2 = player.getLocation();
        Particle.DustOptions green = new Particle.DustOptions(Color.GREEN, 1);
        Particle.DustOptions red = new Particle.DustOptions(Color.RED, 1);

        for (double covered = 0; covered <= distance; covered += inbetween) {
            w.spawnParticle(Particle.REDSTONE, loc1.add(playerDir), 2, green);
            if (!ignoreMovement) w.spawnParticle(Particle.REDSTONE, loc2.add(movementDir), 2, red);
            covered += inbetween;
        }

        if (!ignoreMovement) {
            double angle = movementDir.angle(playerDir);
            angle = angle * (180/Math.PI);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.YELLOW + "Angle between the two lines is " + String.format("%.2f", angle) +
                            " degrees"));
        }
    }

    private int trackLocations() {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Pocketknife.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {

                Location oldLoc;
                Location newLoc = player.getLocation();
                if (!previousLocations.containsKey(player)) {
                    previousLocations.put(player, newLoc);
                }
                oldLoc = previousLocations.get(player);

//                double oldAngle;
//                double newAngle = calculateAngle(player, oldLoc, newLoc);
//                if (!previousAngles.containsKey(player)) {
//                    previousAngles.put(player, newAngle);
//                }
//                oldAngle = previousAngles.get(player);

//                /* Conditions to gain speed:
//                - Player must be sprinting
//                - Angle must deviate at least 10 degrees from its previous measurement (previous tick)
//                - Angle must be between 10 and 35 degrees
//
//                Speed will build up linearly at a rate of 0.007.
//                (Default speed is 0.2, max speed is 1)
//                A successful tick adds 1 to the speed variable.
//                Players need at least 5 on this meter before gaining any speed.
//
//                Speed will reset to its initial value if:
//                - Player instantly comes to a stop (previous location == current location)
//                This is not lag-friendly, but that's okay.
//                */
//
//                double thresholdMin = 10;
//                double thresholdMax = 35;
//                double deviate = 10;
//                double rate = 0.007;
//
//                // Conditions to gain speed
//                if (player.isSprinting() && Math.abs(oldAngle - newAngle) >= deviate
//                        && thresholdMin <= newAngle && newAngle <= thresholdMax) {
//
//                    // Add speed bonus
//                    if (!speedBonuses.containsKey(player)) {
//                        initialSpeeds.put(player, player.getWalkSpeed());
//                        speedBonuses.put(player, 1);
//                    } else {
//                        speedBonuses.put(player, speedBonuses.get(player) + 1);
//                    }
//
//                    player.sendMessage("Speeding up...");
//
//                    int bonus = speedBonuses.get(player);
//                    float initialSpeed = initialSpeeds.get(player);
//                    float speed;
//                    // Calculate walk speed
//                    if (bonus > 5) {
//                        speed = (float) (initialSpeed + (rate * (bonus - 5)));
//                    } else {
//                        // do not apply bonuses yet
//                        speed = initialSpeed;
//                    }
//
//                    if (speed < initialSpeed) speed = initialSpeed;
//                    if (speed > 1) speed = 1;
//
//                    player.setWalkSpeed(speed);
//
//                }
//
//                // Conditions to lose all speed
//                if (speedBonuses.containsKey(player)) {
//                    if (oldLoc.equals(newLoc)) {
//                        speedBonuses.remove(player);
//                        player.setWalkSpeed(initialSpeeds.get(player));
//                        initialSpeeds.remove(player);
//                    }
//                }


                // Operation's over, prepare for the next tick
//                previousAngles.put(player, newAngle);
                player.sendMessage(newLoc.toString());
                player.sendMessage(oldLoc.toString());
                previousLocations.put(player, newLoc);
            }
        }, 0, 1);

        return task.getTaskId();
    }

    private double calculateAngle(Player player, Location prevLoc, Location currentLoc) {
        // Clone before we use them
        prevLoc = prevLoc.clone();
        currentLoc = currentLoc.clone();

        World w = player.getWorld();

        if (currentLoc.getWorld() != w || prevLoc.getWorld() != w) return -1;

        // Ignore y coordinates here
        Vector movementDir = currentLoc.subtract(prevLoc).toVector().setY(0);
        Vector playerDir = player.getLocation().getDirection().setY(0);

        if (movementDir.isZero()) return -1;

        double angle = movementDir.angle(playerDir);
        angle = angle * (180/Math.PI);
        return angle;
    }
}
