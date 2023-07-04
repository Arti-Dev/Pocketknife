package com.articreep.pocketknife;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class SpawnPigsOnDeath extends PocketknifeSubcommand implements Listener, PocketknifeConfigurable {
	boolean enabled;
	Pocketknife plugin;
	public SpawnPigsOnDeath() {
		plugin = Pocketknife.getInstance();
		enabled = plugin.getConfig().getBoolean("spawnpigsondeath");
	}
	@Override
	public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 0) {
				sendDescriptionMessage(sender);
				sendSyntaxMessage(sender);
			} else {
				if (args[0].equalsIgnoreCase("toggle")) {
					enabled = !enabled;
					sender.sendMessage(ChatColor.GREEN + "SpawnPigsOnDeath toggled " + Utils.booleanStatus(enabled));
				} else {
					sendSyntaxMessage(sender);
				}
				plugin.getConfig().set("spawnpigsondeath", enabled);
				plugin.saveConfig();
			}
			return true;
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();
		if (args.length == 1) {
			ArrayList<String> strings = new ArrayList<>();
			strings.add("on");
			strings.add("off");
			StringUtil.copyPartialMatches(args[0], strings, completions);
		}
		return completions;
	}

	@Override
	String getSyntax() {
		return "Usage: /pocketknife SpawnPigsOnDeath <on/off>";
	}

	@Override
	public void loadConfig(FileConfiguration config) {
		enabled = config.getBoolean("spawnpigsondeath");
		config.set("spawnpigsondeath", enabled);
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

	@Override
	public String getDescription() {
		return "All this feature does is spawn ten pigs when someone dies.";
	}
}
