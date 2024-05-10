package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeSubcommand;
import com.articreep.pocketknife.Utils;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Parametric extends PocketknifeSubcommand {

    @Override
    public String getDescription() {
        return "parametric equations";
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            int id = Utils.parseInt(args[0]);
            int seconds = Utils.parseInt(args[1]);
            if (id == -1 || seconds == -1) {
                sender.sendMessage(getSyntax());
                return true;
            }
            if (id == 1) {
                parametric1(seconds * 20, (Player) sender);
            } else {
                sender.sendMessage(getSyntax());
            }
            return true;
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife parametric <id> <seconds>";
    }

    private void parametric1(int ticks, Player player) {
        new BukkitRunnable() {
            int elapsed = 0;
            double t = 0.0;
            @Override
            public void run() {
                elapsed++;
                t += (double) 1/20;
                if (t > 2 * Math.PI) t -= 2 * Math.PI;

                if (elapsed > ticks) {
                    cancel();
                    return;
                }
                double x = Math.sin(3*t) * Math.sin(t);
                double y = Math.sin(3*t) * Math.sin(t) * Math.cos(t);
                double z = Math.sin(3*t) * Math.cos(t);
                player.getWorld().spawnParticle(Particle.DUST, player.getLocation().
                        add(0, 1, 0).
                        add(x, y, z), 1);
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }
}
