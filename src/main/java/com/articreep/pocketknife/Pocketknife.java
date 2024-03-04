package com.articreep.pocketknife;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
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
    /** Stores all Pocketknife Feature instances and their string equivalent. */
    private static final HashMap<String, PocketknifeFeature> featureMap = new HashMap<>();
    /** Exclusively stores instances of Listener objects that aren't PocketknifeFeatures
     * Sometimes I get lazy
     */
    private static final HashMap<String, Listener> listenerMap = new HashMap<>();

    // HashSets here are for caching purposes only
    private static final HashSet<String> pocketknifeSubcommands = new HashSet<>();
    private static final HashSet<PocketknifeConfigurable> configurableClasses = new HashSet<>();

    /**
     * Initializes Reflections library for me.
     * I don't know how it works, but it works
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

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Pocketknife (testing plugin) disabled");
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


        // Attempt to identify its type
        boolean registered = false;
        if (classObj instanceof PocketknifeFeature feature) {
            // todo feature.enabled = ???
            if (feature.isEnabled() && feature instanceof Listener listener) registerListener(listener);
            if (feature instanceof PocketknifeSubcommand command) registerSubcommand(command);
            featureMap.put(targetClass.getSimpleName(), feature);
            registered = true;

        } else if (classObj instanceof Listener listener) {
            registerListener(listener);
            listenerMap.put(targetClass.getSimpleName(), listener);
            registered = true;
        }

        if (classObj instanceof PocketknifeConfigurable configurable) {
            // Add instance to our set
            registerConfigurable(configurable);
            registered = true;
        }

        return registered;
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        Bukkit.getConsoleSender().sendMessage("Registered listener from " + listener.getClass().getSimpleName());
    }

    private void registerSubcommand(PocketknifeSubcommand command) {
        pocketknifeSubcommands.add(command.getClass().getSimpleName());
        Bukkit.getConsoleSender().sendMessage("Registered command from " + command.getClass().getSimpleName());
    }

    private void registerConfigurable(PocketknifeConfigurable configurable) {
        configurableClasses.add(configurable);
        Bukkit.getConsoleSender().sendMessage("Registered configuration from " + configurable.getClass().getSimpleName());
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

    private PocketknifeFeature getFeature(String query) {
        for (String str : featureMap.keySet()) {
            if (str.equalsIgnoreCase(query)) return featureMap.get(str);
        }
        return null;
    }

    private PocketknifeSubcommand getSubcommand(String query) {
        for (String str : featureMap.keySet()) {
            if (str.equalsIgnoreCase(query) && featureMap.get(str) instanceof PocketknifeSubcommand command)
                return command;
        }
        return null;
    }

    private Listener getListener(String query) {
        for (String str : listenerMap.keySet()) {
            if (str.equalsIgnoreCase(query)) {
                return listenerMap.get(query);
            }
        }
        return null;
    }

    // todo in the future i might think about garbage collecting the feature instances to disable them to save memory
    private void disableFeature(PocketknifeFeature feature) {
        if (feature instanceof Listener listener) unregisterListener(listener);
        feature.onDisable();
        feature.enabled = false;
    }

    private void enableFeature(PocketknifeFeature feature) {
        if (feature instanceof Listener listener) getServer().getPluginManager().registerEvents(listener, this);
        feature.onEnable();
        feature.enabled = true;
    }

    private void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /pocketknife <feature> <args>");
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

        // Toggle
        if (args[0].equalsIgnoreCase("toggle")) {
            PocketknifeFeature pocketFeature = getFeature(args[1]);
            if (pocketFeature != null) {
                if (pocketFeature.isEnabled()) {
                    disableFeature(pocketFeature);
                    sender.sendMessage(ChatColor.RED + pocketFeature.getClass().getSimpleName() + " disabled");
                } else {
                    enableFeature(pocketFeature);
                    sender.sendMessage(ChatColor.GREEN + pocketFeature.getClass().getSimpleName() + " enabled");
                }
            } else if (getListener(args[1]) != null) {
                sender.sendMessage(ChatColor.RED + "Toggling listeners is not supported yet");
                // todo support regular listeners
            } else {
                sender.sendMessage(ChatColor.RED + "Couldn't find that feature.");
            }
            return true;
        }

        // Run a command

        PocketknifeSubcommand pocketCommand = getSubcommand(args[0]);

        if (pocketCommand == null) {
            sender.sendMessage(ChatColor.RED + "Couldn't find that feature.");
            return true;
        }

        if (!(sender instanceof Player) && !pocketCommand.canConsoleUse()) {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        if (!pocketCommand.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "This feature is currently disabled!");
            sender.sendMessage(ChatColor.GRAY + "Enable it with /pocketknife toggle <feature>");
            return true;
        }

        boolean success = pocketCommand.runCommand(sender, command, label, Utils.removeFirstArg(args));
        if (!success) {
            sender.sendMessage(ChatColor.RED + "Something went wrong when running the command.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            ArrayList<String> strings = new ArrayList<>(pocketknifeSubcommands);
            strings.add("reload");
            strings.add("toggle");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }

        else if (args.length > 1 && args[0].equalsIgnoreCase("toggle")) {
            ArrayList<String> strings = new ArrayList<>(featureMap.keySet());
            strings.addAll(listenerMap.keySet());
            StringUtil.copyPartialMatches(args[1], strings, completions);
        }
        // Up to another class to tabcomplete
        else if (args.length > 1) {
            PocketknifeSubcommand pocketCommand = getSubcommand(args[0]);

            if (pocketCommand == null) {
                return completions;
            } else {
                completions = pocketCommand.tabComplete(sender, command, alias, Utils.removeFirstArg(args));
                if (completions == null) return new ArrayList<>();
            }
        }

        return completions;
    }

    public static Pocketknife getInstance() {
        return instance;
    }

}
