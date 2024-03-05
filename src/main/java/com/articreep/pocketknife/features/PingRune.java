package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeFeature;
import com.articreep.pocketknife.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class PingRune extends PocketknifeFeature implements Listener {
    @Override
    public String getDescription() {
        return "Runes are cosmetic items that can be applied to items on Hypixel Skyblock. " +
                "Ping is the temporal latency between your client and the server.";
    }

    @Override
    protected void onDisable() {
        // nothing
    }

    @EventHandler
    public void onEntityKill(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof Damageable victim) {
            // Did this result in a kill?
            if (victim.getHealth() - event.getFinalDamage() <= 0) {
                World w = player.getWorld();
                int ping = player.getPing();

                ChatColor color;
                if (ping < 100) color = ChatColor.GREEN;
                else if (ping < 200) color = ChatColor.YELLOW;
                else color = ChatColor.RED;

                Location loc = victim.getLocation().add(0, 0.5, 0);
                offsetLocation(loc, player.getLocation());
                TextDisplay display = (TextDisplay) w.spawnEntity(loc, EntityType.TEXT_DISPLAY);
                display.setBillboard(Display.Billboard.CENTER);
                display.setText(color + String.valueOf(ping) + "ms");
                setTextRotation(display, player.getLocation());

                Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), display::remove, 40);
            }
        }
    }

    private static void offsetLocation(Location textLoc, Location playerLoc) {
        // TODO We could do some linear algebra logic, but not right now

        Location primaryLoc = textLoc.clone();
        Vector diff = primaryLoc.subtract(playerLoc).toVector();
        // Create a random vector to be applied to the original effect location
        Vector randomVec = Utils.randomVector(0.7);
        Vector resultant = diff.clone().add(randomVec);

        // Ensure that the resulting location will be closer to the player and not further away.
        // If not just subtract the random vector rather than add it.
        if (resultant.lengthSquared() < diff.lengthSquared()) {
            textLoc.add(randomVec);
        } else {
            textLoc.subtract(randomVec);
        }
    }



    private static void setTextRotation(TextDisplay display, Location damagerLoc) {
        display.setRotation(Utils.invertYaw(damagerLoc.getYaw()), Utils.invertPitch(damagerLoc.getPitch()));
    }
}
