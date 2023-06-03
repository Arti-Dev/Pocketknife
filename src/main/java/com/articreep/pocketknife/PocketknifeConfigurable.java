package com.articreep.pocketknife;

import org.bukkit.configuration.file.FileConfiguration;

public interface PocketknifeConfigurable extends PocketknifeFeature {
    /**
     * Method called when config is reloaded.
     * @param config FileConfiguration object
     */
    void loadConfig(FileConfiguration config);
}
