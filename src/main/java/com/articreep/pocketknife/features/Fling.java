package com.articreep.pocketknife.features;

import com.articreep.pocketknife.PocketknifeSubcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class Fling extends PocketknifeSubcommand {

    @Override
    public String getDescription() {
        return "Fling you in the direction of where you're looking.";
    }

    @Override
    protected void onDisable() {
        // nothing
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 1) {
            double magnitude = Double.parseDouble(args[0]);
            player.setVelocity(player.getLocation().getDirection().multiply(magnitude));
            player.sendMessage(ChatColor.DARK_GREEN + "Flung you with a magnitude of " + magnitude);
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
        return "/pocketknife Fling <magnitude> or /pocketknife Fling <x> <y> <z>";
    }
}