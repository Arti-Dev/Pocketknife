package com.articreep.pocketknife.features.combo;

import com.articreep.pocketknife.Pocketknife;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ComboCounter {
    private final Player player;
    private int combo = 0;
    /** Ticks since last combo increase */
    private int inactiveTicks = 0;
    /** Number of inactive ticks until combo resets */
    private final int expiry;
    /** Contains task that increments inactiveTicks every second */
    private BukkitTask expiryTask = null;
    private ComboSound currentSound = ComboSound.createComboSound(0);
    // Rhythm combo fields
    /** Stores the amount of ticks in between each combo increment */
    private int ticksBetweenCombo = 0;
    private int rhythmCombo = 0;
    private BukkitTask rhythmComboFX = null;

    /**
     * Constructs a new Combo Counter with bound player and expiry time (in ticks)
     * @param player Player which this counter is bound to
     * @param expiry Expiry time (in ticks)
     */
    public ComboCounter(Player player, int expiry) {
        this.player = player;
        this.expiry = expiry;
    }

    public void incrementCombo() {
        currentSound.interrupt();
        currentSound = ComboSound.createComboSound(++combo);
        currentSound.play(player);

        if (combo >= 1 && expiryTask == null) expiryTask = countDown();

        // Detect rhythm combo
        if (Math.abs(ticksBetweenCombo - inactiveTicks) < 3) {
            rhythmCombo++;
        } else {
            ticksBetweenCombo = inactiveTicks;
            resetRhythmCombo();
        }

        if (combo > 10 && rhythmCombo >= 4) {
            playComboSFX();
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                ChatColor.GRAY + "" + combo + "x " + ChatColor.DARK_PURPLE + rhythmCombo + "x"));
        resetInactiveTicks();
    }
    private void resetCombo() {
        combo = 0;
        ticksBetweenCombo = 0;
        resetInactiveTicks();
        if (!expiryTask.isCancelled()) {
            expiryTask.cancel();
            expiryTask = null;
        }
        resetRhythmCombo();
    }

    private void resetRhythmCombo() {
        rhythmCombo = 0;
        if (rhythmComboFX != null) {
            rhythmComboFX.cancel();
            rhythmComboFX = null;
        }
    }

    private void resetInactiveTicks() {
        inactiveTicks = 0;
    }

    /**
     * Returns a BukkitRunnable that increments inactiveTicks every tick
     * @return new runnable
     */
    private BukkitTask countDown() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                inactiveTicks++;
                if (inactiveTicks >= expiry) {
                    resetCombo();
                }
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 1);
    }

    private void playComboSFX() {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, 1);
        player.playSound(player, Sound.BLOCK_BAMBOO_STEP, 1, 1);
        rhythmComboFX = new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
            }
        }.runTaskLater(Pocketknife.getInstance(), ticksBetweenCombo/2);
    }
}
