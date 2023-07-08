package com.articreep.pocketknife;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PingRune extends PocketknifeSubcommand implements PocketknifeFeature, Listener, PocketknifeConfigurable {
    private boolean enabled = false;
    @Override
    public String getDescription() {
        return "Runes are cosmetic items that can be applied to items on Hypixel Skyblock. " +
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
            Pocketknife.getInstance().getConfig().set("pingrune", enabled);
            Pocketknife.getInstance().saveConfig();
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

                Location loc = victim.getLocation().add(0, 0.5, 0);
                offsetLocation(loc, player.getLocation());
                TextDisplay display = (TextDisplay) w.spawnEntity(loc, EntityType.TEXT_DISPLAY);
                display.setText(color + String.valueOf(ping) + "ms");
                setTextRotation(display, player.getLocation());

                new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if (i >= 40) {
                            display.remove();
                            this.cancel();
                        }
                        setTextRotation(display, player.getLocation());
                        i++;

                    }
                }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
            }
        }
    }

    private static void offsetLocation(Location textLoc, Location playerLoc) {
        // TODO We could do some linear algebra logic, but not right now

        Location primaryLoc = textLoc.clone();
        Vector diff = primaryLoc.subtract(playerLoc).toVector();
        // Create a random vector to be applied to the original effect location
        Vector randomVec = Utils.randomVector(0.7);
        Vector resultant = diff.clone().add(randomVec);

        // Ensure that the resulting location will be closer to the player and not further away.
        // If not just subtract the random vector rather than add it.
        if (resultant.lengthSquared() < diff.lengthSquared()) {
            textLoc.add(randomVec);
        } else {
            textLoc.subtract(randomVec);
        }
    }



    private static void setTextRotation(TextDisplay display, Location damagerLoc) {
        display.setRotation(Utils.invertYaw(damagerLoc.getYaw()), Utils.invertPitch(damagerLoc.getPitch()));
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        enabled = config.getBoolean("pingrune");
        config.set("pingrune", enabled);
    }
}
