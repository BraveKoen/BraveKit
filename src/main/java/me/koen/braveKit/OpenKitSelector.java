package me.koen.braveKit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;


public class OpenKitSelector implements CommandExecutor {

    public void KitSelector(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27,"Open Kit Selector");
        player.openInventory(inventory);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        KitSelector((Player) sender);
        return true;
    }
}
