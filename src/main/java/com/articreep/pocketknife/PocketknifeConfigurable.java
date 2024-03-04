package com.articreep.pocketknife;

import org.bukkit.configuration.file.FileConfiguration;

public interface PocketknifeConfigurable {
    /**
     * Method called when config is reloaded.
     * Implement logic to fetch config values here.
     * @param config FileConfiguration object
     */
    void loadConfig(FileConfiguration config);
}
