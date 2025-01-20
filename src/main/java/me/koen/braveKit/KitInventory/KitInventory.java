package me.koen.braveKit.KitInventory;

import me.koen.braveKit.BraveKit;
import me.koen.braveKit.OpenKitSelector;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class KitInventory implements Listener {
    private final NamespacedKey kitNameKey;
    private final OpenKitSelector kitSelector;

    public KitInventory(BraveKit plugin, OpenKitSelector kitSelector) {
        this.kitNameKey = new NamespacedKey(plugin, "kit-name");
        this.kitSelector = kitSelector;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("KitInventory")) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        ItemMeta itemData = clickedItem.getItemMeta();
        PersistentDataContainer container = itemData.getPersistentDataContainer();

        String name = container.get(kitNameKey, PersistentDataType.STRING);


        kitSelector.givePlayerKit(player, name);

    }
}
