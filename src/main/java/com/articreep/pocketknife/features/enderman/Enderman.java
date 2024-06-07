package com.articreep.pocketknife.features.enderman;

import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.EnderMan;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEnderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
        if (event.getEntity().equals(endy) && !allowTeleport) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.ENDER_EYE) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                if (endy != null && !endy.isDead()) {
                    allowTeleport = true;
                    endy.teleport(event.getClickedBlock().getLocation().add(0, 1, 0));
                    allowTeleport = false;
                    setCurrentGoal(event.getClickedBlock().getLocation().add(0, 1, 0));
                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent("Endy is here"));
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                if (endy != null && !endy.isDead()) {
                    Location location = event.getClickedBlock().getLocation();
                    setCurrentGoal(location);
                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent("Endy is rapidly approaching your location"));
                }
            }
        }
    }

    private void setCurrentGoal(Location location) {
        EnderMan customEnderman = ((CraftEnderman)endy).getHandle();
        if (currentGoal != null) {
            customEnderman.goalSelector.removeGoal(currentGoal);
        }
        currentGoal = new CustomPathfinderGoal(customEnderman, location, 1.5);
        customEnderman.goalSelector.addGoal(0, currentGoal);
    }

    private org.bukkit.entity.Enderman endy;
    private boolean allowTeleport = false;
    private Goal currentGoal = null;
    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (endy != null && !endy.isDead()) {
                endy.remove();
                player.sendMessage("Killed Endy");
            }
            Location location = player.getLocation();
            endy = (org.bukkit.entity.Enderman)
                    player.getWorld().spawnEntity(location, EntityType.ENDERMAN);
            endy.setCustomName("Endy");

            EnderMan customEnderman = ((CraftEnderman)endy).getHandle();
            Set goals = (Set) Utils.getPrivateField("availableGoals", GoalSelector.class, customEnderman.goalSelector);
            Set targets = (Set) Utils.getPrivateField("availableGoals", GoalSelector.class, customEnderman.targetSelector);
            goals.clear();
            targets.clear();

            customEnderman.goalSelector.addGoal(1, new LookAtPlayerGoal(customEnderman, net.minecraft.world.entity.player.Player.class, 8.0F));

            player.sendMessage("Spawned new Endy");
            player.sendMessage("Right click a block with an eye of ender to teleport Endy to that location");
            player.sendMessage("Left click a block with an eye of ender to have Endy pathfind to that location");
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
