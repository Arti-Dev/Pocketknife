package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeFeature;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.HashMap;

public class PVPDebug extends PocketknifeFeature implements Listener {
    Pocketknife plugin;
    private int tickCount = 0;
    private BukkitTask task;
    private final HashMap<Player, Integer> ticksSinceLastHit = new HashMap<>();

    public PVPDebug() {
        plugin = Pocketknife.getInstance();
    }

    @Override
    public String getDescription() {
        return "Debug command for some PVP related things";
    }


    @Override
    protected void onEnable() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> tickCount += 1, 0, 1);
    }
    @Override
    protected void onDisable() {
        task.cancel();
        task = null;
        ticksSinceLastHit.clear();
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

        DecimalFormat formatter = new DecimalFormat("#.##");

        // Quick debug
        damager.sendMessage("You dealt " + ChatColor.RED + formatter.format(event.getFinalDamage()) + ChatColor.WHITE + " to " + victim.getName());
        victim.sendMessage(damager.getName() + " dealt " + ChatColor.RED + formatter.format(event.getFinalDamage()) + ChatColor.WHITE + " to you");
        // Tick testing
        if (ticksSinceLastHit.containsKey(victim)) {
            damager.sendMessage(victim.getName() + " was last hit " + (tickCount - ticksSinceLastHit.get(victim)) + " ticks ago");
            ticksSinceLastHit.put(victim, tickCount);
        }
        ticksSinceLastHit.put(victim, tickCount);
        damager.sendMessage("Your victim has " + victim.getNoDamageTicks() + " no damage ticks.");
        Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> damager.sendMessage("Your victim has " + victim.getNoDamageTicks()
                + " no damage ticks 1 tick later"), 1);

        // Reach
        Location damagerLoc = damager.getLocation();
        Location victimLoc = victim.getLocation();
        double distance = damagerLoc.distance(victimLoc);
        damager.sendMessage("You hit " + victim.getName() + " from " + ChatColor.GREEN + formatter.format(distance) +
                ChatColor.WHITE + " blocks away");
        victim.sendMessage(damager.getName() + " hit you from " + ChatColor.GREEN + formatter.format(distance) +
                ChatColor.WHITE + " blocks away");
    }
}
