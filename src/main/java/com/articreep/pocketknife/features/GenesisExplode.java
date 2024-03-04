package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeConfigurable;
import com.articreep.pocketknife.PocketknifeSubcommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This was used for the Pit Day 2022 video! <a href="https://www.youtube.com/watch?v=JkHlECk1_Ao">...</a>
 */
public class GenesisExplode extends PocketknifeSubcommand implements PocketknifeConfigurable {
    private boolean enabled = false;
    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (!enabled) {
            player.sendMessage(ChatColor.RED + "This feature is currently disabled!");
            return true;
        }

        World w = player.getWorld();
        if (!w.getName().equalsIgnoreCase("Genesis")) {
            sender.sendMessage(ChatColor.RED + "Please navigate to the \"Genesis\" world if it exists.");
            return true;
        }
        Location l = new Location(w, 0, 45, 0);

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

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife GenesisExplode";
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        enabled = config.getBoolean("genesisexplode");
        config.set("genesisexplode", enabled);
    }

    private static Vector randomVec() {
        return new Vector(ThreadLocalRandom.current().nextDouble(-3, 3), Math.random() * 3,
                ThreadLocalRandom.current().nextDouble(-3, 3));
    }

    @Override
    public String getDescription() {
        return "Explodes the Genesis map. Used for the Pit Day 2022 video.";
    }

    @Override
    protected void onDisable() {
        // nothing
    }
}
