package com.articreep.pocketknife.features;

import com.articreep.pocketknife.PocketknifeFeature;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class MinecartRacing extends PocketknifeFeature implements Listener {

    /**
     * Returns a description of this feature.
     *
     * @return A string description of this feature
     */
    @Override
    public String getDescription() {
        return "";
    }

    /**
     * Logic to run before the feature is disabled, e.g. clearing HashMaps
     */
    @Override
    protected void onDisable() {

    }

    private static Map<Minecart, Double> minecartSpeeds = new HashMap<>();

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        Block block = event.getHitBlock();
        if (block == null) return;
        if (!(event.getEntity() instanceof Arrow arrow)) return;

        if (!(arrow.getShooter() instanceof Player player)) return;

        // Get minecart that player is in
        Entity entity = player.getVehicle();
        if (!(entity instanceof RideableMinecart minecart)) return;

        minecartSpeeds.merge(minecart, 0.0, (a, b) -> a + 0.1);
        double magnitude = minecartSpeeds.get(minecart);
        Vector velocity = minecart.getVelocity();
        if (velocity.isZero()) {
            velocity = player.getLocation().getDirection().multiply(magnitude);
        } else {
            velocity = velocity.normalize().multiply(magnitude);
        }
        minecart.setVelocity(velocity);
        block.breakNaturally();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
        event.getEntity().remove();
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getDismounted() instanceof RideableMinecart minecart)) return;

        minecartSpeeds.remove(minecart);
    }
}
