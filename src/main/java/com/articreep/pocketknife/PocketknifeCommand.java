package com.articreep.pocketknife;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * This exists purely to differentiate from CommandExecutor.
 */
public interface PocketknifeCommand {
    boolean runCommand(CommandSender sender, Command command, String label, String[] args);

}
