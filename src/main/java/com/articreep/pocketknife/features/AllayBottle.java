package com.articreep.pocketknife.features;

import com.articreep.pocketknife.PocketknifeFeature;
import org.bukkit.Material;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AllayBottle implements PocketknifeFeature, Listener {
    @Override
    public String getDescription() {
        return "Allows you to place allays in a bottle";

        /*
        - Allays can be shift-right clicked to trap them into a glass bottle and called "Bottle of Allay"
        - The item they are carrying will stay in the bottle
        - The Allay can be released by right-clicking a block
        - You can drink the Bottle of Allay which will cause you to levitate for 30 seconds
            - The item that the Allay was carrying, if any, will drop (as if you burped it)
         */
    }

    @EventHandler
    public void onAllayRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Allay allay) {
            Player player = event.getPlayer();
            PlayerInventory inv = player.getInventory();
            if (inv.getItemInMainHand().getType() == Material.GLASS_BOTTLE) {
                event.setCancelled(true);
                // may be null
                ItemStack item = allay.getInventory().getItem(0);
                // todo record the allay's item (can be null) and put it in the bottle
            } else if (inv.getItemInOffHand().getType() == Material.GLASS_BOTTLE) {
                event.setCancelled(true);
                // may be null
                ItemStack item = allay.getInventory().getItem(0);
            }
            // todo unduplicate code
        }
    }



}
