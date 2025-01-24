package me.koen.braveKit.KitInventory;

import me.koen.braveKit.BraveKit;
import me.koen.braveKit.OpenKitSelector;
import me.koen.braveKit.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Map;

public class KitUI implements Listener {
    private final NamespacedKey kitNameKey;
    private Inventory inv;
    private Map<Integer, Kit> kitList;
    private final OpenKitSelector kitSelector;
    private int startSize = 9;
    private final String kitUIName;

    public KitUI(String kitUIName, NamespacedKey nameSpaceKey , OpenKitSelector kitSelector) {
        this.kitNameKey = nameSpaceKey;
        this.kitUIName = kitUIName;
        this.inv = Bukkit.createInventory(null, startSize,kitUIName);
        this.kitSelector = kitSelector;
    }

    /**
     * Initializes the inventory with kit icons. This method first clears the existing inventory,
     * then populates it with icons representing each available kit.
     *
     * @implNote Each kit's icon is added to the inventory
     *
     * @see Kit#getIcon()
     */
    public void initializeItems() {
        inv.clear();
        for (Kit kit : kitList.values()) {
            ItemStack icon = kit.getIcon();
            inv.addItem(icon);
        }
    }

    /**
     * Updates the kit inventory and resizes it if necessary to accommodate all kits.
     * The inventory size will always be a multiple of 9 to maintain Minecraft's standard layout.
     *
     * @param kitList The new map of kits to be displayed in the inventory
     * @implNote If the number of kits exceeds the current inventory size, it will resize to the next multiple of 9
     */
    public void updateKits(Map<Integer, Kit> kitList) {
        int requiredSlots = kitList.size();
        int newSize = calculateInventorySize(requiredSlots);

        if (newSize > startSize) {
            this.inv = Bukkit.createInventory(null, newSize, kitUIName);
            this.startSize = newSize;
        }

        this.kitList = kitList;
        initializeItems();
    }

    /**
     * Calculates the required inventory size based on the number of items.
     * Returns the next multiple of 9 that can hold the specified number of items.
     *
     * @param items Number of items that need to be stored
     * @return The calculated inventory size (always a multiple of 9)
     */
    private int calculateInventorySize(int items) {
        return ((items + 8) / 9) * 9; // Rounds up to next multiple of 9
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        e.setCancelled(true);

        @Nullable final ItemStack clickedItem = e.getCurrentItem();


        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        kitSelector.givePlayerKit(p, clickedItem.getItemMeta().getCustomModelData());

    }


    public void openInventory(final Player ent) {
        ent.openInventory(inv);
    }
}
