package com.articreep.pocketknife.features;

import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendUp extends PocketknifeSubcommand {

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) { //Sender only typed '/sendup' and nothing else
                ((Player) sender).setVelocity(new Vector(0, 5, 0));
                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Up you go!");
                ((Player) sender).getWorld().strikeLightning(((Player) sender).getLocation());
            } else {
                // This takes multiple arguments and players!
                for (String arg : args) {
                    Player player = Bukkit.getPlayer(arg);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "You sent an invalid player!");
                        continue;
                    }
                    player.setVelocity(new Vector(0, 5, 0));
                    player.getWorld().strikeLightningEffect(player.getLocation());
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10, 1);
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
        final List<String> onlinePlayerNames = new ArrayList<>();
        List<String> specifiedPlayerNames;
        final List<String> completions = new ArrayList<>();

        if (args.length >= 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                onlinePlayerNames.add(player.getName());
            }

            specifiedPlayerNames = Arrays.stream(args).toList();

            Utils.removeAllIgnoreCase(onlinePlayerNames, specifiedPlayerNames);

            StringUtil.copyPartialMatches(args[args.length - 1], onlinePlayerNames, completions);
        }

        return completions;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife SendUp <player> <player>...";
    }

    @Override
    public String getDescription() {
        return "Launches the targeted player into the sky.";
    }
}
