package com.articreep.pocketknife;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This was used for the Pit Day 2022 video! <a href="https://www.youtube.com/watch?v=JkHlECk1_Ao">...</a>
 */
public class GenesisExplode implements PocketknifeCommand {
    // TODO add a config file disabling this
    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        Location l;
        World w = Bukkit.getWorld("Genesis");
        if (w == null) {
            sender.sendMessage(ChatColor.RED + "Genesis world not found.");
            return true;
        }
        l = new Location(w, 0, 45, 0);

        sender.sendMessage("You may want to rollback the " + ChatColor.RED + "Genesis" + ChatColor.RESET + " world after this explosion!");
        TextComponent msg = new TextComponent(ChatColor.YELLOW + "Maybe run /co rollback t:30s r:100?");
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to run")));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/co rollback t:30s r:100"));
        sender.spigot().sendMessage(msg);

        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i >= 10) this.cancel();
                w.spawnEntity(l, EntityType.PRIMED_TNT);
                i++;
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 4);
        new BukkitRunnable() {

            @Override
            public void run() {
                w.createExplosion(new Location(w, 0, 43, 0), 45);
                w.createExplosion(new Location(w, 22, 43, 0), 16);
                w.createExplosion(new Location(w, -22, 43, 0), 16);
                w.createExplosion(new Location(w, 0, 43, 22), 16);
                w.createExplosion(new Location(w, 0, 43, -22), 16);
                w.createExplosion(new Location(w, 14, 43, -16), 16);
                w.createExplosion(new Location(w, 17, 43, 16), 16);
                w.createExplosion(new Location(w, -17, 43, 15), 16);
                w.createExplosion(new Location(w, -16, 43, -18), 16);
            }
        }.runTaskLater(Pocketknife.getInstance(), 100);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i >= 75) this.cancel();
                Entity e = w.spawnEntity(l, EntityType.PIG);
                e.setVelocity(randomVec());
                i++;
            }
        }.runTaskTimer(Pocketknife.getInstance(), 110, 1);
        return true;
    }

    private static Vector randomVec() {
        return new Vector(ThreadLocalRandom.current().nextDouble(-3, 3), Math.random() * 3,
                ThreadLocalRandom.current().nextDouble(-3, 3));
    }
}
