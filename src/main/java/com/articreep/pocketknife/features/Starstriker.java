package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeSubcommand;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;

/**
 * This was used for the Pit Day 2023 video! <a href="https://www.youtube.com/watch?v=QZCXWbQvQ8g">...</a>
 */
public class Starstriker extends PocketknifeSubcommand {
    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        World world = player.getWorld();
        if (!world.getName().equalsIgnoreCase("Seasons")) {
            sender.sendMessage(ChatColor.RED + "Please navigate to the \"Seasons\" world if it exists.");
            return true;
        }

        player.playSound(player.getLocation(), Sound.MUSIC_DISC_MELLOHI, 1000, 1);
        Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> {
            player.teleport(new Location(world, 0.5, 116, -5.5));
        }, 13);
        new BukkitRunnable() {
            int i = 0;
            int diff = 2;
            final LinkedList<Block> list = new LinkedList<>();

            @Override
            public void run() {
                if (!player.isOnline()) this.cancel();

                diff = i % 2 == 0 ? 2 : -2;
                Block b = world.getBlockAt(player.getLocation().add(diff, 0, 0));

                if (b.getType() != Material.AIR) return;
                b.setType(Material.NOTE_BLOCK);
                NoteBlock noteblock = (NoteBlock) b.getBlockData();
                player.playNote(b.getLocation(), noteblock.getInstrument(), noteblock.getNote());
                // todo particles no longer appear, must do ourselves
                list.add(b);

                if (i >= 32) {
                    for (Block block : list) {
                        block.setType(Material.AIR);
                    }
                    player.stopSound(Sound.MUSIC_DISC_MELLOHI);
                    this.cancel();
                }
                i++;
            }
        }.runTaskTimer(Pocketknife.getInstance(), 48, 3);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife Starstriker";
    }

    @Override
    public String getDescription() {
        return "Special effect used in the Pit Day 2023 video.";
    }
}
