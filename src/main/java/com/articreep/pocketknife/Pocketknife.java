package com.articreep.pocketknife;

import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Pocketknife extends JavaPlugin implements Listener {
    private static Pocketknife instance;
    // why would you load this plugin
    public void onEnable() {
        instance = this;
        SpawnPigsOnDeath spawnPigsOnDeath = new SpawnPigsOnDeath();
        FunnyPickaxe funnyPickaxe = new FunnyPickaxe();
        GenesisExplode genesisExplode = new GenesisExplode();
        DiamondHit diamondHit = new DiamondHit();

        getCommand("funnypickaxe").setExecutor(funnyPickaxe);
        this.getCommand("genesisexplode").setExecutor(genesisExplode);
        this.getCommand("spawnpigsondeath").setExecutor(spawnPigsOnDeath);
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

}
