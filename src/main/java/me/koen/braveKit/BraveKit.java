package me.koen.braveKit;

import de.tr7zw.nbtapi.NBT;
import me.koen.braveKit.KitDatabase.DatabaseManager;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public final class BraveKit extends JavaPlugin {
    FileConfiguration config = getConfig();
    DatabaseManager databaseManager = new DatabaseManager(config);

    @Override
    public void onEnable() {

        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getPluginLoader().disablePlugin(this);
            return;
        }

        if (!databaseManager.isConntected()) {
            getLogger().severe("Failed to initialize database. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        OpenKitSelector kitCommandHandler = new OpenKitSelector(this, databaseManager, config);

        getCommand("kits").setExecutor(kitCommandHandler);
        getCommand("createkit").setExecutor(kitCommandHandler);
        getCommand("refreshkits").setExecutor(kitCommandHandler);
        getCommand("debugbrave").setExecutor(kitCommandHandler);

        getLogger().info("BraveKit has been enabled!");
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
        saveDefaultConfig();
        getLogger().info("Your plugin has been disabled!");
    }
}
