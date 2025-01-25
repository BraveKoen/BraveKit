package me.koen.braveKit.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Kit {
    private static final Logger log = LoggerFactory.getLogger(Kit.class);
    private final int kitId;
    private final String name;
    private final ItemStack icon;
    private final List<String> description;
    private final ItemStack[] items;
    private final int timeout;


    public Kit(int kitId, String name, ItemStack icon, List<String> description, ItemStack[] items) {
        this(kitId, name, icon, description, items, 30); // Default timeout of 30
    }

    public Kit(int kitId, String name, ItemStack icon, List<String> description, ItemStack[] items, int timeout) {
        log.info("Creating kit " + kitId + ": " + name);
        this.kitId = kitId;
        ItemMeta meta = icon.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§6" + name);  // Set name (§ is color code)
        meta.setLore(description);
        icon.setItemMeta(meta);

        this.name = name;
        this.icon = icon;
        this.description = description;
        this.items = items;
        this.timeout = timeout;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public int getKitId() {
        return kitId;
    }
    public int getTimeout() {
        return timeout;
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