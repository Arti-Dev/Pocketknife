package com.articreep.pocketknife;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PingRune extends PocketknifeSubcommand implements PocketknifeFeature, Listener {
    private boolean enabled = false;
    @Override
    public String getDescription() {
        return "Runes are cosmetic items that can be applied to items on Hypixel Skyblock." +
                "Ping is the temporal latency between your client and the server.";
    }

    @Override
    boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendDescriptionMessage(sender);
            sendSyntaxMessage(sender);
        } else {
            if (args[0].equals("toggle")) {
                enabled = !enabled;
                sender.sendMessage(ChatColor.GREEN + "PingRune toggled " + Utils.booleanStatus(enabled));
            } else {
                sendSyntaxMessage(sender);
            }
        }
        return true;
    }

    @Override
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add("toggle");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @Override
    String getSyntax() {
        return "Usage: /pocketknife PingRune toggle";
    }

    @EventHandler
    public void onEntityKill(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof Damageable victim) {
            // Did this result in a kill?
            if (victim.getHealth() - event.getFinalDamage() <= 0) {
                World w = player.getWorld();
                int ping = player.getPing();

                ChatColor color;
                if (ping < 100) color = ChatColor.GREEN;
                else if (ping < 200) color = ChatColor.YELLOW;
                else color = ChatColor.RED;

                Location loc = victim.getLocation();
                TextDisplay display = (TextDisplay) w.spawnEntity(loc, EntityType.TEXT_DISPLAY);
                display.setText(color + String.valueOf(ping) + "ms");

                Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), display::remove, 40);
            }


        }


    }
}
