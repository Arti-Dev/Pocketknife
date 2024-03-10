# Pocketknife
A plugin that stores all of my cutting-edge pocket experiments! Hence the name.

# The Problem
I oftentimes like to make small projects with Spigot plugins, and I used to create a new plugin every time I started something different.
However, remembering to register listeners, registering commands, updating my plugin.yml, etc. every time got very tedious.
Sometimes I even forget and end up wasting time.

# What Pocketknife Does
**Pocketknife automatically registers listeners and commands in all classes in the plugin.**
For example, a developer only needs to have their listeners class implement `Listener`, and Pocketknife does the rest. No registration in the main class required.

Each class file that has listeners or extends `PocketknifeFeature` is considered a "feature".
Features can be toggled on and off, so they can be put away when they aren't needed and are ready for immediate reactivation.

All commands are under the `/pocketknife` command. For example, if I wanted to run the command method from "SendUp", I would run `/pocketknife SendUp <args>`.
All command classes extend `PocketknifeSubcommand`.
