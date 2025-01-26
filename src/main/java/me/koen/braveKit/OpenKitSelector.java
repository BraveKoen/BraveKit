package me.koen.braveKit;

import me.koen.braveKit.KitDatabase.DatabaseManager;
import me.koen.braveKit.KitInventory.KitUI;
import me.koen.braveKit.kit.Kit;
import me.koen.braveKit.kit.KitParser;


import me.koen.braveKit.kit.KitBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;


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
        String secretKey = validateConfigString("kit-key", "KitSecred", 100, "[a-zA-Z0-9]+");

        this.kitNameKey = new NamespacedKey(plugin, secretKey);

        this.kitUI = new KitUI(kitName, kitNameKey, this);
        plugin.getServer().getPluginManager().registerEvents(kitUI, plugin);
        configKitToDatabase();
        refreshKits();
    }

    public void givePlayerKit(Player player, int kitId) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        Kit kit = kits.get(kitId);
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "ERROR: No kit found with ID: '" + kitId + "'");
            return;
        }

        try {
            if (!database.canUseKit(player, kitId, kit.getTimeout())) {
                player.sendMessage(ChatColor.RED + "Kit is on cooldown");
                return;
            }

            database.recordKitUsage(player, kitId);
            kit.giveTo(player);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error accessing kit database");
            plugin.getLogger().log(Level.WARNING, "Database error while handling kit " + kitId, e);
        }
    }

    public void refreshKits() {
        kits.clear();
        try {
            Map<Integer, Kit> newKits = database.getAllKits();
            kits.putAll(newKits);
            kitUI.updateKits(Map.copyOf(kits));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to refresh kits: " + e.getMessage());
        }
    }

    private void configKitToDatabase() {
        List<Kit> l = KitParser.getKitsFromConfig(pluginConfig);
        for(Kit kit : l) {
            if(database.findKitByName(kit.getName())){
                plugin.getLogger().info("Kit " + kit.getName() + " already exists in database. Skipping.");
                continue;
            }
            database.saveKit(kit);
            plugin.getLogger().info(ChatColor.GREEN + "Kit " + kit.getKitId() + ": " + kit.getName());
        }
    }

    public void debugbrave(Player player) {
        for(int kitId : kits.keySet()) {
            player.sendMessage(ChatColor.GREEN + "Kit " + kitId + ": " + kits.get(kitId).getName());
            player.sendMessage(ChatColor.GREEN + "kitId: " + kits.get(kitId).getKitId());
        }

    }

    private void createKit(Player player, String[] args) {

        if (args == null || args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /createkit <kit name>");
            return;
        }

        String kitName = args[0].trim();
        if (kitName.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Kit name cannot be empty");
            return;
        }

        KitBuilder builder = new KitBuilder(database.getKitId(), plugin, kitNameKey, player, kitName);
        builder.buildKit()
                .thenAccept(kit -> {
                    if (kit != null) {
                        try {
                            kits.put(kit.getKitId(), kit);
                            database.saveKit(kit);
                            player.sendMessage(ChatColor.GREEN + "Kit '" + kitName + "' created successfully");
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "Error saving kit to database");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Kit creation failed");
                    }
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace(); // Replace with proper logging
                    player.sendMessage(ChatColor.RED + "Error creating kit");
                    return null;
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
                createKit(player, args);
                break;
            case "refreshkits":
                refreshKits();
                break;
            case "debugbrave":
                debugbrave(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command!");
                break;
        }
        return true;
    }
}
