package me.koen.braveKit;

import org.bukkit.plugin.java.JavaPlugin;

public final class BraveKit extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("BraveKit has been enabled!");

        getCommand("kits").setExecutor(new OpenKitSelector());


    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("Your plugin has been disabled!");
    }
}
