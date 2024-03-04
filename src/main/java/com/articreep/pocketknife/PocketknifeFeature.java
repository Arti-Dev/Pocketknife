package com.articreep.pocketknife;

public abstract class PocketknifeFeature {
    protected boolean enabled = false;
    /**
     * Returns a description of this feature.
     * @return A string description of this feature
     */
    public abstract String getDescription();

    /**
     * Override as needed
     */
    protected void onEnable() {
        enabled = true;
    }

    /**
     * Logic to run before the feature is disabled, e.g. clearing HashMaps
     */
    protected abstract void onDisable();

    public boolean isEnabled() {
        return enabled;
    }
}
