package com.articreep.pocketknife;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Pocketknife extends JavaPlugin implements CommandExecutor {
    private static Pocketknife instance;
    // why would you load this plugin
    public void onEnable() {
        instance = this;
        SpawnPigsOnDeath spawnPigsOnDeath = new SpawnPigsOnDeath();
        FunnyPickaxe funnyPickaxe = new FunnyPickaxe();
        GenesisExplode genesisExplode = new GenesisExplode();
        DiamondHit diamondHit = new DiamondHit();

        getCommand("pocketknife").setExecutor(this);
        getServer().getPluginManager().registerEvents(funnyPickaxe, this);
        getServer().getPluginManager().registerEvents(diamondHit, this);
        getServer().getPluginManager().registerEvents(spawnPigsOnDeath, this);
        getLogger().info(ChatColor.GOLD + "Testing plugin enabled");
    }
    // this is the better method.
    public void onDisable() {
        getLogger().info(ChatColor.GOLD + "Testing plugin disabled");
    }

    public static Pocketknife getInstance() {
        return instance;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // TODO tabcomplete this and cleanup
        // TODO probably going to throw this out and just make a list of the classes in my package here
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /pocketknife <feature>");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Get off the console");
            return true;
        }

        boolean success;

        Method m = Utils.getCommandMethod("com.articreep.pocketknife." + args[0], getClassLoader());

        if (m == null) {
            // TODO they won't be case-sensitive later
            sender.sendMessage(ChatColor.RED + "Couldn't find that feature. Features are case-sensitive!");
            return true;
        }

        try {
            success = (boolean) m.invoke(null, sender, command, label, Utils.removeFirstArg(args));
        } catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
            e.printStackTrace();
            success = false;
        }

        if (!success) {
            sender.sendMessage(ChatColor.RED + "Something went wrong.");
        }

        return true;
    }

}
