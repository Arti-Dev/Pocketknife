package com.articreep.pocketknife.features.combo;

import com.articreep.pocketknife.Pocketknife;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class ComboSound {
    private final ArrayList<Pitch[]> chart = new ArrayList<>();
    private BukkitTask runnable = null;
    /**
     * Create a ComboSound with first note
     * @param pitches Pitches to play at once
     */
    public ComboSound(Pitch... pitches) {
        chart.add(pitches);
    }

    /**
     * Use this to generate combo SFX
     * These combo sfx are meant to be single-use, but can be reused if needed
     * @param combo Combo number.
     * @return the sound
     */
    public static ComboSound createComboSound(int combo) {
        // Combo sounds start at 2
        if (combo <= 1) return new ComboSound();
        if (combo == 2) return new ComboSound(Pitch.G);
        if (combo == 3) return new ComboSound(Pitch.A, Pitch.C);
        if (combo == 4) return new ComboSound(Pitch.B, Pitch.D);
        if (combo == 5) return new ComboSound(Pitch.C, Pitch.E, Pitch.AHI);
        if (combo == 6) return new ComboSound(Pitch.D, Pitch.FSHARPHI, Pitch.AHI);
        if (combo == 7) return new ComboSound(Pitch.E, Pitch.GHI, Pitch.BHI);
        if (combo == 8) return new ComboSound(Pitch.FSHARPHI, Pitch.AHI, Pitch.CHI);
        if (combo == 9) return new ComboSound(Pitch.FSHARPHI, Pitch.AHI, Pitch.CHI).addNotes(Pitch.DHI).addNotes(Pitch.EHI);
        if (combo == 10) return new ComboSound(Pitch.FSHARPHI, Pitch.AHI, Pitch.CHI).addNotes(Pitch.DHI).
                addNotes(Pitch.EHI).addNotes(Pitch.FSHARPHIGHEST);
        else return new ComboSound(Pitch.FSHARPHI, Pitch.AHI, Pitch.CHI).addNotes(Pitch.EHI).
                addNotes(Pitch.DHI).addNotes(Pitch.FSHARPHIGHEST);
    }

    /**
     * Adds more notes to play two ticks after the previously added set of notes.
     * @param pitches Pitches to play at once two ticks after the previous set
     */
    public ComboSound addNotes(Pitch... pitches) {
        chart.add(pitches);
        return this;
    }

    public void play(Player player) {
        runnable = new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                for (Pitch pitch : chart.get(i)) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1, pitch.getPitch());
                }
                i++;
                if (i >= chart.size()) {
                    runnable = null;
                    this.cancel();
                }
            }
        }.runTaskTimer(Pocketknife.getInstance(), 0, 2);
    }

    /**
     * Stops the current sound if it is playing
     */
    public void interrupt() {
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }
    }
}
