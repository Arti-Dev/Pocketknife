package com.articreep.pocketknife;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Pocketknife extends JavaPlugin implements CommandExecutor, TabCompleter {
    private static Pocketknife instance;
    private static Reflections reflections;
    private static final HashMap<String, Class<?>> commandClassNameMap = new HashMap<>();
    public void onEnable() {
        instance = this;

        // i don't know how it works but it works
        // pulled this from here: https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.articreep.pocketknife"))));

        // Store each and every command class name in a set
        // This will not change while the plugin is loaded so it's fine
        for (Class<?> clazz : reflections.getSubTypesOf(Object.class)) {
            if (Utils.hasCommandMethod(clazz)) {
                commandClassNameMap.put(clazz.getSimpleName(), clazz);
            }
        }

        // create instances for use later
        SpawnPigsOnDeath spawnPigsOnDeath = new SpawnPigsOnDeath();
        FunnyPickaxe funnyPickaxe = new FunnyPickaxe();
        DiamondHit diamondHit = new DiamondHit();

        // /pocketknife is the umbrella command for all features of this plugin.
        getCommand("pocketknife").setExecutor(this);
        getCommand("pocketknife").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(funnyPickaxe, this);
        getServer().getPluginManager().registerEvents(diamondHit, this);
        getServer().getPluginManager().registerEvents(spawnPigsOnDeath, this);
        getLogger().info(ChatColor.GOLD + "Testing plugin enabled");
    }

    public void onDisable() {
        getLogger().info(ChatColor.GOLD + "Testing plugin disabled");
    }

    public static Pocketknife getInstance() {
        return instance;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // TODO cleanup

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /pocketknife <feature>");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Get off the console");
            return true;
        }

        Class<?> targetClass = null;
        // Check args[0] against commandClassNameMap.keySet()
        for (String str : commandClassNameMap.keySet()) {
            if (str.equalsIgnoreCase(args[0])) targetClass = commandClassNameMap.get(str);
        }

        if (targetClass == null) {
            sender.sendMessage(ChatColor.RED + "Couldn't find that feature.");
            return true;
        }

        boolean success;
        Method m = Utils.getCommandMethod(targetClass);

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final ArrayList<String> strings = new ArrayList<>();
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            strings.addAll(commandClassNameMap.keySet());
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        // TODO Add a way to grab tabcompletions from other classes too
        return completions;
    }

}
