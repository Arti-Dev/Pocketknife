package com.articreep.pocketknife.features.combo;

import com.articreep.pocketknife.PocketknifeFeature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class Combo extends PocketknifeFeature implements Listener {
    private static final HashMap<Player, ComboCounter> comboMap = new HashMap<>();
    @Override
    public String getDescription() {
        return "Combo cosmetic system.";
    }

    @Override
    protected void onDisable() {
        comboMap.clear();
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
