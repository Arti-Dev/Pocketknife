package com.articreep.pocketknife.features;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.DecoratedPot;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public class SplashDecoratedPot implements Listener {
    /*
    When a decorated pot containing a splash/lingering potion is broken
    the potion comes out as a thrown potion
     */
    @EventHandler
    public void onPotBreak(ProjectileHitEvent event) {
        Block block = event.getHitBlock();
        if (block == null) return;
        if (block.getState() instanceof DecoratedPot pot) {
            if (!canBreakDecoratedPot(event.getEntity())) return;
            handleItemInPot(pot);
        }
    }

    @EventHandler
    public void onPotBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getState() instanceof DecoratedPot pot) {
            handleItemInPot(pot);
        }
    }

    private void handleItemInPot(DecoratedPot pot) {
        ItemStack droppedItem = pot.getInventory().getItem();
        if (droppedItem == null) return;
        if (droppedItem.getType() == Material.SPLASH_POTION || droppedItem.getType() == Material.LINGERING_POTION) {
            ThrownPotion potion = (ThrownPotion) pot.getWorld().spawnEntity(pot.getLocation(), EntityType.POTION);
            potion.setItem(droppedItem);
            // empty the pot before it's broken
            pot.getInventory().setItem(new ItemStack(Material.AIR));
        }
    }

    /**
     * This is obviously very limited - I haven't tested EVERY projectile.
     * @param projectile
     * @return true if the projectile can break decorated pots
     */
    public static boolean canBreakDecoratedPot(Projectile projectile) {
        if (projectile instanceof Arrow) return true;
        if (projectile instanceof Snowball) return true;
        if (projectile instanceof Egg) return true;
        if (projectile instanceof Firework) return true;
        else return false;
    }
}
