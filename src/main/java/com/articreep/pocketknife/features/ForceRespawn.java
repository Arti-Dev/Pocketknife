package com.articreep.pocketknife.features;

import com.articreep.pocketknife.PocketknifeSubcommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ForceRespawn extends PocketknifeSubcommand {
    public ForceRespawn() {
        this.setConsoleCanUse(true);
    }
    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendSyntaxMessage(sender);
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Invalid player!");
            return true;
        }
        target.spigot().respawn();
        sender.sendMessage(ChatColor.GREEN + "Target has respawned or is already alive!");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final ArrayList<String> strings = new ArrayList<>();
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                strings.add(p.getName());
            }
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife ForceRespawn <player>";
    }

    @Override
    public String getDescription() {
        return "Forcefully respawns any player that is on the \"You Died\" screen.";
    }

    @Override
    protected void onDisable() {
        // nothing
    }
}
