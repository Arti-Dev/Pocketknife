package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeSubcommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Fling extends PocketknifeSubcommand implements Listener {

    @Override
    public String getDescription() {
        return "Fling you in the direction of where you're looking.";
    }

    @Override
    protected void onDisable() {
        // nothing
    }

    Set<Player> slidingPlayers = new HashSet<>();
    @EventHandler
    public void isGliding(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (slidingPlayers.contains(player)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("slide")) {
                slidingPlayers.add(player);
                Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(),
                        () -> slidingPlayers.remove(player), 5);
                player.setGliding(true);
                player.setSprinting(true);
                player.setVelocity(player.getLocation().getDirection().multiply(1.5));
                player.sendMessage(ChatColor.DARK_GREEN + "Try doing this in one-block high gaps!");
                return true;
            } else {
                double magnitude = Double.parseDouble(args[0]);
                player.setVelocity(player.getLocation().getDirection().multiply(magnitude));
                player.sendMessage(ChatColor.DARK_GREEN + "Flung you with a magnitude of " + magnitude);
            }
            return true;
        } else if (args.length == 3) {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            Vector vector = new Vector(x, y, z);
            player.setVelocity(vector);
            player.sendMessage(ChatColor.DARK_GREEN + "Set your velocity to " + x + " " + y + " " + z);
            return true;
        } else {
            sendSyntaxMessage(player);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return """
                /pocketknife Fling <magnitude>
                /pocketknife Fling <x> <y> <z>
                /pocketknife Fling slide""";
    }
}