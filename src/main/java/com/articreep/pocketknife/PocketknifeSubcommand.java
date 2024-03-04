package com.articreep.pocketknife;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * This exists purely to differentiate from CommandExecutor.
 */
public abstract class PocketknifeSubcommand extends PocketknifeFeature {
    private boolean consoleCanUse = false;

    /**
     * Custom subcommand that follows /pocketknife CLASS_NAME (params)
     * @param sender Sender of the command
     * @param command ...
     * @param label ...
     * @param args Arguments
     * @return Whether the command was successful (try not to return false)
     */
    public abstract boolean runCommand(CommandSender sender, Command command, String label, String[] args);

    /**
     * Tabcomplete method, same as regular tabcomplete. Can return null
     * @param sender Sender of the command
     * @param command ...
     * @param alias ...
     * @param args incomplete arguments
     * @return Tab completions
     */
    public abstract List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args);

    /**
     * Message that explains syntax of this command to the player.
     * e.g. Usage: /pocketknife BlockList <blocks>
     * @return String explaining syntax
     */
    public abstract String getSyntax();

    /**
     * Sends the specified object a message with the description.
     */
    public void sendDescriptionMessage(CommandSender sender) {
        sender.sendMessage(getDescription());
    }

    /**
     * Sends the specified object a message with the command syntax.
     */
    public void sendSyntaxMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + getSyntax());
    }

    /**
     * Sets whether the console or non-player may use this command.
     * @param canUse Whether the console can use this command
     */
    protected void setConsoleCanUse(boolean canUse) {
        consoleCanUse = canUse;
    }

    /**
     * Gets whether this command can be used by the console.
     * @return Whether this command can be used by the console
     */
    public boolean canConsoleUse() {
        return consoleCanUse;
    }

}
