package com.articreep.pocketknife.features.combo;

import com.articreep.pocketknife.Pocketknife;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class ComboCounter {
    private final Player player;
    private int combo = 0;
    private int multiplicity = 1;
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
    private final NamespacedKey bossBarKey;
    private BossBar bossBar;

    /**
     * Constructs a new Combo Counter with bound player and expiry time (in ticks)
     * @param player Player which this counter is bound to
     * @param expiry Expiry time (in ticks)
     */
    public ComboCounter(Player player, int expiry) {
        this.player = player;
        this.expiry = expiry;
        this.bossBarKey = new NamespacedKey(Pocketknife.getInstance(),
                player.getUniqueId() + "_Bossbar");
    }

    private boolean incrementedThisTick = false;

    public void incrementCombo() {
        if (incrementedThisTick) {
            incrementMultiplicity();
            return;
        }
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
            if (bossBar == null) {
                bossBar = Bukkit.createBossBar(bossBarKey, ChatColor.BOLD + "" +
                        ChatColor.DARK_PURPLE + rhythmCombo + " RHYTHM COMBO", BarColor.BLUE, BarStyle.SOLID);
                bossBar.addPlayer(player);
                bossBar.setProgress(1);
            }
            playComboFX();
        }

        sendActionBar(combo, multiplicity);
        resetInactiveTicks();

        incrementedThisTick = true;
        Bukkit.getScheduler().runTask(Pocketknife.getInstance(), () -> {
            incrementedThisTick = false;
            multiplicity = 1;
        });
    }

    public void incrementMultiplicity() {
        multiplicity += 1;
        if (multiplicity >= 4 && combo >= 4) currentSound.extra = true;
        sendActionBar(combo, multiplicity);
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
        if (bossBar != null) {
            bossBar.removeAll();
            Bukkit.removeBossBar(bossBarKey);
            bossBar = null;
        }
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


    private void playComboFX() {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, 1);
        player.playSound(player, Sound.BLOCK_BAMBOO_STEP, 1, 1);
        updateBossBar(BarColor.BLUE);
        spawnNoteParticle();
        rhythmComboFX = new BukkitRunnable() {
            @Override
            public void run() {
                updateBossBar(BarColor.RED);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
            }
        }.runTaskLater(Pocketknife.getInstance(), ticksBetweenCombo/2);
    }

    private void sendActionBar(int combo, int multiplicity) {
        TextComponent component;
        if (combo <= 1) return;
        else if (combo <= 4) component = new TextComponent(ChatColor.GRAY + "" + combo + " Combo");
        else if (combo <= 7) component = new TextComponent(ChatColor.YELLOW + "" + combo + " Combo");
        else if (combo <= 10) component = new TextComponent(ChatColor.GOLD + "" + combo + " COMBO");
        else component = new TextComponent(ChatColor.AQUA + "" + ChatColor.BOLD + combo + " COMBO!!!");

        if (multiplicity > 1) {
            component.addExtra(" " + ChatColor.BOLD + ChatColor.DARK_RED + multiplicity + "x");
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    private void updateBossBar(BarColor color) {
        bossBar.setColor(color);
        bossBar.setTitle(ChatColor.BOLD + "" +
                ChatColor.DARK_PURPLE + rhythmCombo + " RHYTHM COMBO");
    }

    private void spawnNoteParticle() {
        Random random = new Random();
        player.getWorld().spawnParticle(Particle.NOTE,
                player.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2);
    }
}
