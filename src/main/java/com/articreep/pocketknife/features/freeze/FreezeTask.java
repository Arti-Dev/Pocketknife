package com.articreep.pocketknife.features.freeze;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.Utils;
import com.articreep.pocketknife.features.Parametric;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class FreezeTask {
    private int ticks;
    private final Player player;
    private final HashMap<Attribute, Double> originalValues;
    private BukkitTask task = null;
    private final HashSet<BlockDisplay> iceEntities = new HashSet<>();
    private final HashSet<Block> iceBlocks = new HashSet<>();

    public FreezeTask(Player player, HashMap<Attribute, Double> originalValues, int ticks) {
        this.player = player;
        this.originalValues = originalValues;
        this.ticks = ticks;
    }

    public void run() {
        spawnSnow(player);
        spawnIceDisplay();
        Utils.teleportToCenterOfBlock(player);
        task = new BukkitRunnable() {
            public void run() {
                if (ticks > 0) {
                    player.setFreezeTicks(ticks + 140);
                } else {
                    despawn();
                    Freeze.resetAttributes(player, originalValues);
                    Freeze.frozenPlayers.remove(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 5, 0.5f);
                    player.getWorld().spawnParticle(Particle.BLOCK,
                            player.getLocation().add(0.0, 1.0, 0.0), 40,
                            0.5, 1.0, 0.5, 0.5, Material.ICE.createBlockData());
                    this.cancel();
                }
                ticks--;
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

    public void decreaseTicks(int amount) {
        ticks -= amount;
    }

    private void spawnSnow(Player player) {
        Location reference = player.getLocation().getBlock().getLocation();
        // Edges only
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location location = reference.clone().add(x, 0, z);
                // xor
                if ((x == 0 || z == 0) && (!(x == 0 && z == 0))) {
                    Block block = location.getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.SNOW);
                        Snow snow = (Snow) block.getBlockData();
                        snow.setLayers(2);
                        block.setBlockData(snow);
                        iceBlocks.add(block);
                    }
                }
            }
        }

        // Display entities
        BlockDisplay blockDisplay = (BlockDisplay) player.getWorld().spawnEntity(reference, EntityType.BLOCK_DISPLAY);
        blockDisplay.setBlock(Material.SNOW.createBlockData());
        iceEntities.add(blockDisplay);
    }

    private void spawnIceDisplay() {
        Location reference = player.getLocation();
        Random random = new Random();
        HashMap<BlockDisplay, Double> displays = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            double offset = i * 0.4;
            BlockDisplay display = Parametric.iceDisplay(reference.clone().add(0, offset, 0)
                    , Material.ICE, random.nextFloat(0.8f, 1f));
            display.setRotation(random.nextInt(0, 23), random.nextInt(-10, 10));
            iceEntities.add(display);
            displays.put(display, offset);
        }
        Bukkit.getScheduler().runTaskTimer(Pocketknife.getInstance(), () -> {
            Iterator<BlockDisplay> it = displays.keySet().iterator();
            while (it.hasNext()) {
                BlockDisplay display = it.next();
                if (!display.isDead()) {
                    Location loc = player.getLocation().add(0, displays.get(display), 0);
                    loc.setYaw(display.getLocation().getYaw());
                    loc.setPitch(display.getLocation().getPitch());
                    display.teleport(loc);
                } else {
                    it.remove();
                    if (displays.isEmpty()) {
                        cancel();
                    }
                }
            }
        }, 0, 1);
    }

    // prototype
    private void spawnIce(Player player) {
        Location reference = player.getLocation().getBlock().getLocation();

        // 3x3 base
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location location = reference.clone().add(x, 0, z);
                Block block = location.getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.ICE);
                    iceBlocks.add(block);
                }
            }
        }

        // Edges only
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location location = reference.clone().add(x, 1, z);
                // xor
                if ((x == 0 || z == 0) && (!(x == 0 && z == 0))) {
                    Block block = location.getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.ICE);
                        iceBlocks.add(block);
                    }
                }
            }
        }

        // Display entities
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location location = reference.clone().add(x, 2, z);
                // xor
                if ((x == 0 || z == 0) && (!(x == 0 && z == 0))) {
                    BlockDisplay blockDisplay = (BlockDisplay) player.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
                    blockDisplay.setBlock(Material.ICE.createBlockData());
                    iceEntities.add(blockDisplay);
                }
            }
        }

        // Last two layers
        for (int y = 3; y <= 4; y++) {
            Location location = reference.clone().add(0, y, 0);
            Block block = location.getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.ICE);
                iceBlocks.add(block);
            }
        }


    }

    private void despawn() {
        for (Block block : iceBlocks) {
            if (block.getType() == Material.ICE || block.getType() == Material.SNOW)
                block.setType(Material.AIR);
        }
        iceBlocks.clear();
        for (BlockDisplay blockDisplay : iceEntities) {
            blockDisplay.remove();
        }
        iceEntities.clear();
    }

}
