package com.articreep.pocketknife;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public class Geyser extends PocketknifeSubcommand implements Listener {
    private static Location hardCodedLocation;
    private static World world;
    private Set<Block> noFlowBlocks = new HashSet<>();

    public Geyser() {
        world = Bukkit.getWorld("flat");
        hardCodedLocation = new Location(world, -278, -61, -314);
    }

    @EventHandler
    public void onFlow(BlockFromToEvent event) {
        if (noFlowBlocks.contains(event.getBlock())) event.setCancelled(true);
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        List<BlockDisplay> blockDisplays = new ArrayList<>();
        BlockDisplay fence = (BlockDisplay) world.spawnEntity(hardCodedLocation.clone().add(0, 1, 0), EntityType.BLOCK_DISPLAY);
        fence.setBlock(Material.CRIMSON_FENCE.createBlockData());
        BlockDisplay hopper = (BlockDisplay) world.spawnEntity(hardCodedLocation.clone().add(0, 2, 0), EntityType.BLOCK_DISPLAY);
        hopper.setBlock(Material.HOPPER.createBlockData());
        BlockDisplay fire = (BlockDisplay) world.spawnEntity(hardCodedLocation.clone().add(0, 3, 0), EntityType.BLOCK_DISPLAY);
        fire.setBlock(Material.AIR.createBlockData());

        blockDisplays.add(fence);
        blockDisplays.add(hopper);
        blockDisplays.add(fire);

        for (BlockDisplay blockDisplay : blockDisplays) {
            blockDisplay.setTeleportDuration(1);
            blockDisplay.setInterpolationDuration(1);
        }

        new BukkitRunnable() {
            int ticks = 0;
            double acceleration = 10.0;
            double gravity = -15.0;
            Deque<Block> waterStack = new ArrayDeque<>();
            double torchVelocity = 10;
            double torchOffset = -3;

            int waterDespawnPeriod = 5;

            int phase = 0;
            @Override
            public void run() {
                /*
                A few phases:
                - Water is actively propelling the fire torch up at some acceleration
                - Water stops rising and starts to go back down at some linear speed
                - Torch continues to rise and is magically lit at the peak
                - Torch despawns when it falls below its initial spawn location
                 */
                if (phase == 0) {
                    torchVelocity += acceleration / 20;
                } else  {
                    if (torchVelocity > 0 && torchVelocity < Math.abs(gravity / 20)) {
                        lightTorch(fire, torchOffset);
                    }
                    torchVelocity += gravity / 20;
                }

                if (phase == 0 && ticks > 10) {
                    phase = 1;
                } else if (phase == 1 && ticks > 15) {
                    phase = 2;
                }

                torchOffset += torchVelocity / 20;

                for (BlockDisplay blockDisplay : blockDisplays) {
                    blockDisplay.setInterpolationDelay(0);
                    blockDisplay.setTransformation(new Transformation(
                            new Vector3f(0, (float) torchOffset, 0),
                            new AxisAngle4f(0, 0, 0, 1), new Vector3f(1, 1, 1),
                            new AxisAngle4f(0, 0, 0, 1)));
                }

                if (phase <= 1) {
                    // Set water blocks until deque size is torchOffset
                    // Start at 1 above the reference location
                    while (waterStack.size() < torchOffset) {
                        Block block = hardCodedLocation.clone().add(0, waterStack.size() + 1, 0).getBlock();
                        if (block.getType() == Material.AIR || block.getType() == Material.WATER || block.getType() == Material.BUBBLE_COLUMN) {
                            block.setType(Material.WATER);
                            noFlowBlocks.add(block);
                            waterStack.push(block);
                        } else {
                            phase = 2;
                            break;
                        }
                    }
                } else if (phase == 2) {
                    // Remove water blocks until deque is empty
                    if (ticks % waterDespawnPeriod == 0 && !waterStack.isEmpty()) {
                        Block block = waterStack.poll();
                        noFlowBlocks.remove(block);
                        block.setType(Material.AIR);
                    }
                }

                if (torchOffset < -5) {
                    for (BlockDisplay blockDisplay : blockDisplays) {
                        blockDisplay.remove();
                    }
                    cancel();
                }

                ticks++;

            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
        return true;
    }

    public void lightTorch(BlockDisplay display, double offset) {
        Location location = display.getLocation().add(0, offset, 0);
        display.setBlock(Material.SOUL_FIRE.createBlockData());
        display.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, display.getLocation().add(0.5, offset + 0.5, 0.5), 20, 0, 0, 0, 0.3);
        display.getWorld().playSound(display.getLocation().add(0, offset, 0), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
        location.getBlock().setType(Material.LIGHT);
        Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () ->
                location.getBlock().setType(Material.AIR), 20 * 3);

    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }

    @Override
    public String getSyntax() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected void onDisable() {

    }
}
