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
    // PlayerMoveEvent.getFrom() is not being reliable, so...
    // Honestly this is pretty bad either way
    private final Map<Player, Location> previousLocations = new HashMap<>();
    private final Map<Player, Location> currentLocations = new HashMap<>();


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
        if (enabled) {
            // nothing here yet
        }
        if (lines) {
            drawLines(player, previousLocations.get(player), player.getLocation());
        }
    }

    private void drawLines(Player player, Location prevLoc, Location currentLoc) {
        World w = player.getWorld();
        boolean ignoreMovement = false;

        if (currentLoc.getWorld() != w || prevLoc.getWorld() != w) return;

        // Ignore y coordinates here
        Vector movementDir = currentLoc.subtract(prevLoc).toVector().setY(0);
        Vector playerDir = player.getLocation().getDirection().setY(0);

        if (movementDir.isZero()) ignoreMovement = true;

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
                if (currentLocations.containsKey(player)) {
                    Location oldLoc = currentLocations.get(player);
                    previousLocations.put(player, oldLoc);
                    currentLocations.put(player, player.getLocation());
                } else {
                    currentLocations.put(player, player.getLocation());
                    previousLocations.put(player, player.getLocation());
                }
            }
        }, 0, 1);

        return task.getTaskId();
    }
}
