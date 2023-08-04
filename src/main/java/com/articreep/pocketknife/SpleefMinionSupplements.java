package com.articreep.pocketknife;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SpleefMinionSupplements extends PocketknifeSubcommand implements PocketknifeFeature {
    @Override
    public String getDescription() {
        return "Supplements for my Spleef Minion idea";
    }

    @Override
    boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    String getSyntax() {
        return null;
    }
}
