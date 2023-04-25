package com.articreep.pocketknife;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class SendUp implements PocketknifeCommand {

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) { //Sender only typed '/sendup' and nothing else
                ((Player) sender).setVelocity(new Vector(0, 5, 0));
                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Up you go!");
                ((Player) sender).getWorld().strikeLightning(((Player) sender).getLocation());
            } else {
                // This takes multiple arguments and players!
                for (int i = 0; i < args.length; i++) {
                    Player player = Bukkit.getPlayer(args[i]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "You sent an invalid player!");
                        continue;
                    }
                    player.setVelocity(new Vector(0, 5, 0));
                    player.getWorld().strikeLightningEffect(player.getLocation());
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1, 1);
                    player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Up you go!");

                }
            }
            return true;
        }
        Bukkit.getLogger().severe("Only players can run this command!");
        return true;

    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // todo ehh we can tabcomplete laterrrr
        return null;
    }

}
