package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeFeature;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnPigsOnDeath extends PocketknifeFeature implements Listener {

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

	@Override
	public String getDescription() {
		return "All this feature does is spawn ten pigs when someone dies.";
	}

	@Override
	protected void onDisable() {
		// nothing
	}
}
