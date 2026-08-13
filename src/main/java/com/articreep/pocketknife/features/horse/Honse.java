package com.articreep.pocketknife.features.horse;

import com.articreep.pocketknife.PocketknifeSubcommand;
import com.destroystokyo.paper.entity.ai.Goal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Honse extends PocketknifeSubcommand {
    private org.bukkit.entity.Horse honse;


    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command must be run as player");
            return true;
        }

        if (args.length == 0) {
            if (honse == null) {
                honse = (org.bukkit.entity.Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
                Bukkit.getMobGoals().removeAllGoals(honse);
                sender.sendMessage("Horse alive");
            } else {
                honse.kill();
                honse = null;
                sender.sendMessage("Horse dead");
            }
        } else if (args.length == 1) {
            if (honse == null) {
                sender.sendMessage("Spawn a horse first with /spawnhorse (no args)");
                return true;
            }
            switch (args[0]) {
                case "rearing" -> {
                    boolean rearing = honse.isRearing();
                    honse.setRearing(!rearing);
                    sender.sendMessage("Toggled rearing");
                }
                case "eating" -> {
                    boolean eating = honse.isEating();
                    honse.setEating(!eating);
                    sender.sendMessage("Toggled eating");
                }
                case "eatinggrass" -> {
                    boolean eatinggrass = honse.isEatingGrass();
                    honse.setEatingGrass(!eatinggrass);
                    sender.sendMessage("Toggled eating grass");
                }
                case "ram" -> {
                    Goal<Horse> goal = new HorseRam(honse, player);
                    Bukkit.getMobGoals().addGoal(honse, 0, goal);
                }
                case "ramforever" -> {
                    HorseRam goal = new HorseRam(honse, player);
                    goal.setForever(true);
                    Bukkit.getMobGoals().addGoal(honse, 0, goal);
                }
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final ArrayList<String> strings = new ArrayList<>();
        final List<String> completions = new ArrayList<>();
        if (args.length == 1 && honse != null) {
            strings.add("rearing");
            strings.add("eating");
            strings.add("eatinggrass");
            strings.add("ram");
            strings.add("ramforever");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @Override
    public String getSyntax() {
        return "/pk spawnhorse <rearing/eating/eatingrass>";
    }

    @Override
    public String getDescription() {
        return "Spawns a horse";
    }

    @Override
    protected void onDisable() {

    }
}
