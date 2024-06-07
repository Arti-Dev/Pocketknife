package com.articreep.pocketknife.features.enderman;

import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.EnderMan;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEnderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Enderman extends PocketknifeSubcommand implements Listener {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    protected void onDisable() {

    }

    @EventHandler
    public void onEndermanTeleport(EntityTeleportEvent event) {
        if (endermen.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    // todo im just gonna leave this memory leak here idc
    private HashSet<Entity> endermen = new HashSet<>();
    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            Location location = player.getLocation();
            org.bukkit.entity.Enderman endy = (org.bukkit.entity.Enderman)
                    player.getWorld().spawnEntity(location, EntityType.ENDERMAN);
            EnderMan customEnderman = ((CraftEnderman)endy).getHandle();

            Set goals = (Set) Utils.getPrivateField("availableGoals", GoalSelector.class, customEnderman.goalSelector);
            Set targets = (Set) Utils.getPrivateField("availableGoals", GoalSelector.class, customEnderman.targetSelector);
            goals.clear();
            targets.clear();

            customEnderman.goalSelector.addGoal(0, new CustomPathfinderGoal(customEnderman, location.add(0, 0, 10), 1.0D));

            endermen.add(endy);

            player.sendMessage("Attempted to summon an enderman");
            return true;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }
}
