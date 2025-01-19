package me.koen.braveKit;

import me.koen.braveKit.KitInventory.KitInventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class BraveKit extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("BraveKit has been enabled!");

        getServer().getPluginManager().registerEvents(new KitInventory(), this);

        getCommand("kits").setExecutor(new OpenKitSelector());


    }

    @Override
    public void onDisable() {
        getLogger().info("Your plugin has been disabled!");
    }
}
