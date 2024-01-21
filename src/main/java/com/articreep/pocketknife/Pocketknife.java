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
import java.util.*;

public class Pocketknife extends JavaPlugin implements CommandExecutor, TabCompleter {
    private static Pocketknife instance;
    private static Reflections reflections;
    private static final HashMap<String, PocketknifeSubcommand> commandClassNameMap = new HashMap<>();
    private static final HashSet<PocketknifeConfigurable> configurableClasses = new HashSet<>();
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        initReflections();

        /* The plugin checks every class in this package and creates an instance of it.
        Then, it checks to see if these are subclasses of Listener, PocketknifeSubcommand, or both
        It is then registered in the respective location.
        Therefore, all that needs to be done in each individual class is to implement Listener or PocketknifeSubcommand and that's it! */
        for (Class<?> clazz : reflections.getSubTypesOf(Object.class)) {
            if (clazz == this.getClass()) continue;
            try {
                if (!registerClass(clazz)) {
                    Bukkit.getConsoleSender().sendMessage(clazz.getName() + " had nothing to register!");
                }
            } catch (InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Couldn't load " + clazz.getName()
                        + "because it is inaccessible. Perhaps the constructor is private? (IllegalAccessException)");
            } catch (IllegalArgumentException e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Couldn't load " + clazz.getName()
                        + " since it has inconsistent constructor arguments (IllegalArgumentException)");
            }
        }

        loadConfig();

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
     * Load this AFTER registering everything!
     */
    private void loadConfig() {
        for (PocketknifeConfigurable clazz : configurableClasses) {
            clazz.loadConfig(getConfig());
        }
    }

    /**
     * Creates an instance of the target class.
     * Then, registers event methods to Bukkit or registers the command method to my HashMap if it implements Listener or PocketknifeSubcommand, respectively.
     * If none match returns false
     * @param targetClass The target class
     * @return Whether anything was registered
     */
    private boolean registerClass(Class<?> targetClass) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        // Will be garbage collected if not used
        Object classObj;

        Constructor<?> ctor = getConstructor(targetClass);

        // Obligatory null check
        if (ctor == null) return false;

        classObj = ctor.newInstance();

        boolean registered = false;
        if (classObj instanceof PocketknifeSubcommand) {
            // Register command into my hashmap
            commandClassNameMap.put(targetClass.getSimpleName(), (PocketknifeSubcommand) classObj);
            Bukkit.getConsoleSender().sendMessage("Registered command from " + targetClass.getSimpleName());
            registered = true;
        }
        if (classObj instanceof Listener) {
            // Register listener
            getServer().getPluginManager().registerEvents((Listener) classObj, this);
            Bukkit.getConsoleSender().sendMessage("Registered listener from " + targetClass.getSimpleName());
            registered = true;
        }
        if (classObj instanceof PocketknifeConfigurable) {
            // Add instance to our set
            configurableClasses.add((PocketknifeConfigurable) classObj);
            Bukkit.getConsoleSender().sendMessage("Registered configuration from " + targetClass.getSimpleName());
            registered = true;
        }
        return registered;
    }

    /**
     * Looks for one of two constructors: a default constructor or one that has a single parameter (FileConfiguration).
     * The constructor with the single parameter takes priority.
     * If none found, return null
     * @param targetClass Any class
     * @return The desired constructor, null if not found
     */
    private Constructor<?> getConstructor(Class<?> targetClass) {
        // Only load classes that have a constructor that takes no arguments.
        // Anything that takes more - let's manually register them.
        Constructor<?>[] ctors = targetClass.getDeclaredConstructors();
        Constructor<?> ctor = null;
        for (Constructor<?> constructor : ctors) {
            ctor = constructor;
            if (ctor.getGenericParameterTypes().length == 0)
                break;
        }
        return ctor;
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

        // Special arguments

        // Reload
        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            loadConfig();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
            return true;
        }

        PocketknifeSubcommand pocketCommand = getPocketKnifeCommand(args[0]);

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

    private PocketknifeSubcommand getPocketKnifeCommand(String query) {
        // Check against commandClassNameMap.keySet()
        for (String str : commandClassNameMap.keySet()) {
            if (str.equalsIgnoreCase(query)) return commandClassNameMap.get(str);
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            ArrayList<String> strings = new ArrayList<>(commandClassNameMap.keySet());
            strings.add("reload");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }

        // Up to another class to tabcomplete
        if (args.length > 1) {
            PocketknifeSubcommand pocketCommand = getPocketKnifeCommand(args[0]);

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
