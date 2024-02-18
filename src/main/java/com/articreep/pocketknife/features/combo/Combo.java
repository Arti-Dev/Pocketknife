package com.articreep.pocketknife.features.combo;

import com.articreep.pocketknife.PocketknifeFeature;
import com.articreep.pocketknife.PocketknifeSubcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;

public class Combo extends PocketknifeSubcommand implements PocketknifeFeature, Listener {
    private static final HashMap<Player, ComboCounter> comboMap = new HashMap<>();
    private static boolean enabled = true;
    @Override
    public String getDescription() {
        return "Combo cosmetic system.";
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        enabled = !enabled;
        if (!enabled) {
            sender.sendMessage("Combo is now disabled");
        } else {
            sender.sendMessage("Combo is now enabled");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife Combo";
    }

    @EventHandler
    public void onDiamondBreak(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        if (comboMap.containsKey(player)) comboMap.get(player).incrementCombo();
        else {
            ComboCounter counter = new ComboCounter(player, 60);
            counter.incrementCombo();
            comboMap.put(player, counter);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (comboMap.containsKey(player)) comboMap.get(player).incrementCombo();
        else {
            ComboCounter counter = new ComboCounter(player, 60);
            counter.incrementCombo();
            comboMap.put(player, counter);
        }
    }


    @EventHandler
    public void onDC(PlayerQuitEvent event) {
        comboMap.remove(event.getPlayer());
    }

    public static void registerCombo(Player player) {
        if (hasRegisteredCombo(player)) return;
        comboMap.put(player, new ComboCounter(player, 60));
    }

    public static boolean hasRegisteredCombo(Player player) {
        return comboMap.containsKey(player);
    }

    public static ComboCounter getComboCounter(Player player) {
        if (!hasRegisteredCombo(player)) registerCombo(player);
        return comboMap.get(player);
    }


}
