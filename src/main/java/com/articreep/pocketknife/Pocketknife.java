package com.articreep.pocketknife;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Pocketknife extends JavaPlugin implements CommandExecutor, TabCompleter {
    private static Pocketknife instance;
    private static Reflections reflections;
    private static final HashMap<String, PocketknifeCommand> commandClassNameMap = new HashMap<>();
    public void onEnable() {
        instance = this;

        initReflections();

        /* The plugin checks every class in this package and creates an instance of it.
        Then, it checks to see if these are subclasses of Listener, PocketknifeCommand, or both
        It is then registered in the respective location.
        Therefore, all that needs to be done in each individual class is to implement Listener or PocketknifeCommand and that's it! */
        for (Class<?> clazz : reflections.getSubTypesOf(Object.class)) {
            if (clazz == this.getClass()) continue;
            if (!registerClass(clazz)) {
                Bukkit.getConsoleSender().sendMessage(clazz.getName() + " had nothing to register!");
            }
        }

        // /pocketknife is the umbrella command for all features of this plugin.
        getCommand("pocketknife").setExecutor(this);
        getCommand("pocketknife").setTabCompleter(this);
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Pocketknife (testing plugin) enabled");
    }

    /**
     * Initializes Reflections library for me.
     * i don't know how it works, but it works
     * pulled this from here: <a href="https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection">...</a>
     */
    private void initReflections() {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.articreep.pocketknife"))));
    }

    /**
     * Creates an instance of the target class.
     * Then, registers event methods to Bukkit or registers the command method to my HashMap if it implements Listener or PocketknifeCommand, respectively.
     * If none match returns false
     * @param targetClass The target class
     * @return Whether anything was registered
     */
    private boolean registerClass(Class<?> targetClass) {
        // Will be garbage collected if not used
        Object classObj;

        // Only load classes that have a constructor that takes no arguments.
        // Anything that takes more - let's manually register them.
        Constructor<?>[] ctors = targetClass.getDeclaredConstructors();
        Constructor<?> ctor = null;
        for (Constructor<?> constructor : ctors) {
            ctor = constructor;
            if (ctor.getGenericParameterTypes().length == 0)
                break;
        }

        // Obligatory null check
        if (ctor == null) return false;

        try {
            classObj = ctor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        boolean registered = false;
        if (classObj instanceof PocketknifeCommand) {
            // Register command into my hashmap
            commandClassNameMap.put(targetClass.getSimpleName(), (PocketknifeCommand) classObj);
            registered = true;
        }
        if (classObj instanceof Listener) {
            // Register listener
            getServer().getPluginManager().registerEvents((Listener) classObj, this);
            registered = true;
        }
        return registered;
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Pocketknife (testing plugin) disabled");
    }

    public static Pocketknife getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /pocketknife <feature> <args>");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Get off the console");
            return true;
        }

        PocketknifeCommand pocketCommand = getPocketKnifeCommand(args[0]);

        if (pocketCommand == null) {
            sender.sendMessage(ChatColor.RED + "Couldn't find that feature.");
            return true;
        }

        boolean success = pocketCommand.runCommand(sender, command, label, Utils.removeFirstArg(args));
        if (!success) {
            sender.sendMessage(ChatColor.RED + "Something went wrong when running the command.");
        }

        return true;
    }

    private PocketknifeCommand getPocketKnifeCommand(String query) {
        // Check against commandClassNameMap.keySet()
        for (String str : commandClassNameMap.keySet()) {
            if (str.equalsIgnoreCase(query)) return commandClassNameMap.get(str);
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> strings = new ArrayList<>();
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            strings.addAll(commandClassNameMap.keySet());
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }

        // Up to another class to tabcomplete
        if (args.length > 1) {
            PocketknifeCommand pocketCommand = getPocketKnifeCommand(args[0]);

            if (pocketCommand == null) {
                return completions;
            } else {
                completions = pocketCommand.tabComplete(sender, command, alias, Utils.removeFirstArg(args));
                if (completions == null) return new ArrayList<>();
            }
        }

        return completions;
    }

}
