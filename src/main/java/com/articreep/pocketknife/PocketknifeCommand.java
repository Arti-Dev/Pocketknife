package com.articreep.pocketknife;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * This exists purely to differentiate from CommandExecutor.
 */
public interface PocketknifeCommand {
    boolean runCommand(CommandSender sender, Command command, String label, String[] args);
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args);

}
