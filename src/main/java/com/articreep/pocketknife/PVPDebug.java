package com.articreep.pocketknife;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;

public class PVPDebug extends PocketknifeSubcommand implements Listener {
    Pocketknife plugin;
    private boolean enabled = false;
    private int tickCount = 0;
    private BukkitTask task;
    private HashMap<Player, Integer> ticksSinceLastHit = new HashMap<>();

    public PVPDebug() {
        plugin = Pocketknife.getInstance();
    }

    @Override
    public String getDescription() {
        return "Debug command for some PVP related things";
    }

    @Override
    boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        enabled = (!enabled);
        if (enabled) {
            // Start runnable
            task = Bukkit.getScheduler().runTaskTimer(plugin, () -> tickCount += 1,
        0, 1);
            sender.sendMessage(ChatColor.GREEN + "PVPDebug enabled");
        } else {
            task.cancel();
            task = null;
            ticksSinceLastHit.clear();
            sender.sendMessage(ChatColor.RED + "PVPDebug disabled");
        }
        return true;
    }

    @Override
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    String getSyntax() {
        return "Usage: /pocketknife PVPDebug";
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!enabled) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Player damager;

        // Melee hit?
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
            // Ranged hit?
        } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            damager = (Player) ((Projectile) event.getDamager()).getShooter();
            // None of those? Don't care, then
        } else {
            return;
        }

        // Quick debug
        damager.sendMessage("You dealt " + event.getFinalDamage() + " to " + victim.getName());
        victim.sendMessage(damager.getName() + " dealt " + event.getFinalDamage() + " to you");
        // Tick testing
        if (ticksSinceLastHit.containsKey(victim)) {
            damager.sendMessage(victim.getName() + " was last hit " + (tickCount - ticksSinceLastHit.get(victim)) + " ticks ago");
            ticksSinceLastHit.put(victim, tickCount);
        }
        ticksSinceLastHit.put(victim, tickCount);
        damager.sendMessage("Your victim now has " + victim.getNoDamageTicks() + " no damage ticks.");
    }
}
