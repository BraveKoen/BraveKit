package me.koen.braveKit;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.koen.braveKit.KitDatabase.DatabaseManager;
import me.koen.braveKit.KitInventory.KitInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public final class BraveKit extends JavaPlugin {


    @Override
    public void onEnable() {

        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getPluginLoader().disablePlugin(this);
            return;
        }

        FileConfiguration config = getConfig();
        getLogger().info("BraveKit has been enabled!");

        DatabaseManager databaseManager = new DatabaseManager(config);
        if (!databaseManager.isConntected()) {
            getLogger().severe("Failed to initialize database. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        getServer().getPluginManager().registerEvents(new KitInventory(), this);

        getCommand("kits").setExecutor(new OpenKitSelector(databaseManager));


    }

    @Override
    public void onDisable() {
        saveDefaultConfig();
        getLogger().info("Your plugin has been disabled!");
    }
}
