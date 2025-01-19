package me.koen.braveKit.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Kit {
    private final String name;
    private final Material icon;
    private final List<String> description;
    private final ItemStack[] items;

    public Kit(String name, Material icon, List<String> description, ItemStack[] items) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public List<String> getDescription() {
        return description;
    }

    public void giveTo(Player player) {
        player.getInventory().addItem(items);
    }
}