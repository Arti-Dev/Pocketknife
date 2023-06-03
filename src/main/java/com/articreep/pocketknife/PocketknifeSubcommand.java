package com.articreep.pocketknife;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * This exists purely to differentiate from CommandExecutor.
 */
public abstract class PocketknifeSubcommand implements PocketknifeFeature {
    /**
     * Custom subcommand that follows /pocketknife CLASS_NAME (params)
     * @param sender Sender of the command
     * @param command ...
     * @param label ...
     * @param args Arguments
     * @return Whether the command was successful (try not to return false)
     */
    abstract boolean runCommand(CommandSender sender, Command command, String label, String[] args);

    /**
     * Tabcomplete method, same as regular tabcomplete. Can return null
     * @param sender Sender of the command
     * @param command ...
     * @param alias ...
     * @param args incomplete arguments
     * @return Tab completions
     */
    abstract List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args);

    abstract String getSyntax();

    /**
     * Sends the specified object a message with the description.
     */
    void sendDescriptionMessage(CommandSender sender) {
        sender.sendMessage(getDescription());
    }

    /**
     * Sends the specified object a message with the command syntax.
     */
    void sendSyntaxMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + getSyntax());
    }

}
