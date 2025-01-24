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
    private final Inventory inv;
    private Map<String, Kit> kitList;
    private final OpenKitSelector kitSelector;

    public KitUI(String kitUIName, NamespacedKey nameSpaceKey , OpenKitSelector kitSelector) {
        this.kitNameKey = nameSpaceKey;
        this.inv = Bukkit.createInventory(null, 9,kitUIName);
        this.kitSelector = kitSelector;
    }

    public void initializeItems() {
        inv.clear();
        // Add kits to inventory
        for (Kit kit : kitList.values()) {
            ItemStack icon = kit.getIcon();
            inv.addItem(icon);
        }
    }

    public void updateKits(Map<String, Kit> kitList){
        this.kitList = kitList;
        initializeItems();
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        e.setCancelled(true);

        @Nullable final ItemStack clickedItem = e.getCurrentItem();


        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        // Using slots click is a best option for your inventory click's
        p.sendMessage("You clicked at slot " + e.getRawSlot());
        p.sendMessage("Model data " + clickedItem.getItemMeta().getCustomModelData());
        p.sendMessage("Model data " + clickedItem.getItemMeta().getDisplayName().toLowerCase());
        kitSelector.givePlayerKit(p, clickedItem.getItemMeta().getDisplayName().toLowerCase());

    }


    public void openInventory(final Player ent) {
        ent.openInventory(inv);

    }
}
