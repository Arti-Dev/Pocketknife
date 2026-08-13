package com.articreep.pocketknife.features.horse;

import com.articreep.pocketknife.Pocketknife;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class HorseRam implements Goal<Horse> {
    public static final GoalKey<@NotNull Horse> KEY = GoalKey.of(
            Horse.class,
            new NamespacedKey(Pocketknife.getInstance(), "HorseRam")
    );

    private LivingEntity target;
    private Horse horse;
    private boolean forever = false;

    public HorseRam(Horse horse, LivingEntity target) {
        this.horse = horse;
        this.target = target;
    }

    @Override
    public boolean shouldActivate() {
        return true;
    }

    @Override
    public @NotNull GoalKey<@NotNull Horse> getKey() {
        return KEY;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }

    @Override
    public void tick() {
        if (target != null && !target.isDead()) {
            horse.getPathfinder().moveTo(target, 3.0);
            if (inRange()) {
                damageAndKnockback();
                if (!forever) Bukkit.getMobGoals().removeGoal(horse, this);
            }
        } else {
            Bukkit.getMobGoals().removeGoal(horse, this);
        }
    }

    public boolean inRange() {
        if (target == null || target.isDead()) return false;
        Location horseLoc = horse.getLocation();
        Location targetLoc = target.getLocation();
        Vector direction = horseLoc.getDirection();
        Vector toTarget = targetLoc.toVector().subtract(horseLoc.toVector());
        if (toTarget.length() == 0) return true;
        if (toTarget.length() > 3) return false;

        return (direction.dot(toTarget) > 0.5);
    }

    public void damageAndKnockback() {
        horse.setEatingGrass(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                horse.setEatingGrass(false);
            }
        }.runTaskLater(Pocketknife.getInstance(), 10);
        if (target == null || target.isDead()) return;
        target.damage(5.0);
        Vector knockbackDir = target.getLocation().toVector().subtract(horse.getLocation().toVector()).normalize();
        target.setVelocity(knockbackDir.multiply(3).setY(1));
    }

    public void setForever(boolean forever) {
        this.forever = forever;
    }
}
