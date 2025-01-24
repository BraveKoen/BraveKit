package me.koen.braveKit;

import me.koen.braveKit.KitDatabase.DatabaseManager;
import me.koen.braveKit.KitInventory.KitUI;
import me.koen.braveKit.kit.Kit;

import me.koen.braveKit.kit.KitBuilder;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;


public class OpenKitSelector implements CommandExecutor {
    private final DatabaseManager database;
    private Map<Integer, Kit> kits = new HashMap<>();
    private final NamespacedKey kitNameKey;
    private final KitUI kitUI;
    private final Plugin plugin;
    private final FileConfiguration pluginConfig;

    public OpenKitSelector(BraveKit plugin, DatabaseManager database, FileConfiguration pluginConfig) {
        this.plugin = plugin;
        this.database = database;
        this.pluginConfig = pluginConfig;

        String kitName = validateConfigString("kit-ui-name", "Default Kit Menu", 32, "[a-zA-Z0-9 ]+");
        String secredKey = validateConfigString("kit-key", "KitSecred", 100, "[a-zA-Z0-9]+");

        this.kitNameKey = new NamespacedKey(plugin, secredKey);

        this.kitUI = new KitUI(kitName, kitNameKey, this);
        plugin.getServer().getPluginManager().registerEvents(kitUI, plugin);
    }

    public void givePlayerKit(Player player, int kitId) {
        Kit kit = kits.get(kitId);
        if (kit == null) {
            player.sendMessage("ERROR: No kit found with ID: '" + kitId + "'");
            return;
        }

        //check if player has a countdown on the kit

        kit.giveTo(player);

    }

    public void refreshKits() {
        kits.putAll(database.getAllKits());
        kitUI.updateKits(kits);
    }

    private void CreateKit(Player player, String[] args) {

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /CreateKit <kit name>");
            return;
        }

        KitBuilder builder = new KitBuilder(database.getKitId(), plugin, kitNameKey, player, args[0]);
        builder.buildKit().thenAccept(kit -> {
            if (kit != null) {
                kits.put(kit.getKitId(), kit);
                database.SaveKit(kit);
            }else{
                player.sendMessage("Kit not created");
            }
        });
    }

    private String validateConfigString(String configPath, String defaultValue, int maxLength, String pattern) {
        if (!pluginConfig.contains(configPath)) {
            plugin.getLogger().warning("Missing " + configPath + " in config! Using default value.");
            return defaultValue;
        }

        String configValue = pluginConfig.getString(configPath);
        if (configValue == null || configValue.trim().isEmpty()) {
            plugin.getLogger().warning("Invalid " + configPath + " in config! Using default value.");
            return defaultValue;
        }

        // Length validation
        if (configValue.length() > maxLength) {
            plugin.getLogger().warning(configPath + " too long! Truncating to " + maxLength + " characters.");
            return configValue.substring(0, maxLength);
        }

        // Character validation with custom pattern
        if (pattern != null && !configValue.matches(pattern)) {
            plugin.getLogger().warning(configPath + " contains invalid characters! Using default value.");
            return defaultValue;
        }

        return configValue;
    }

        @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        switch (label.toLowerCase()) {
            case "kits":
                kitUI.openInventory(player);
                break;
            case "createkit":
                CreateKit(player, args);
                break;
            case "refreshkits":
                refreshKits();
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command!");
                break;
        }
        return true;
    }
}
