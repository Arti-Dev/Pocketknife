package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Speedtech extends PocketknifeSubcommand {
    private boolean lines = false;
    /**
     * If this variable is something other than -1, a Runnable is already running.
     * Do not call trackLocations() if this is the case.
     */
    private int taskID = -1;
    // PlayerMoveEvent.getFrom() is not being reliable, so...
    // Honestly this is pretty bad either way
    private final Map<Player, Location> previousLocations = new HashMap<>();
    private final Map<Player, Double> previousAngles = new HashMap<>();
    private final Map<Player, Vector> previousMovementDir = new HashMap<>();


    @Override
    public String getDescription() {
        return "Weird ways to go fast!";
    }

    @Override
    protected void onDisable() {
        stopTracking();
    }

    @Override
    protected void onEnable() {
        trackLocations();
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                sendDescriptionMessage(sender);
                sendSyntaxMessage(sender);
            } else {
                if (args[0].equals("lines")) {
                    lines = !lines;
                    sender.sendMessage(ChatColor.GREEN + "Lines toggled " + Utils.booleanStatus(lines));
                    if (lines) {
                        player.sendMessage(ChatColor.YELLOW + "Speedtech was toggled ON for you");
                        trackLocations();
                        enabled = true;
                        player.sendMessage(ChatColor.GREEN + "GREEN is the direction where you are looking at");
                        player.sendMessage(ChatColor.RED + "RED is the direction where you are moving");
                    }
                } else {
                    sendSyntaxMessage(sender);
                }
                Pocketknife.getInstance().getConfig().set("speedtech", enabled);
                Pocketknife.getInstance().saveConfig();
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
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
    public String getSyntax() {
        return "Usage: /pocketknife Speedtech toggle";
    }

    private void drawLines(Player player, Vector playerDir, Vector movementDir, double angle) {
        playerDir = playerDir.clone();
        movementDir = movementDir.clone();

        // Do not spawn particles for movement if the player is standing still
        boolean ignoreMovement = movementDir.isZero();
        World w = player.getWorld();
        double inbetween = 0.2;
        double distance = 10;

        // Normalize vectors for use
        playerDir.normalize().multiply(inbetween);
        if (!ignoreMovement) movementDir.normalize().multiply(inbetween);

        Location loc1 = player.getLocation();
        Location loc2 = player.getLocation();
        final Particle.DustOptions GREEN = new Particle.DustOptions(Color.GREEN, 1);
        final Particle.DustOptions RED = new Particle.DustOptions(Color.RED, 1);

        for (double covered = 0; covered <= distance; covered += inbetween) {
            w.spawnParticle(Particle.REDSTONE, loc1.add(playerDir), 2, GREEN);
            if (!ignoreMovement) w.spawnParticle(Particle.REDSTONE, loc2.add(movementDir), 2, RED);
            covered += inbetween;
        }

        if (!ignoreMovement) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.YELLOW + "Angle between the two lines is " + String.format("%.2f", angle) +
                            " degrees"));
        }
    }

    private void trackLocations() {
        if (taskID != -1) return;
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Pocketknife.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Obtain last location and current location
                Location oldLoc;
                Location newLoc = player.getLocation();
                if (!previousLocations.containsKey(player)) {
                    previousLocations.put(player, newLoc);
                }
                oldLoc = previousLocations.get(player).clone();

                // Guard statement
                World w = player.getWorld();
                if (newLoc.getWorld() != w || oldLoc.getWorld() != w) continue;

                // Obtain movement and player head directions
                Vector oldMovementDir;
                Vector newMovementDir = newLoc.clone().subtract(oldLoc).toVector().setY(0);
                Vector newPlayerDir = player.getLocation().getDirection().setY(0);
                if (!previousMovementDir.containsKey(player)) {
                    previousMovementDir.put(player, newMovementDir);
                }
                oldMovementDir = previousMovementDir.get(player).clone();

                // Calculate angles
                double oldAngle;
                double newAngle = Utils.calculateAngle(newPlayerDir, newMovementDir);
                if (!previousAngles.containsKey(player)) {
                    previousAngles.put(player, newAngle);
                }
                oldAngle = previousAngles.get(player);

                // Methods that use these values are below:
                if (lines) drawLines(player, newPlayerDir, newMovementDir, newAngle);
                checkForSpeedSlide(player, oldLoc, newLoc, oldAngle, newAngle);
                checkForRevUp(player, oldMovementDir, newMovementDir, newAngle);

                // Operation's over, prepare for the next tick
                // todo the put() method actually returns the old value. may want to experiment with that
                previousAngles.put(player, newAngle);
                previousLocations.put(player, newLoc);
                previousMovementDir.put(player, newMovementDir);
            }
        }, 0, 2);

        taskID = task.getTaskId();
    }

    private void stopTracking() {
        if (taskID == -1) return;
        Bukkit.getScheduler().cancelTask(taskID);
        taskID = -1;
        previousAngles.clear();
        previousLocations.clear();
        previousMovementDir.clear();
    }

    private final Map<Player, Float> initialSpeeds = new HashMap<>();
    private final Map<Player, Integer> speedBonuses = new HashMap<>();
    private final Map<Player, Integer> stopTimer = new HashMap<>();
    private void checkForSpeedSlide(Player player, Location oldLoc, Location newLoc, double oldAngle, double newAngle) {
        /* Conditions to gain speed:
        - Player must be sprinting
        - Angle must deviate at least 10 degrees from its previous measurement (previous tick)
        - Angle must be between 10 and 35 degrees

        Speed will build up linearly at a rate of 0.007.
        (Default speed is 0.2, max speed is 1)
        A successful tick adds 1 to the speed variable.
        Players need at least 5 on this meter before gaining any speed.

        Speed will reset to its initial value if:
        - Player instantly comes to a stop (previous location == current location for more than 3 ticks)
        This is not lag-friendly, but that's okay.
        */

        oldLoc = oldLoc.clone();
        newLoc = newLoc.clone();

        final double thresholdMin = 10;
        final double thresholdMax = 35;
        final double deviate = 10;
        final double rate = 0.007;
        final double stopTicks = 2;
        final int requiredProcs = 5;

        // Check if player meets conditions to gain speed
        if (player.isSprinting() && Math.abs(oldAngle - newAngle) >= deviate
                && thresholdMin <= newAngle && newAngle <= thresholdMax) {

            // Add anticipatedSpeed bonus
            if (!speedBonuses.containsKey(player)) {
                initialSpeeds.put(player, player.getWalkSpeed());
                speedBonuses.put(player, 1);
            } else {
                speedBonuses.put(player, speedBonuses.get(player) + 1);
            }

            int bonus = speedBonuses.get(player);
            float initialSpeed = initialSpeeds.get(player);
            float anticipatedSpeed;

            // Calculate walk anticipatedSpeed
            if (bonus > requiredProcs) {
                anticipatedSpeed = (float) (initialSpeed + (rate * (bonus - 5)));
            } else {
                // do not apply bonuses yet
                anticipatedSpeed = initialSpeed;
            }

            // edge case where we might be actually decreasing anticipated speed
            if (anticipatedSpeed < initialSpeed) anticipatedSpeed = initialSpeed;
            if (anticipatedSpeed > 1) anticipatedSpeed = 1;

            player.setWalkSpeed(anticipatedSpeed);

            // Lines debug message
            if (lines) player.sendMessage("Speeding up to " + anticipatedSpeed);
            return;
        }

        // If the condition above wasn't met, check if the player should lose their speed
        if (speedBonuses.containsKey(player)) {
            if (oldLoc.equals(newLoc)) {
                stopTimer.put(player, stopTimer.get(player) + 1);
            } else {
                // Reset stop timer
                stopTimer.put(player, 0);
            }
            // TODO This stopping system doesn't work if the player is very slowly sliding against some blocks.
            if (stopTimer.get(player) >= stopTicks) {
                speedBonuses.remove(player);
                player.setWalkSpeed(initialSpeeds.get(player));
                initialSpeeds.remove(player);
                // Lines debug message
                if (lines) player.sendMessage("Speed reset");
            }
        }

    }


    private final HashMap<Player, Integer> stacks = new HashMap<>();
    private final HashMap<Player, Double> initialHeight = new HashMap<>();
    private void checkForRevUp(Player player, Vector oldMovementDir, Vector newMovementDir, double angle) {
        /* The angle between the last movement direction should be close to 180 degrees, allow for 3 degree leeway
        The angle between the movement and player direction should be 90 degrees, 3 degree leeway

        Meeting these conditions any tick grants one stack.

        You can have a maximum of 12 stacks.
        todo If you haven't gained a stack in 10 ticks, you lose two stacks.

        Depending on how many stacks you have, pressing jump will shoot you forward with speed.
        Moving forward will also trigger this - check by checking if the angle is below 46 deg
         */
        // todo we need a better name for just "angle"
        double leeway = 10;
        final double targetMovementAngle = 180;
        final double targetAngle = 90;
        final int maxStacks = 12;

        double movementAngle = Utils.calculateAngle(oldMovementDir, newMovementDir);
        // Adding stacks
        if (Math.abs(targetMovementAngle-movementAngle) <= leeway &&
                Math.abs(targetAngle-angle) <= leeway) {
            if (!stacks.containsKey(player)) {
                stacks.put(player, 0);
                initialHeight.put(player, player.getLocation().getY());
            }
            stacks.put(player, Math.min(stacks.get(player) + 1, maxStacks));
            int stackCount = stacks.get(player);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1,
                    (float) Math.pow(2, (float) stackCount/12F));
        // Using stacks
        } else if (stacks.containsKey(player)) {
            if (/*angle <= 46 || */player.getLocation().getY() > initialHeight.get(player)) {
                Vector dir = player.getLocation().getDirection().setY(0).normalize();
                int stackCount = stacks.get(player);
                player.setVelocity(dir.multiply(3 + stacks.get(player) * 0.4).setY(0.2));
                player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1,
                        (float) Math.pow(2, (float) stackCount/12F));
                stacks.remove(player);
                initialHeight.remove(player);
            }
        }
    }

}
