package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Parametric extends PocketknifeSubcommand {

    @Override
    public String getDescription() {
        return "parametric equations";
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            int id = Utils.parseInt(args[0]);
            int seconds = Utils.parseInt(args[1]);
            if (id == -1 || seconds == -1) {
                sender.sendMessage(getSyntax());
                return true;
            }
            if (id > 0 && id <= 3) {
                parametric(id, seconds * 20, (Player) sender);
                sender.sendMessage("Now displaying");
            } else {
                sender.sendMessage(getSyntax());
            }
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("bouncyice")) {
            bouncyIce((Player) sender);
            sender.sendMessage("Now displaying ice");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("continousbouncyice")) {
            continuousbouncyIce((Player) sender, 1000);
            sender.sendMessage("Now displaying continuous ice");
        } else {
            sendSyntaxMessage(sender);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife parametric <id> <seconds>";
    }

    private void parametric(int id, int ticks, Player player) {
        new BukkitRunnable() {
            int elapsed = 0;
            double t = 0.0;
            @Override
            public void run() {
                elapsed++;
                t += (double) 1/20;
                if (t > 2 * Math.PI) t -= 2 * Math.PI;

                if (elapsed > ticks) {
                    cancel();
                    return;
                }
                player.getWorld().spawnParticle(Particle.DUST, player.getLocation().
                        add(0, 1, 0).
                        add(parametricCoordinates(id, t)), 1,
                        new Particle.DustOptions(Color.RED, 0.7F));
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }

    private Vector parametricCoordinates(int id, double t) {
        if (id == 1) {
            double x = Math.sin(3*t) * Math.sin(t);
            double y = Math.sin(3*t) * Math.sin(t) * Math.cos(t);
            double z = Math.sin(3*t) * Math.cos(t);
            return new Vector(x, y, z);
        } else if (id == 2) {
            double x = 2 * Math.sin(3*t) * Math.sin(t);
            double y = 2 * Math.sin(3*t) * Math.sin(t) * Math.cos(t);
            double z = 2 * Math.sin(3*t) * Math.cos(t);
            return new Vector(x, y, z);
        } else if (id == 3) {
            double x = 3 * Math.cos(4*t) * Math.sin(t);
            double y = 3 * Math.cos(4*t) * Math.sin(t) * Math.cos(t);
            double z = 3 * Math.cos(4*t) * Math.cos(t);
            return new Vector(x, y, z);
        } else {
            return new Vector(0, 0, 0);
        }
    }

    private void bouncyIce(Player player) {
        Location initialLocation = player.getLocation().add(0, 1, 0);
        Vector direction = player.getLocation().getDirection().setY(0);
        int maxTicks = 100;

        ArrayList<BlockDisplay> trail = new ArrayList<>();
        BlockDisplay display = iceDisplay(initialLocation, Material.SNOW_BLOCK, 0.5f);
        new BukkitRunnable() {
            int elapsed = 0;
            double t = Math.PI / 2;
            @Override
            public void run() {
                elapsed++;
                t += 1;

                if (elapsed < 60 && elapsed % 2 == 0) {
                    trail.add(iceDisplay(initialLocation, Material.ICE, 0.5f));
                }
                double x = t - Math.sin(t);
                double y = 0.25 - Math.cos(t)/4;
                Random random = new Random();
                display.teleport(initialLocation.clone().add(0, y, 0)
                        .add(direction.clone().multiply(x)));
                display.setRotation(random.nextInt(0, 360), random.nextInt(-90, 90));

                double offset = 2;
                for (BlockDisplay block : trail) {
                    x = (t - offset) - Math.sin(t - offset);
                    y = 1 - Math.cos(t - offset);
                    block.teleport(initialLocation.clone().add(0, y, 0)
                            .add(direction.clone().multiply(x)));
                    block.setRotation(random.nextInt(0, 360), random.nextInt(-90, 90));
                    offset += 2;
                }

                if (elapsed > maxTicks) {
                    display.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, display.getLocation(), 75, 0, 0, 1, 75);
                    display.remove();
                    for (BlockDisplay block : trail) {
                        block.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, block.getLocation(), 75, 0, 0, 1, 75);
                        block.remove();
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }

    private void continuousbouncyIce(Player player, int maxTicks) {
        Location initialLocation = player.getLocation().add(0, 1, 0);
        Vector direction = player.getLocation().getDirection().setY(0);

        LinkedList<BlockDisplay> trail = new LinkedList<>();
        new BukkitRunnable() {
            int elapsed = 0;
            double t = Math.PI / 2;
            @Override
            public void run() {
                elapsed++;
                t += 1;

                if (elapsed % 2 == 0) {
                    trail.add(iceDisplay(initialLocation, Material.BLUE_ICE,1f));
                }

                if (elapsed % 2 == 0 && trail.size() > 200) {
                    BlockDisplay toRemove = trail.removeFirst();
                    toRemove.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, toRemove.getLocation(), 75, 0, 0, 1, 75);
                    toRemove.remove();
                    t -= 2;
                }

                Random random = new Random();
                double offset = 2;
                for (BlockDisplay block : trail) {
                    double x = (t - offset) - Math.sin(t - offset);
                    double y = 1 - Math.cos(t - offset);
                    block.teleport(initialLocation.clone().add(0, y, 0)
                            .add(direction.clone().multiply(x)));
                    block.setRotation(random.nextInt(0, 360), random.nextInt(-90, 90));
                    offset += 2;
                }

                if (elapsed > maxTicks) {
                    for (BlockDisplay block : trail) {
                        block.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, block.getLocation(), 75, 0, 0, 1, 75);
                        block.remove();
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }

    public static BlockDisplay iceDisplay(Location location, Material material, float size) {
        BlockDisplay display = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(material.createBlockData());
        display.setTeleportDuration(1);
        display.setInterpolationDuration(10);
        display.setTransformation(new Transformation(new Vector3f(-size/2f, -size/2f, -size/2f),
                new AxisAngle4f(0, 0, 0, 1),
                new Vector3f(size, size, size),
                new AxisAngle4f(0, 0, 0, 1)));
        return display;
    }
}
