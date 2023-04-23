package com.articreep.pocketknife;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class Main extends JavaPlugin implements Listener {

    // why would you load this plugin
    public void onEnable() {
        getCommand("funnypickaxe").setExecutor(new FunnyPickaxe());
        getServer().getPluginManager().registerEvents(new FunnyPickaxe(), this);
        getServer().getPluginManager().registerEvents(new DiamondHit(), this);
        getLogger().info(ChatColor.GOLD + "Testing plugin enabled");
    }
    // this is the better method.
    public void onDisable() {
        getLogger().info(ChatColor.GOLD + "Testing plugin disabled");
    }


}
