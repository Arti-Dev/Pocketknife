package com.articreep.pocketknife.features.freeze;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class FreezeTask {
    private int ticks = 160;
    private final Player player;
    private final HashMap<Attribute, Double> originalValues;
    private BukkitTask task = null;
    private final HashSet<BlockDisplay> iceEntities = new HashSet<>();
    private final HashSet<Block> iceBlocks = new HashSet<>();

    public FreezeTask(Player player, HashMap<Attribute, Double> originalValues) {
        this.player = player;
        this.originalValues = originalValues;
    }

    public void run() {
        spawnSnow(player);
        Utils.teleportToCenterOfBlock(player);
        task = new BukkitRunnable() {
            public void run() {
                if (ticks > 0) {
                    player.setFreezeTicks(ticks);
                } else {
                    despawn();
                    Freeze.resetAttributes(player, originalValues);
                    Freeze.frozenPlayers.remove(player);
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
