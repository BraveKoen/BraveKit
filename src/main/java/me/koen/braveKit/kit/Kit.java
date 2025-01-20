package me.koen.braveKit.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class Kit {
    private final String name;
    private final ItemStack icon;
    private final List<String> description;
    private final ItemStack[] items;

    public Kit(String name, ItemStack icon, List<String> description, ItemStack[] items) {

        ItemMeta meta = icon.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§6" + name);  // Set name (§ is color code)
        meta.setLore(description);
        meta.setCustomModelData(1234);

        icon.setItemMeta(meta);

        this.name = name;
        this.icon = icon;
        this.description = description;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public List<String> getDescription() {
        return description;
    }

    public void giveTo(Player player) {
        // Filter out null items and add them one by one
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item.clone());
            }
        }
    }
}