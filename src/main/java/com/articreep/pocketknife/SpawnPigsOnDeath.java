package com.articreep.pocketknife;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class SpawnPigsOnDeath implements Listener, PocketknifeSubcommand {
	boolean enabled;
	Pocketknife plugin;
	public SpawnPigsOnDeath() {
		plugin = Pocketknife.getInstance();
		enabled = plugin.getConfig().getBoolean("spawnpigsondeath");
	}
	@Override
	public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO Add toggle
		if (sender instanceof Player) {
			sender.sendMessage("All this feature does is spawn ten pigs when someone dies.");
			return true;
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}

	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
		if (!enabled) return;
    	Player p = event.getEntity();
    	World w = p.getWorld();
    	for (int i = 0; i<10; i++) {
    		w.spawnEntity(event.getEntity().getLocation(), EntityType.PIG);
    	}
   	
    	new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.broadcastMessage("Ten pigs spawned where they died!");
			}
        }.runTaskLater(Pocketknife.getInstance(), 1);
    }
}
