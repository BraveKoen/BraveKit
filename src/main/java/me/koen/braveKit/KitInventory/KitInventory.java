package me.koen.braveKit.KitInventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class KitInventory implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("KitInventory")) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        String kitName = clickedItem.getItemMeta().getDisplayName()
                .replace("§8", "")
                .replace(" Kit", "")
                .trim();

        Player player = (Player) event.getWhoClicked();

        player.sendMessage("Raw kitName: '" + kitName + "'");
        player.sendMessage("Length: " + kitName.length());

        if (kitName.equalsIgnoreCase("DiaPick")) {
            player.sendMessage(kitName + " has been clicked.");
            player.sendMessage("Nice!");
            player.closeInventory();
        }
    }
}
