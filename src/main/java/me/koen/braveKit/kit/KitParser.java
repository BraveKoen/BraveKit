package me.koen.braveKit.kit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KitParser {
    private static final Logger log = LoggerFactory.getLogger(KitParser.class);

    public static List<Kit> getKitsFromConfig(FileConfiguration pluginConfig) {
        List<Kit> configKits = new ArrayList<>();
        ConfigurationSection kitsSection = pluginConfig.getConfigurationSection("kits");
        if (kitsSection != null) {
            for (String key : kitsSection.getKeys(false)) {
                try {
                    ConfigurationSection kitSection = kitsSection.getConfigurationSection(key);
                    // Parse the kit
                    Kit kit = parseKitFromConfig(kitSection);

                    if (kit != null) {
                        configKits.add(kit);
                    }
                } catch (NumberFormatException e) {
                    log.info(e.getMessage());
                }
            }
        }
        return configKits;
    }

    private static Kit parseKitFromConfig(ConfigurationSection section) {
        String name = section.getString("name");
        List<String> description = section.getStringList("description"); // Get description as a list
        int cooldown = section.getInt("cooldown", 0); // Default cooldown to 0 if not specified

        String icon = section.getString("icon");
        Material iconMaterial = Material.matchMaterial(icon);
        ItemStack iconStack = new ItemStack(iconMaterial, 1);

        // Parse items
        List<ItemStack> items = new ArrayList<>();
        List<Map<?, ?>> itemMaps = section.getMapList("items");
        for (Map<?, ?> itemMap : itemMaps) {
            ItemStack item = getConfigItem(itemMap);
            items.add(item);
        }
        ItemStack[] itemsStack = items.toArray(new ItemStack[0]);
        // Create and return the Kit object
        return new Kit(-1,name, iconStack, description, itemsStack, cooldown);
    }

    private static ItemStack getConfigItem(Map<?, ?> itemMap) {
        // Get material and amount from the map
        String materialName = (String) itemMap.get("material");
        int amount = (int) itemMap.get("amount");

        // Match the material name to a Material enum
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + materialName);
        }

        // Create the ItemStack
        ItemStack currentItem = new ItemStack(material, amount);

        // Get the enchantments from the map
        List<Map<?, ?>> enchantments = (List<Map<?, ?>>) itemMap.get("enchantments");

        // If enchantments are specified, apply them to the item
        if (enchantments != null) {
            for (Map<?, ?> enchantmentMap : enchantments) {
                String enchName = (String) enchantmentMap.get("type");
                int level = (int) enchantmentMap.get("level");

                // Match the enchantment name to an Enchantment enum
                Enchantment enchantment = Enchantment.getByName(enchName);
                if (enchantment == null) {
                    throw new IllegalArgumentException("Invalid enchantment: " + enchName);
                }

                // Add the enchantment to the item
                currentItem.addEnchantment(enchantment, level);
            }
        }
        return currentItem;
    }




}
